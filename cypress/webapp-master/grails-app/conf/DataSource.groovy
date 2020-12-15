hibernate {
    cache {
        use_second_level_cache = true
        use_query_cache = true
        provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
        region {
            factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory'
        }
    }
    session_factory_name_is_jndi = false
    session_factory_name = 'definedForAvoidBoooom'
}