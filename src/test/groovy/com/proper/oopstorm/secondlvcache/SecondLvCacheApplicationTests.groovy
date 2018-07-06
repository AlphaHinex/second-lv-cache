package com.proper.oopstorm.secondlvcache

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner)
@SpringBootTest
class SecondLvCacheApplicationTests {

	@Autowired
	AnRepository repository

    @Test
    void abc() {
        println repository.findAll()
        repository.save(new AnEntity('abc', 'pwd'))
        assert repository.findAll().size() == 0
    }

}
