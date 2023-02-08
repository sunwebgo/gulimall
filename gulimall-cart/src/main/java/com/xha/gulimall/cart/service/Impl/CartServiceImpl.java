package com.xha.gulimall.cart.service.Impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.xha.gulimall.cart.feign.ProductFeign;
import com.xha.gulimall.cart.interceptor.CartInterceptor;
import com.xha.gulimall.cart.service.CartService;
import com.xha.gulimall.cart.to.UserInfoTO;
import com.xha.gulimall.cart.vo.CartInfoVO;
import com.xha.gulimall.cart.vo.CartVO;
import com.xha.gulimall.common.constants.CacheConstants;
import com.xha.gulimall.common.to.CartInfoTO;
import com.xha.gulimall.common.to.SkuInfoTO;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ProductFeign productFeign;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 添加到购物车
     *
     * @param skuId
     * @param num
     * @return {@link CartInfoVO}
     */
    @Override
    public CartInfoVO addToCart(String skuId, Integer num) throws ExecutionException, InterruptedException {

//        1.首先查询缓存中是否存在当前商品
        Object cacheSkuInfo = getBoundHashOps().get(skuId);
//          1.1没有当前商品
        CartInfoVO cartInfoVO = null;
        if (Objects.isNull(cacheSkuInfo)) {
//          1.2获取到商品信息并存入缓存
            cartInfoVO = getCartInfoVO(skuId, num);
        } else {
//          1.3有当前商品:增加商品数量,修改总价格
            cartInfoVO = JSONUtil.toBean(JSONUtil.toJsonStr(cacheSkuInfo), CartInfoVO.class);
            cartInfoVO
                    .setCount(cartInfoVO.getCount() + num)
                    .setTotalPrice(
                            cartInfoVO
                                    .getTotalPrice()
                                    .add(cartInfoVO.getPrice()
                                            .multiply(BigDecimal.valueOf(num))));
//          2.4更新缓存
            getBoundHashOps().put(skuId, JSONUtil.toJsonStr(cartInfoVO));
        }
        return cartInfoVO;
    }

    /**
     * 判断当前用户的登录状态
     * 判断hash结构的key
     *
     * @return {@link String}
     */
    private BoundHashOperations<String, Object, Object> getBoundHashOps() {
        //          1.1判断用户的登录状态,组装key
        String cartStr = "";
        UserInfoTO userInfoTO = CartInterceptor.threadLocal.get();
        if (!Objects.isNull(userInfoTO.getUserId())) {
            cartStr = CacheConstants.CART_CACHE + userInfoTO.getUserId();
        } else {
//          5.2临时用户
            cartStr = CacheConstants.CART_CACHE + userInfoTO.getUserKey();
        }
        return stringRedisTemplate.boundHashOps(cartStr);
    }

    /**
     * 获取到商品信息并存入缓存
     *
     * @param skuId sku id
     * @param num
     * @return {@link CartInfoVO}
     * @throws InterruptedException 中断异常
     * @throws ExecutionException   执行异常
     */
    private CartInfoVO getCartInfoVO(String skuId, Integer num) throws InterruptedException, ExecutionException {
        CartInfoVO cartInfoVO = new CartInfoVO();
//        1.采用CompletableFuture实现线程的异步编排
//        2.根据skuId远程查询商品信息
        CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
            SkuInfoTO skuInfo = productFeign.getSkuInfo(Long.valueOf(skuId))
                    .getData("skuInfo", new TypeReference<SkuInfoTO>() {
                    });
//          2.1封装上述商品
            cartInfoVO.setSkuId(skuInfo.getSkuId())
                    .setCheck(true)
                    .setCount(num)
                    .setImage(skuInfo.getSkuDefaultImg())
                    .setTitle(skuInfo.getSkuTitle())
                    .setPrice(skuInfo.getPrice())
                    .setTotalPrice(skuInfo.getPrice().multiply(BigDecimal.valueOf(num)));
        }, threadPoolExecutor);

//        3.根据skuId查询商品的销售属性信息
        CompletableFuture<Void> getSaleAttrList = CompletableFuture.runAsync(() -> {
            List<String> saleAttrList = productFeign.getSaleAttrBySkuId(Long.valueOf(skuId));
            cartInfoVO.setSkuAttr(saleAttrList);
        }, threadPoolExecutor);

//        4.等待所有线程完成
        CompletableFuture
                .allOf(getSkuInfo, getSaleAttrList)
                .get();
//        5.将当前商品保存到redis中

