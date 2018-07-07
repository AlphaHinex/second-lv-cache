package com.proper.oopstorm.secondlvcache.repository;

import com.proper.enterprise.platform.core.jpa.annotation.CacheQuery;
import com.proper.oopstorm.secondlvcache.entity.AnEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AnRepository extends PagingAndSortingRepository<AnEntity, String> {

    /**
     * 通过username 取到缓存的entity
     * @param username username
     * @return AnEntity
     */
    @CacheQuery
    AnEntity findByUsername(String username);

}
