package no.nav.klage.oppgave.service

import org.testcontainers.elasticsearch.ElasticsearchContainer

class TestElasticsearchContainer private constructor() :
    ElasticsearchContainer(IMAGE_VERSION) {

    companion object {
        private const val IMAGE_VERSION = "docker.elastic.co/elasticsearch/elasticsearch:7.9.3"

        private val CONTAINER: TestElasticsearchContainer = TestElasticsearchContainer()

        val instance: TestElasticsearchContainer
            get() {
                return CONTAINER
            }
    }

    override fun start() {
        super.start()
        System.setProperty("AIVEN_ES_HOST", CONTAINER.host)
        System.setProperty("AIVEN_ES_PORT", CONTAINER.firstMappedPort.toString())
        System.setProperty("ELASTIC_USERNAME", "elastic")
        System.setProperty("ELASTIC_PASSWORD", "changeme")
        System.setProperty("AIVEN_ES_SCHEME", "http")
        System.setProperty("AIVEN_ES_USE_SSL", "false")
    }

    override fun stop() {
        //do nothing, JVM handles shut down
    }


}