//          5.3以Hash的数据形式保存到redis中
        getBoundHashOps().put(cartInfoVO.getSkuId().toString(),
                JSONUtil.toJsonStr(cartInfoVO));
        return cartInfoVO;
    }


    /**
     * 获取到当前添加成功的购物项
     *
     * @param skuId sku id
     * @return {@link CartInfoVO}
     */
    @Override
    public CartInfoVO getCartItem(String skuId) {
        String skuInfo = JSONUtil.toJsonStr(getBoundHashOps().get(skuId));
//        将JSON数据转换为CartInfoVO对象
        CartInfoVO cartInfoVO = JSONUtil.toBean(skuInfo, CartInfoVO.class);
        return cartInfoVO;
    }

    /**
     * 获取到购物车
     *
     * @return {@link CartVO}
     */
    @Override
    public CartVO getCart() throws ExecutionException, InterruptedException {
        CartVO cartVO = new CartVO();
        UserInfoTO userInfoTO = CartInterceptor.threadLocal.get();
//        1.调用getBoundHashOps方法,判断当前是登录用户还是临时用户
        String key = getBoundHashOps().getKey();
//          1.1当前是登录用户
        if (key.equals(CacheConstants.CART_CACHE + userInfoTO.getUserId())) {
//        2.获取到当前登录用户的购物车数据
            List<Object> currentUserCartOrigin = getBoundHashOps().values();

//        3.获取到当前临时用户的购物车数据
            List<Object> temporaryCartListOrigin = stringRedisTemplate.opsForHash().getOperations().boundHashOps(CacheConstants.CART_CACHE + userInfoTO.getUserKey()).values();

            List<CartInfoVO> temporaryCartList = null;
//        4.如果临时购物车的数据不为空
            if (!CollectionUtils.isEmpty(temporaryCartListOrigin)) {
                temporaryCartList = typeSwitch(temporaryCartListOrigin);
//          4.1删除临时购物车缓存
                stringRedisTemplate.delete(CacheConstants.CART_CACHE + userInfoTO.getUserKey());

//        5.如果当前用户的购物车有数据
                if (!CollectionUtils.isEmpty(currentUserCartOrigin)) {
                    List<CartInfoVO> cartInfoList = typeSwitch(currentUserCartOrigin);
//          5.1合并用户购物车和临时购物车
                    for (CartInfoVO cartInfoVO : temporaryCartList) {
                        addToCart(cartInfoVO.getSkuId().toString(), cartInfoVO.getCount());
                    }
                    cartInfoList.addAll(temporaryCartList);
//          5.2得到商品总数量
                    Integer productNums = getProductTotalNum(cartInfoList);
//          5.3得到商品总价格
                    BigDecimal totalPrices = getProductTotalPrice(cartInfoList);
                    cartVO.setItems(cartInfoList)
                            .setProductNum(productNums)
                            .setProductTypeNum(cartInfoList.size())
                            .setTotalAmountPrice(totalPrices);
                } else {
//        6.当前登录的用户首次并没有添加商品到购物车,所以只将临时购物的商品添加进去
                    for (CartInfoVO cartInfoVO : temporaryCartList) {
                        addToCart(cartInfoVO.getSkuId().toString(), cartInfoVO.getCount());
                    }
//          6.1得到商品总数量
                    Integer productNums = getProductTotalNum(temporaryCartList);
//          6.2得到商品总价格
                    BigDecimal totalPrices = getProductTotalPrice(temporaryCartList);
                    cartVO.setItems(temporaryCartList)
                            .setProductNum(productNums)
                            .setProductTypeNum(temporaryCartList.size())
                            .setTotalAmountPrice(totalPrices);
                }
            } else {
//        7.临时购物车的数据为空，当前用户的购物车有数据
                if (!CollectionUtils.isEmpty(currentUserCartOrigin)) {
                    List<CartInfoVO> cartInfoList = typeSwitch(currentUserCartOrigin);
//          7.1得到商品总数量
                    Integer productNums = getProductTotalNum(cartInfoList);
//          7.2得到商品总价格
                    BigDecimal totalPrices = getProductTotalPrice(cartInfoList);
                    cartVO.setItems(cartInfoList)
                            .setProductNum(productNums)
                            .setProductTypeNum(cartInfoList.size())
                            .setTotalAmountPrice(totalPrices);
                }
            }
        } else {
//          1.2当前是临时用户,获取到临时用的购物车列表
            List<Object> temporaryUserCart = getBoundHashOps().values();
            if (!CollectionUtils.isEmpty(temporaryUserCart)) {
                List<CartInfoVO> temporaryUserCartList = typeSwitch(temporaryUserCart);
                temporaryUserCartList = filterCartListByCheck(temporaryUserCartList);
//          等到商品总数量
                Integer productNums = getProductTotalNum(temporaryUserCartList);
//          得到商品总价格
                BigDecimal totalPrices = getProductTotalPrice(temporaryUserCartList);

                cartVO.setItems(temporaryUserCartList)
                        .setProductNum(productNums)
                        .setProductTypeNum(temporaryUserCart.size())
                        .setTotalAmountPrice(totalPrices);
            }
        }
        return cartVO;
    }

    /**
     * 通过检查过滤购物车列表
     *
     * @param cartList 临时购物车列表
     * @return {@link List}<{@link CartInfoVO}>
     */
    private List<CartInfoVO> filterCartListByCheck(List<CartInfoVO> cartList) {
        cartList = cartList
                .stream()
                .filter(cartInfoVO -> cartInfoVO.getCheck() == true)
                .collect(Collectors.toList());
        return cartList;
    }


    /**
     * 类型转换：将List<Object>转换为List<CartInfoVO>
     *
     * @param ObjCartInfoVO obj购物车信息签证官
     * @return {@link List}<{@link CartInfoVO}>
     */
    private List<CartInfoVO> typeSwitch(List<Object> ObjCartInfoVO) {
        List<CartInfoVO> cartInfoVOList = ObjCartInfoVO.stream().map(userCart -> {
            CartInfoVO cartInfoVO = JSONUtil.toBean
                    (JSONUtil.toJsonStr(userCart), CartInfoVO.class);
            return cartInfoVO;
        }).collect(Collectors.toList());
        return cartInfoVOList;
    }


    /**
     * 得到产品总价格
     *
     * @param cartList 临时用户购物车列表
     * @return {@link BigDecimal}
     */
    private BigDecimal getProductTotalPrice(List<CartInfoVO> cartList) {
//        1.过滤选中状态
        cartList = filterCartListByCheck(cartList);
//        2.获取到商品总价格列表
        List<BigDecimal> productPriceList = cartList.stream().map(userCart -> {
            return userCart.getTotalPrice();
        }).collect(Collectors.toList());

//        3.对商品价格列表求和
        BigDecimal totalPrices = new BigDecimal(0);
        for (BigDecimal productPrice : productPriceList) {
            totalPrices = totalPrices.add(productPrice);
        }
        return totalPrices;
    }

    /**
     * 得到商品总数量
     *
     * @param cartList 临时用户购物车列表
     * @return {@link Integer}
     */
    private Integer getProductTotalNum(List<CartInfoVO> cartList) {
//        1.过滤选中状态
        cartList = filterCartListByCheck(cartList);
//        2.获取到商品数量列表
        List<Integer> productNumList = cartList.stream().map(userCart -> {
            return userCart.getCount();
        }).collect(Collectors.toList());

//        3.对商品列表求和
        Integer productNums = 0;
        for (Integer productNum : productNumList) {
            productNums += productNum;
        }
        return productNums;
    }

    /**
     * 更新选中状态
     *
     * @param skuId sku id
     * @param check 检查
     */
    @Override
    public void updateCheckStatus(Long skuId, Integer check) {
//        1.根据skuId获取到缓存中的sku信息
        CartInfoVO cartInfo = getCartItem(skuId.toString());
        cartInfo.setCheck(check == 1);
//        2.更新缓存
        getBoundHashOps().put(skuId.toString(), JSONUtil.toJsonStr(cartInfo));
    }

    /**
     * 更新商品数量
     *
     * @param skuId sku id
     * @param num
     * @return {@link String}
     */
    @Override
    public void updateNum(Long skuId, Integer num) {
//        1.根据skuId获取到缓存中的sku信息
        CartInfoVO cartInfo = getCartItem(skuId.toString());
        cartInfo.setCount(num);
        cartInfo.setTotalPrice(cartInfo.getPrice().multiply(BigDecimal.valueOf(num)));

//        2.更新缓存
        getBoundHashOps().put(skuId.toString(), JSONUtil.toJsonStr(cartInfo));
    }

    /**
     * 删除产品
     *
     * @param skuId sku id
     */
    @Override
    public void deleteProduct(Long skuId) {
        getBoundHashOps().delete(skuId.toString());
    }

    /**
     * 获取用户购物车条目
     *
     * @return {@link List}<{@link CartInfoTO}>
     */
    @Override
    public List<CartInfoTO> getUserCartItems() {
//        1.获取到当前登录用户的id
        Long userId = CartInterceptor.threadLocal.get().getUserId();
        List<CartInfoTO> cartInfoTOList = null;
        if (Objects.isNull(userId)) {
            return null;
        } else {
//        2.获取到当前用户的购物车商品列表
            List<Object> cartItemList = stringRedisTemplate.boundHashOps(CacheConstants.CART_CACHE + userId).values();

            cartInfoTOList = cartItemList.stream().map(cartItem -> {
                return JSONUtil.toBean(JSONUtil.toJsonStr(cartItem), CartInfoTO.class);
            }).collect(Collectors.toList());

//            2.1过滤未选中的商品
            cartInfoTOList = cartInfoTOList
                    .stream()
                    .filter(cartInfoTO -> cartInfoTO.getCheck() == true)
                    .map(cartInfoTO -> {
                        String dataPrice = productFeign.getSkuPrice(cartInfoTO.getSkuId()).getData(new TypeReference<String>() {
                        });
                        cartInfoTO.setPrice(new BigDecimal(dataPrice));
                        cartInfoTO.setTotalPrice(cartInfoTO.getPrice().multiply(BigDecimal.valueOf(cartInfoTO.getCount())));
                        return cartInfoTO;
                    }).collect(Collectors.toList());
        }
        return cartInfoTOList;
    }

}
