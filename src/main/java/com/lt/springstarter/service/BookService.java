package com.lt.springstarter.service;

import com.lt.springstarter.entity.dto.BookWithAuthorDTO;
import com.lt.springstarter.entity.vo.BookVO;
import com.lt.springstarter.model.BookModel;
import com.lt.springstarter.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookModel createBook(String name) {
        BookModel bookModel = new BookModel();
        bookModel.setName(name);
        bookRepository.insert(bookModel);
        return bookModel;
    }

    public @Nullable BookVO getBook(Integer id) {
        BookWithAuthorDTO bookWithAuthorDTO = bookRepository.getBookWithAuthor(id);
        if (bookWithAuthorDTO == null) {
            return null;
        }
        return BookVO.from(bookWithAuthorDTO);
    }
}
