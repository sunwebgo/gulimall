package com.xha.gulimall.search.controller;

import com.xha.gulimall.search.dto.ParamDTO;
import com.xha.gulimall.search.service.MallSearchService;
import com.xha.gulimall.search.vo.SearchResponseVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
public class MallSearchController {

    @Resource
    private MallSearchService mallSearchService;

    /**
     * 搜索产品
     *
     * @param paramDTO param dto
     * @return {@link String}
     */
    @GetMapping("/list.html")
    public String searchProducts(ParamDTO paramDTO, Model model, HttpServletRequest request) {
        paramDTO.setQueryString(request.getQueryString());
        SearchResponseVO searchResult = mallSearchService.searchProducts(paramDTO);
        model.addAttribute("searchResult", searchResult);
        System.out.println(searchResult);
        return "list";
    }


}
