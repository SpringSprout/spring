package com.spring.sprout.dummy.scan;

import com.spring.sprout.annotation.Autowired;
import com.spring.sprout.annotation.Component;

@Component
public class ScanService {

    private final ScanRepository repository;

    @Autowired
    public ScanService(ScanRepository repository) {
        this.repository = repository;
    }

    public ScanRepository getRepository() {
        return repository;
    }
}
