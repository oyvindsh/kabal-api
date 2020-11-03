package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.api.OppgaveController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.Tag
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import java.time.LocalDate

@Configuration
class OpenApiConfig {

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.OAS_30)
            .select()
            .apis(RequestHandlerSelectors.basePackage(OppgaveController::class.java.packageName))
            .build()
            .pathMapping("/")
            .directModelSubstitute(LocalDate::class.java, String::class.java)
            .genericModelSubstitutes(ResponseEntity::class.java)
            .enableUrlTemplating(true)
            .tags(Tag("klage-oppgave-api", "oppgave-api for saksbehandlere ved klageinstansen"))
    }

}