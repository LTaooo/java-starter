package com.lt.springstarter.controller;

import com.lt.springstarter.queue.demo.DemoProducer;
import com.lt.springstarter.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/demo")
@RestController
public class DemoController {
    private final DemoProducer demoProducer;

    public DemoController(DemoProducer demoProducer) {
        this.demoProducer = demoProducer;
    }

    @GetMapping("/")
    @Operation(summary = "hello")
    public ApiResponse<String> hello() {
        demoProducer.sendMessage(new DemoProducer.DemoMessage("hello"));
        return ApiResponse.success("hello");
    }
}
