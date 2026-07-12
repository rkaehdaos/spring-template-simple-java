package dev.haja.springtemplatesimplejava.controller;

import dev.haja.springtemplatesimplejava.service.SampleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping("/sample")
    public String sample() {
        return sampleService.getMessage();
    }
}
