package com.proper.oopstorm.secondlvcache;

import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.persistence.QueryHint;

public interface AnRepository extends PagingAndSortingRepository<AnEntity, String> {

    /**
     * 通过username 取到缓存的entity
     * @param username username
     * @return AnEntity
     */
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    AnEntity findByUsername(String username);

}
