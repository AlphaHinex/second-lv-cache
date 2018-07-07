/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

package com.proper.enterprise.platform.cache.ehcache;

import com.proper.enterprise.platform.api.cache.CacheDuration;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.ehcache.EhCacheMessageLogger;
import org.hibernate.cache.ehcache.internal.util.HibernateEhcacheUtils;
import org.jboss.logging.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A non-singleton EhCacheRegionFactory implementation.
 *
 * @author Chris Dennis
 * @author Greg Luck
 * @author Emmanuel Bernard
 * @author Abhishek Sanoujam
 * @author Alex Snaps
 *
 * Copy from {@link org.hibernate.cache.ehcache.EhCacheRegionFactory} v5.2.17.Final
 * Extend {@link EhCacheRegionFactory#start(SessionFactoryOptions, Properties)} to support {@link CacheDuration}
 */
public class EhCacheRegionFactory extends AbstractEhcacheRegionFactory {
    //CHECKSTYLE:OFF

    private static final EhCacheMessageLogger LOG = Logger.getMessageLogger(
        EhCacheMessageLogger.class,
        EhCacheRegionFactory.class.getName()
    );

    /**
     * Creates a non-singleton EhCacheRegionFactory
     */
    @SuppressWarnings("UnusedDeclaration")
    public EhCacheRegionFactory() {
    }

    /**
     * Creates a non-singleton EhCacheRegionFactory
     *
     * @param prop Not used
     */
    @SuppressWarnings("UnusedDeclaration")
    public EhCacheRegionFactory(Properties prop) {
        super();
    }

    @Override
    public void start(SessionFactoryOptions settings, Properties properties) throws CacheException {
        this.settings = settings;
        if ( manager != null ) {
            LOG.attemptToRestartAlreadyStartedEhCacheProvider();
            return;
        }

        try {
            String configurationResourceName = null;
            if ( properties != null ) {
                configurationResourceName = (String) properties.get( NET_SF_EHCACHE_CONFIGURATION_RESOURCE_NAME );
            }
            if ( configurationResourceName == null || configurationResourceName.length() == 0 ) {
                final Configuration configuration = ConfigurationFactory.parseConfiguration();
                supplementConfigurationWithCacheDuration(configuration);
                manager = new CacheManager( configuration );
            }
            else {
                final URL url = loadResource( configurationResourceName );
                final Configuration configuration = HibernateEhcacheUtils.loadAndCorrectConfiguration( url );
                supplementConfigurationWithCacheDuration(configuration);
                manager = new CacheManager( configuration );
            }
            mbeanRegistrationHelper.registerMBean( manager, properties );
        }
        catch (net.sf.ehcache.CacheException e) {
            if ( e.getMessage().startsWith(
                "Cannot parseConfiguration CacheManager. Attempt to create a new instance of " +
                    "CacheManager using the diskStorePath"
            ) ) {
                throw new CacheException(
                    "Attempt to restart an already started EhCacheRegionFactory. " +
                        "Use sessionFactory.close() between repeated calls to buildSessionFactory. " +
                        "Consider using SingletonEhCacheRegionFactory. Error from ehcache was: " + e.getMessage()
                );
            }
            else {
                throw new CacheException( e );
            }
        }
    }

    @Override
    public void stop() {
        try {
            if ( manager != null ) {
                mbeanRegistrationHelper.unregisterMBean();
                manager.shutdown();
                manager = null;
            }
        }
        catch (net.sf.ehcache.CacheException e) {
            throw new CacheException( e );
        }
    }

    //CHECKSTYLE:ON

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EhCacheRegionFactory.class);

    private void supplementConfigurationWithCacheDuration(Configuration configuration) {
        CacheDuration cd;
        String cacheName;
        CacheConfiguration config;

        Set<Class<?>> cdTypes = new Reflections("com.proper").getTypesAnnotatedWith(CacheDuration.class);
        Set<Method> cdMethods = new Reflections("com.proper", new MethodAnnotationsScanner()).getMethodsAnnotatedWith(CacheDuration.class);

        Map<String, CacheDuration> cds = new HashMap<>(cdTypes.size() + cdMethods.size());
        for (Class clz : cdTypes) {
            cd = (CacheDuration) clz.getAnnotation(CacheDuration.class);
            if (cd == null) {
                LOGGER.debug("Could NOT find CacheDuration on {}", clz.getCanonicalName());
                continue;
            }
            cacheName = StringUtils.hasText(cd.cacheName()) ? cd.cacheName() : clz.getCanonicalName();
            cds.put(cacheName, cd);
        }
        // Method annotation has higher priority with same cache name
        String canonicalName;
        for (Method method : cdMethods) {
            cd = method.getAnnotation(CacheDuration.class);
            canonicalName = method.getDeclaringClass().getCanonicalName() + "#" + method.getName();
            if (cd == null) {
                LOGGER.debug("Could NOT find CacheDuration on {}", canonicalName);
                continue;
            }
            cacheName = StringUtils.hasText(cd.cacheName()) ? cd.cacheName() : canonicalName;
            cds.put(cacheName, cd);
        }
        long tti;
        long ttl;
        for (Map.Entry<String, CacheDuration> entry : cds.entrySet()) {
            cacheName = entry.getKey();
            config = new CacheConfiguration(cacheName, 10000);
            config.setName(cacheName);
            tti = entry.getValue().maxIdleTime() / 1000;
            ttl = entry.getValue().ttl() / 1000;
            config.setTimeToIdleSeconds(tti);
            config.setTimeToLiveSeconds(ttl);
            // 不能将 copyOnRead 设置为 true，否则会影响缓存的 TTI，详见 ExpireTest#testTTI
            config.setCopyOnWrite(true);
            LOGGER.debug("Load {} with {}s ttl and {}s max idle time.", cacheName, ttl, tti);
            configuration.addCache(config);
        }
    }

}
