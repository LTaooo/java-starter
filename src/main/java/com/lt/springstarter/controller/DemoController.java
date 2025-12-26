package com.lt.springstarter.controller;

import com.lt.springstarter.entity.vo.BookVO;
import com.lt.springstarter.model.BookModel;
import com.lt.springstarter.queue.demo.DemoProducer;
import com.lt.springstarter.service.BookService;
import com.lt.springstarter.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RequestMapping("/demo")
@RestController
@Tag(name = "demo")
@RequiredArgsConstructor
public class DemoController {
    private final DemoProducer demoProducer;

    private final BookService bookService;

    @GetMapping("/")
    @Operation(summary = "hello")
    public ApiResponse<String> hello() {
        demoProducer.sendMessage(new DemoProducer.DemoMessage("hello"));
        return ApiResponse.success("hello");
    }

    @GetMapping("/book_create")
    @Operation(summary = "create book")
    public ApiResponse<BookModel> createBook() {
        Random random = new Random();
        String bookName = "book-" + random.nextInt(1000);
        BookModel bookModel = bookService.createBook(bookName);
        return ApiResponse.success(bookModel);
    }

    @GetMapping("/book_get/{id}")
    @Operation(summary = "get book")
    public ApiResponse<BookVO> getBook(@PathVariable Integer id) {
        BookVO bookVO = bookService.getBook(id);
        return ApiResponse.success(bookVO);
    }
}
