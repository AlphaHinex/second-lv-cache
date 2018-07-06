package com.proper.oopstorm.secondlvcache

import org.hibernate.Session
import org.hibernate.stat.Statistics
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

import javax.persistence.EntityManager

@RunWith(SpringRunner)
@SpringBootTest
class SecondLvCacheApplicationTests {

    @Autowired
    private AnRepository repo

    @Autowired
    private EntityManager entityManager

    @Test
    void checkQueryCache() {
        Session session = (Session) entityManager.getDelegate()
        Statistics statistics = session.getSessionFactory().getStatistics()
        statistics.clear()

        repo.save(new AnEntity('abc', '123'))
        AnEntity entity = repo.findByUsername('abc')
        // cache entity after first load
//        assert statistics.queryCachePutCount == 1
//        assert statistics.updateTimestampsCachePutCount == 1
        showCounts(statistics)

        // hit count of cache will be increased after each load operation
        3.times {
            repo.findByUsername('abc')
            showCounts(statistics)
        }

        entity.setDescription('update_account')
        repo.save(entity)

        repo.findByUsername('abc')

        showCounts(statistics)

        // hit count will be reset after update
        showCounts(statistics)
    }

    def showCounts(Statistics statistics) {
        println "Query Cache Put: ${statistics.getQueryCachePutCount()}"
        println "Query Cache Hit: ${statistics.getQueryCacheHitCount()}"
        println "Query Cache Miss: ${statistics.getQueryCacheMissCount()}"

        println "Update Timestamps Cache Put: ${statistics.getUpdateTimestampsCachePutCount()}"
        println "Update Timestamps Cache Hit: ${statistics.getUpdateTimestampsCacheHitCount()}"
        println "Update Timestamps Cache Miss: ${statistics.getUpdateTimestampsCacheMissCount()}"
    }

}
