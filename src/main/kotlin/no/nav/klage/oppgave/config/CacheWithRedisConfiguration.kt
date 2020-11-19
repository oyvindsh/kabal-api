package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.util.getLogger
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.CachingConfigurerSupport
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration
import java.util.*


@EnableCaching
@EnableConfigurationProperties(CacheConfigurationProperties::class)
@Configuration
class CacheWithRedisConfiguration : CachingConfigurerSupport() {
//This code is taken from the tutorial at https://programmerfriend.com/ultimate-guide-to-redis-cache-with-spring-boot-2-and-spring-data-redis/

    companion object {

        const val ENHET_CACHE = "enhet"
        const val TILGANGER_CACHE = "tilganger"
        const val ROLLER_CACHE = "roller"
        const val SAKSBEHANDLERE_I_ENHET_CACHE = "saksbehandlereienhet"

        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        private fun createCacheConfiguration(timeoutInSeconds: Long): RedisCacheConfiguration {
            return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(timeoutInSeconds))
        }
    }

    @Bean
    fun redisConnectionFactory(properties: CacheConfigurationProperties): LettuceConnectionFactory {
        logger.info(
            "Redis (/Lettuce) configuration enabled. With cache timeout " + properties.timeoutSeconds
                .toString() + " seconds."
        )
        val redisStandaloneConfiguration = RedisStandaloneConfiguration()
        redisStandaloneConfiguration.hostName = properties.redisHost
        redisStandaloneConfiguration.port = properties.redisPort
        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }

    @Bean
    fun redisTemplate(cf: RedisConnectionFactory?): RedisTemplate<String, String> {
        val redisTemplate: RedisTemplate<String, String> = RedisTemplate<String, String>()
        redisTemplate.connectionFactory = cf
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
        return redisTemplate
    }

    @Bean
    fun cacheConfiguration(properties: CacheConfigurationProperties): RedisCacheConfiguration {
        return createCacheConfiguration(properties.timeoutSeconds)
    }

    @Bean
    fun cacheManager(
        redisConnectionFactory: RedisConnectionFactory,
        properties: CacheConfigurationProperties
    ): RedisCacheManager {
        val cacheConfigurations: MutableMap<String, RedisCacheConfiguration> =
            HashMap<String, RedisCacheConfiguration>()
        for ((key, value) in properties.cacheExpirations.entries) {
            cacheConfigurations[key] = createCacheConfiguration(value)
        }
        return RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(cacheConfiguration(properties))
            .withInitialCacheConfigurations(cacheConfigurations).build()
    }
}

@ConfigurationProperties(prefix = "cache")
data class CacheConfigurationProperties(
    var timeoutSeconds: Long = 60,
    var redisPort: Int = 6379,
    var redisHost: String = "localhost",
    // Mapping of cacheNames to expira-after-write timeout in seconds
    var cacheExpirations: Map<String, Long> = HashMap()
)