package com.xha.gulimall.search.service;

import com.xha.gulimall.common.to.es.SpuInfoES;

import java.io.IOException;
import java.util.List;

public interface ElasticSaveService {
    boolean upProduct(List<SpuInfoES> upProducts) throws IOException;
}
