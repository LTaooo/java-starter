package com.lt.springstarter.service;

import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.lt.springstarter.entity.vo.BookVO;
import com.lt.springstarter.mapper.BookMapper;
import com.lt.springstarter.model.AuthorModel;
import com.lt.springstarter.model.BookModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookService {
    private final BookMapper bookMapper;

    public BookModel createBook(String name) {
        BookModel bookModel = new BookModel();
        bookModel.setName(name);
        int id = bookMapper.insert(bookModel);
        bookModel.setId(id);
        return bookModel;
    }

    public BookVO getBook(Integer id) {
        MPJLambdaWrapper<BookModel> wrapper = JoinWrappers.lambda(BookModel.class)
                .selectAll(BookModel.class)
                .selectAs(AuthorModel::getId, BookVO::getAuthorId)
                .selectAs(AuthorModel::getName, BookVO::getAuthorName)
                .leftJoin(AuthorModel.class, AuthorModel::getId, BookModel::getAuthorId)
                .eq(BookModel::getId, id);
        return bookMapper.selectJoinOne(BookVO.class, wrapper);
    }
}
