package no.nav.klage.dokument.api.controller

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import no.nav.klage.dokument.api.mapper.DokumentInputMapper
import no.nav.klage.dokument.api.mapper.DokumentMapper
import no.nav.klage.dokument.api.view.HovedDokumentInput
import no.nav.klage.dokument.api.view.HovedDokumentView
import no.nav.klage.dokument.api.view.SmartHovedDokumentInput
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentType
import no.nav.klage.dokument.domain.dokumenterunderarbeid.HovedDokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.PersistentDokumentId
import no.nav.klage.dokument.service.DokumentService
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(DokumentUnderArbeidController::class)
@ActiveProfiles("local")
internal class DokumentUnderArbeidControllerTest {

    @MockkBean
    private lateinit var innloggetSaksbehandlerService: InnloggetSaksbehandlerRepository

    @MockkBean
    private lateinit var dokumentService: DokumentService

    @SpykBean
    private lateinit var dokumentMapper: DokumentMapper

    @SpykBean
    private lateinit var dokumentInputMapper: DokumentInputMapper

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    @Test
    fun createAndUploadHoveddokument() {

        val hovedDokumentInput = HovedDokumentInput(eksternReferanse = UUID.randomUUID())

        every { innloggetSaksbehandlerService.getInnloggetIdent() } returns "IDENT"
        every {
            dokumentService.opprettOgMellomlagreNyttHoveddokument(
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns HovedDokument(
            persistentDokumentId = PersistentDokumentId(UUID.randomUUID()),
            mellomlagerId = "mellomlagerId",
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "vedtak.pdf",
            behandlingId = hovedDokumentInput.eksternReferanse,
            smartEditorId = null,
            dokumentType = DokumentType.BREV,
        )


        val file =
            MockMultipartFile("file", "file-name.pdf", "application/pdf", "whatever".toByteArray())

        val json = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/dokumenter/hoveddokumenter/fil")
                .file(file)
                .content(objectMapper.writeValueAsString(hovedDokumentInput))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsString

        val hovedDokumentView = objectMapper.readValue(json, HovedDokumentView::class.java)
        assertThat(hovedDokumentView).isNotNull
        assertThat(hovedDokumentView.dokumentTypeId).isEqualTo(DokumentType.BREV.id)
    }

    @Test
    fun createSmartEditorHoveddokument() {

        val smartHovedDokumentInput =
            SmartHovedDokumentInput(eksternReferanse = UUID.randomUUID(), "{ \"json\": \"is cool\" }")

        every { innloggetSaksbehandlerService.getInnloggetIdent() } returns "IDENT"
        every {
            dokumentService.opprettOgMellomlagreNyttHoveddokument(
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns HovedDokument(
            persistentDokumentId = PersistentDokumentId(UUID.randomUUID()),
            mellomlagerId = "mellomlagerId",
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "vedtak.pdf",
            behandlingId = smartHovedDokumentInput.eksternReferanse,
            smartEditorId = UUID.randomUUID(),
            dokumentType = DokumentType.BREV,
        )


        val json = mockMvc.perform(
            MockMvcRequestBuilders.post("/dokumenter/hoveddokumenter/smart")
                .content(objectMapper.writeValueAsString(smartHovedDokumentInput))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsString

        val hovedDokumentView = objectMapper.readValue(json, HovedDokumentView::class.java)
        assertThat(hovedDokumentView).isNotNull
        assertThat(hovedDokumentView.dokumentTypeId).isEqualTo(DokumentType.BREV.id)
    }
}