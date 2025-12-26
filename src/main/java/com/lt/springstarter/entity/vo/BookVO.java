package com.lt.springstarter.entity.vo;

import lombok.Data;

@Data
public class BookVO {
    private Integer id;
    private String name;
    private Integer authorId;
    private String authorName = "";
}
