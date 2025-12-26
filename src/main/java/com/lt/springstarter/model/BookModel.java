package com.lt.springstarter.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@TableName("book")
@Data
public class BookModel {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private Integer authorId;

    @TableField(fill = FieldFill.INSERT)
    private Long createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}
