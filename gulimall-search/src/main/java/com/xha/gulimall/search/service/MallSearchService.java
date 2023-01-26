package com.xha.gulimall.search.service;

import com.xha.gulimall.search.dto.ParamDTO;
import com.xha.gulimall.search.vo.SearchResponseVO;
import org.springframework.stereotype.Service;

@Service
public interface MallSearchService {


    SearchResponseVO searchProducts(ParamDTO paramDTO);
}
