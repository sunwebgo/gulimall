package com.xha.gulimall.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttrDTO {
    private Long attrId;

    private String attrName;

    private List<String> attrValue;
}
