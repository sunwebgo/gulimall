package com.xha.gulimall.product.web;

import com.xha.gulimall.product.entity.CategoryEntity;
import com.xha.gulimall.product.service.CategoryService;
import com.xha.gulimall.product.vo.Catelog2VO;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class IndexController {

    @Resource
    private CategoryService categoryService;

    /**
     * 跳转首页，并且查询出所有一级分类列表
     *
     * @param model 模型
     * @return {@link String}
     */
    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
//        1.查询出所有一级分类
        List<CategoryEntity> firstCategory = categoryService.getFirstCategory();
        model.addAttribute("firstCategory", firstCategory);
        return "index";
    }

    /**
     * 获取到所有分类列表
     *
     * @return {@link Map}<{@link String}, {@link List}<{@link Catelog2VO}>>
     */
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2VO>> getCatalogJson() {
        Map<String, List<Catelog2VO>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }


}
