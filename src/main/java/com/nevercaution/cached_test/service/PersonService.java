package com.nevercaution.cached_test.service;

import com.nevercaution.cached_test.annotation.RedisCached;
import com.nevercaution.cached_test.annotation.RedisCachedKeyParam;
import com.nevercaution.cached_test.model.Person;
import org.springframework.stereotype.Service;


@Service
public class PersonService {

    @RedisCached(key = "person", expire = 300)
    public Person getPerson(@RedisCachedKeyParam(key = "name") String name) {
        // do make cache jobs
        Person person = new Person(name, 10);
        System.out.println("person = " + person);
        return person;
    }
}
