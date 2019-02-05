package com.nevercaution.cached_test.service;

import com.nevercaution.cached_test.config.RedisDB;
import com.nevercaution.cached_test.model.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonServiceTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private RedisDB redisDB;

    @Test
    public void cacheTest() {
        final String name = "teddy";
        final String cacheKey = "person/getPerson/name=" + name;

        // delete if exists by redis
        redisDB.del(cacheKey);

        // redis db is empty
        assertThat(redisDB.get(cacheKey, Person.class)).isNull();

        // make cache
        Person person = personService.getPerson(name);

        // redis get cached data
        assertThat(redisDB.get(cacheKey, Person.class)).isEqualTo(personService.getPerson(name));
        assertThat(redisDB.get(cacheKey, Person.class)).isEqualTo(person);
    }
}
