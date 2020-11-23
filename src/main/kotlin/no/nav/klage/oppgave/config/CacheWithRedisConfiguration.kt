package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.util.getLogger
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder
import org.springframework.data.redis.cache.RedisCacheWriter
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
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
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        return RedisCacheManager(
            RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
            redisCacheConfiguration(),
        )
    }


    @Bean
    fun redisCacheConfiguration(): RedisCacheConfiguration {
        /*
        val jackson2JsonRedisSerializer = Jackson2JsonRedisSerializer(
            Any::class.java
        )
        var redisCacheConfiguration = RedisCacheConfiguration
            .defaultCacheConfig()
        redisCacheConfiguration = redisCacheConfiguration.serializeValuesWith(
            RedisSerializationContext.SerializationPair
                .fromSerializer(jackson2JsonRedisSerializer)
        )
        return redisCacheConfiguration
         */
        return RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(
            RedisSerializationContext
                .SerializationPair
                .fromSerializer(RedisSerializer.json())
        )
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

    /*
    @Bean("redisTemplate")
    fun redisTemplate(cf: RedisConnectionFactory): RedisTemplate<Any, Any> {
        logger.info("Creating redisTemplate with GenericJackson2JsonRedisSerializer")
        val redisTemplate: RedisTemplate<Any, Any> = RedisTemplate()
        redisTemplate.connectionFactory = cf
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
        return redisTemplate
    }

    @Bean
    fun stringRedisTemplate(cf: RedisConnectionFactory): StringRedisTemplate {
        val redisTemplate = StringRedisTemplate();
        redisTemplate.connectionFactory = cf
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
        return redisTemplate
    }
     */
}
