package dev.haja.springtemplatesimplejava.service;

import dev.haja.springtemplatesimplejava.repository.SampleRepository;
import org.springframework.stereotype.Service;

@Service
public class SampleService {

    private final SampleRepository sampleRepository;

    public SampleService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    public String getMessage() {
        return sampleRepository.findMessage();
    }
}
