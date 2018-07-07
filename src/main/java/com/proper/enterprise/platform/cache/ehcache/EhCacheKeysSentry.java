package com.proper.enterprise.platform.cache.ehcache;

import com.proper.enterprise.platform.api.cache.CacheKeysSentry;
import net.sf.ehcache.Ehcache;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class EhCacheKeysSentry implements CacheKeysSentry {

    @Override
    public Collection<Object> keySet(Cache cache) {
        Ehcache ehcache = (Ehcache) cache.getNativeCache();
        // noinspection unchecked
        return ehcache.getKeys();
    }

}
