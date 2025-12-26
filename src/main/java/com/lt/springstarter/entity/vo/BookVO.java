package com.lt.springstarter.entity.vo;

import com.lt.springstarter.entity.dto.BookWithAuthorDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookVO {
    private Integer id;
    private String name;
    private Integer authorId;
    private String authorName;

    public static BookVO from(BookWithAuthorDTO bookWithAuthorDTO) {
        return BookVO.builder()
                .id(bookWithAuthorDTO.getId())
                .name(bookWithAuthorDTO.getName())
                .authorId(bookWithAuthorDTO.getAuthorId())
                .authorName(bookWithAuthorDTO.getAuthorName())
                .build();
    }
}
