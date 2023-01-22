package com.xha.gulimall.search.service;

import com.xha.gulimall.common.to.es.SkuInfoES;

import java.io.IOException;
import java.util.List;

public interface ElasticSaveService {
    boolean upProduct(List<SkuInfoES> upProducts) throws IOException;
}
