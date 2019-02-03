package com.nevercaution.cached_test.controller;

import com.nevercaution.cached_test.model.Person;
import com.nevercaution.cached_test.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PersonController {

    @Autowired
    private PersonService personService;

    @GetMapping("/person/{name}")
    public ResponseEntity getPerson(@PathVariable(value = "name") String name) {

        Person person = personService.getPerson(name);

        System.out.println("getPerson = " + person);

        return ResponseEntity.ok().body(person);
    }
}
