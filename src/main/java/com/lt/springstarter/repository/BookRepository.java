package com.lt.springstarter.repository;

import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.lt.springstarter.base.BaseRepository;
import com.lt.springstarter.entity.dto.BookWithAuthorDTO;
import com.lt.springstarter.mapper.BookMapper;
import com.lt.springstarter.model.AuthorModel;
import com.lt.springstarter.model.BookModel;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository extends BaseRepository<BookMapper, BookModel> {
    public BookRepository(BookMapper mapper) {
        super(mapper);
    }

    public @Nullable BookWithAuthorDTO getBookWithAuthor(Integer id) {
        MPJLambdaWrapper<BookModel> wrapper = JoinWrappers.lambda(BookModel.class)
                .selectAll(BookModel.class)
                .selectAs(AuthorModel::getId, BookWithAuthorDTO::getAuthorId)
                .selectAs(AuthorModel::getName, BookWithAuthorDTO::getAuthorName)
                .leftJoin(AuthorModel.class, AuthorModel::getId, BookModel::getAuthorId)
                .eq(BookModel::getId, id);
        return mapper.selectJoinOne(BookWithAuthorDTO.class, wrapper);
    }


}
