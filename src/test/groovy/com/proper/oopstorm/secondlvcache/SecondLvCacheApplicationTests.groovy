package com.proper.oopstorm.secondlvcache

import com.proper.enterprise.platform.test.AbstractTest
import com.proper.oopstorm.secondlvcache.entity.AnEntity
import com.proper.oopstorm.secondlvcache.repository.AnRepository
import org.hibernate.Session
import org.hibernate.stat.Statistics
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager

@Transactional(transactionManager = "jpaTransactionManager")
@ContextConfiguration("/spring/applicationContext.xml")
@WebAppConfiguration
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SqlConfig(encoding = "UTF-8")
class SecondLvCacheApplicationTests {

    @Autowired
    private AnRepository repo

    @Autowired
    private EntityManager entityManager

    @Test
    void checkQueryCache() {
        Session session = (Session) entityManager.getDelegate()
        Statistics statistics = session.getSessionFactory().getStatistics()
        statistics.setStatisticsEnabled(true)

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

        statistics.setStatisticsEnabled(false)
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
