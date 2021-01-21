package no.nav.klage.oppgave.config

import org.springframework.cache.CacheManager
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent

@Configuration
class CacheInvalidationConfiguration(val cacheManager: CacheManager) :
    ApplicationListener<ContextRefreshedEvent> {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
    }
}