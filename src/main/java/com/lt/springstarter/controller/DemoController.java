package com.lt.springstarter.controller;

import com.lt.springstarter.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/demo")
@RestController
public class DemoController {
    @GetMapping("/")
    @Operation(summary = "hello")
    public ApiResponse<String> hello() {
        return ApiResponse.success("hello");
    }
}
