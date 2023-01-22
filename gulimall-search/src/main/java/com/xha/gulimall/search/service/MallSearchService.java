package com.xha.gulimall.search.service;

import com.xha.gulimall.search.dto.ParamDTO;
import com.xha.gulimall.search.dto.ResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface MallSearchService {


    ResponseDTO searchProducts(ParamDTO paramDTO);
}
