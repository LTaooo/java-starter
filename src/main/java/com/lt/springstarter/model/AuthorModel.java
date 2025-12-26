package com.lt.springstarter.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("author")
public class AuthorModel {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;
}
