package com.lt.springstarter.entity.dto;

import lombok.Data;

@Data
public class BookWithAuthorDTO {
    private Integer id;
    private String name;
    private Integer authorId = 0;
    private String authorName = "";
}
