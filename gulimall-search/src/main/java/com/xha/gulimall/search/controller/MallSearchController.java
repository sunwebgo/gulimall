package com.xha.gulimall.search.controller;

import com.xha.gulimall.search.dto.ParamDTO;
import com.xha.gulimall.search.dto.ResponseDTO;
import com.xha.gulimall.search.service.MallSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;

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
    public String searchProducts(@RequestBody ParamDTO paramDTO, Model model) {
        ResponseDTO searchResult = mallSearchService.searchProducts(paramDTO);
        model.addAttribute("searchResult", searchResult);
        return "list";
    }


}
