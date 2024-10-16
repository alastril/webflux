package com.hibernate.controllers;

import com.hibernate.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class BaseController {
    @Autowired
     MessageRepository repository;
    @GetMapping
    public String sayHello() {
        repository.findAll();
        return "Hello";
    }
}
