package dev.haja.springtemplatesimplejava.repository;

import org.springframework.stereotype.Repository;

@Repository
public class SampleRepository {

    public String findMessage() {
        return "Hello from Repository";
    }
}
