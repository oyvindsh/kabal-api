package no.nav.klage.oppgave.api

import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FeatureToggleController::class)
@ActiveProfiles("local")
class FeatureToggleControllerTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var unleash: Unleash

    @MockBean
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

    @BeforeEach
    fun setup() {
        `when`(innloggetSaksbehandlerRepository.getInnloggetIdent()).thenReturn("H149390")
        `when`(unleash.isEnabled(anyString(), any<UnleashContext>())).thenReturn(true)
    }

    @Test
    fun `open toggle should return true`() {
        this.mockMvc.perform(get("/aapenfeaturetoggle/testing")).andDo(print()).andExpect(status().isOk())
            .andExpect(content().string(containsString("true")))
    }

    @Test
    @Disabled("Authentication is not turned on..")
    fun `protected toggle should throw exception`() {
        this.mockMvc.perform(get("/featuretoggle/testing")).andDo(print()).andExpect(status().is4xxClientError())
    }
}