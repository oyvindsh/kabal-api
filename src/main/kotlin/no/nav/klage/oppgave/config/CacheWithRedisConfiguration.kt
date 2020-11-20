package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.util.getLogger
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@EnableCaching
@Configuration
class CacheWithRedisConfiguration {

    companion object {

        const val ENHET_CACHE = "enhet"
        const val TILGANGER_CACHE = "tilganger"
        const val ROLLER_CACHE = "roller"
        const val SAKSBEHANDLERE_I_ENHET_CACHE = "saksbehandlereienhet"

        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Bean
    fun myRedisCacheManagerBuilderCustomizer(): RedisCacheManagerBuilderCustomizer? {
        return RedisCacheManagerBuilderCustomizer { builder: RedisCacheManagerBuilder ->
            builder.withCacheConfiguration(
                ROLLER_CACHE,
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofHours(1))
            )
        }
    }

    @Bean("redisTemplate")
    fun redisTemplate(cf: RedisConnectionFactory): RedisTemplate<String, String> {
        val redisTemplate: RedisTemplate<String, String> = RedisTemplate<String, String>()
        redisTemplate.connectionFactory = cf
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
        return redisTemplate
    }
}