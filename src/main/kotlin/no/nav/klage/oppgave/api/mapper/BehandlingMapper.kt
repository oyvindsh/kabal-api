package no.nav.klage.oppgave.api.mapper


import no.nav.klage.kodeverk.Type
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.ereg.EregClient
import no.nav.klage.oppgave.clients.kabaldocument.KabalDocumentGateway
import no.nav.klage.oppgave.clients.norg2.Norg2Client
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service

@Service
class BehandlingMapper(
    private val pdlFacade: PdlFacade,
    private val egenAnsattService: EgenAnsattService,
    private val norg2Client: Norg2Client,
    private val eregClient: EregClient,
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val kabalDocumentGateway: KabalDocumentGateway
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun mapBehandlingToBehandlingDetaljerView(behandling: Behandling): BehandlingDetaljerView {
        return when (behandling.type) {
            Type.KLAGE -> mapKlagebehandlingToBehandlingDetaljerView(behandling as Klagebehandling)
            Type.ANKE -> mapAnkebehandlingToBehandlingDetaljerView(behandling as Ankebehandling)
            Type.ANKE_I_TRYGDERETTEN -> mapAnkeITrygderettenbehandlingToBehandlingDetaljerView(behandling as AnkeITrygderettenbehandling)
        }
    }

    fun mapKlagebehandlingToBehandlingDetaljerView(klagebehandling: Klagebehandling): BehandlingDetaljerView {
        val enhetNavn = klagebehandling.avsenderEnhetFoersteinstans.let { norg2Client.fetchEnhet(it) }.navn

        return BehandlingDetaljerView(
            id = klagebehandling.id,
            fraNAVEnhet = klagebehandling.avsenderEnhetFoersteinstans,
            fraNAVEnhetNavn = enhetNavn,
            fraSaksbehandlerident = klagebehandling.avsenderSaksbehandleridentFoersteinstans,
            mottattVedtaksinstans = klagebehandling.mottattVedtaksinstans,
            sakenGjelder = getSakenGjelderView(klagebehandling.sakenGjelder),
            klager = getKlagerView(klagebehandling.klager),
            prosessfullmektig = klagebehandling.klager.prosessfullmektig?.let { getProsessfullmektigView(it) },
            tema = klagebehandling.ytelse.toTema().id,
            temaId = klagebehandling.ytelse.toTema().id,
            ytelse = klagebehandling.ytelse.id,
            ytelseId = klagebehandling.ytelse.id,
            type = klagebehandling.type.id,
            typeId = klagebehandling.type.id,
            mottatt = klagebehandling.mottattKlageinstans.toLocalDate(),
            mottattKlageinstans = klagebehandling.mottattKlageinstans.toLocalDate(),
            tildelt = klagebehandling.tildeling?.tidspunkt?.toLocalDate(),
            avsluttetAvSaksbehandlerDate = klagebehandling.currentDelbehandling().avsluttetAvSaksbehandler?.toLocalDate(),
            isAvsluttetAvSaksbehandler = klagebehandling.currentDelbehandling().avsluttetAvSaksbehandler != null,
            frist = klagebehandling.frist,
            tildeltSaksbehandlerident = klagebehandling.tildeling?.saksbehandlerident,
            tildeltSaksbehandler = getSaksbehandlerView(klagebehandling.tildeling?.saksbehandlerident),
            tildeltSaksbehandlerEnhet = klagebehandling.tildeling?.enhet,
            medunderskriverident = klagebehandling.currentDelbehandling().medunderskriver?.saksbehandlerident,
            medunderskriver = getSaksbehandlerView(klagebehandling.currentDelbehandling().medunderskriver?.saksbehandlerident),
            medunderskriverFlyt = klagebehandling.currentDelbehandling().medunderskriverFlyt,
            datoSendtMedunderskriver = klagebehandling.currentDelbehandling().medunderskriver?.tidspunkt?.toLocalDate(),
            hjemler = klagebehandling.hjemler.map { it.id },
            hjemmelIdList = klagebehandling.hjemler.map { it.id },
            modified = klagebehandling.modified,
            created = klagebehandling.created,
            resultat = klagebehandling.currentDelbehandling().mapToVedtakView(),
            kommentarFraVedtaksinstans = klagebehandling.kommentarFraFoersteinstans,
            tilknyttedeDokumenter = klagebehandling.saksdokumenter.map {
                TilknyttetDokument(
                    journalpostId = it.journalpostId,
                    dokumentInfoId = it.dokumentInfoId
                )
            }.toSet(),
            egenAnsatt = klagebehandling.sakenGjelder.erEgenAnsatt(),
            fortrolig = klagebehandling.sakenGjelder.harBeskyttelsesbehovFortrolig(),
            strengtFortrolig = klagebehandling.sakenGjelder.harBeskyttelsesbehovStrengtFortrolig(),
            vergemaalEllerFremtidsfullmakt = klagebehandling.sakenGjelder.harVergemaalEllerFremtidsfullmakt(),
            kvalitetsvurderingId = klagebehandling.kakaKvalitetsvurderingId,
            kvalitetsvurderingReference = BehandlingDetaljerView.KvalitetsvurderingReference(
                id = klagebehandling.kakaKvalitetsvurderingId,
                version = klagebehandling.kakaKvalitetsvurderingVersion,
            ),
            sattPaaVent = klagebehandling.sattPaaVent,
            feilregistrering = klagebehandling.feilregistrering.toView()
        )
    }

    fun mapAnkebehandlingToBehandlingDetaljerView(ankebehandling: Ankebehandling): BehandlingDetaljerView {
        val forrigeEnhetNavn = ankebehandling.klageBehandlendeEnhet.let { norg2Client.fetchEnhet(it) }.navn

        return BehandlingDetaljerView(
            id = ankebehandling.id,
            fraNAVEnhet = ankebehandling.klageBehandlendeEnhet,
            fraNAVEnhetNavn = forrigeEnhetNavn,
            mottattVedtaksinstans = null,
            sakenGjelder = getSakenGjelderView(ankebehandling.sakenGjelder),
            klager = getKlagerView(ankebehandling.klager),
            prosessfullmektig = ankebehandling.klager.prosessfullmektig?.let { getProsessfullmektigView(it) },
            tema = ankebehandling.ytelse.toTema().id,
            temaId = ankebehandling.ytelse.toTema().id,
            ytelse = ankebehandling.ytelse.id,
            ytelseId = ankebehandling.ytelse.id,
            type = ankebehandling.type.id,
            typeId = ankebehandling.type.id,
            mottatt = ankebehandling.mottattKlageinstans.toLocalDate(),
            mottattKlageinstans = ankebehandling.mottattKlageinstans.toLocalDate(),
            tildelt = ankebehandling.tildeling?.tidspunkt?.toLocalDate(),
            avsluttetAvSaksbehandlerDate = ankebehandling.currentDelbehandling().avsluttetAvSaksbehandler?.toLocalDate(),
            isAvsluttetAvSaksbehandler = ankebehandling.currentDelbehandling().avsluttetAvSaksbehandler != null,
            frist = ankebehandling.frist,
            tildeltSaksbehandlerident = ankebehandling.tildeling?.saksbehandlerident,
            tildeltSaksbehandler = getSaksbehandlerView(ankebehandling.tildeling?.saksbehandlerident),
            tildeltSaksbehandlerEnhet = ankebehandling.tildeling?.enhet,
            medunderskriverident = ankebehandling.currentDelbehandling().medunderskriver?.saksbehandlerident,
            medunderskriver = getSaksbehandlerView(ankebehandling.currentDelbehandling().medunderskriver?.saksbehandlerident),
            medunderskriverFlyt = ankebehandling.currentDelbehandling().medunderskriverFlyt,
            datoSendtMedunderskriver = ankebehandling.currentDelbehandling().medunderskriver?.tidspunkt?.toLocalDate(),
            hjemler = ankebehandling.hjemler.map { it.id },
            hjemmelIdList = ankebehandling.hjemler.map { it.id },
            modified = ankebehandling.modified,
            created = ankebehandling.created,
            fraSaksbehandlerident = null,
            resultat = ankebehandling.currentDelbehandling().mapToVedtakView(),
            kommentarFraVedtaksinstans = null,
            tilknyttedeDokumenter = ankebehandling.saksdokumenter.map {
                TilknyttetDokument(
                    journalpostId = it.journalpostId,
                    dokumentInfoId = it.dokumentInfoId
                )
            }.toSet(),
            egenAnsatt = ankebehandling.sakenGjelder.erEgenAnsatt(),
            fortrolig = ankebehandling.sakenGjelder.harBeskyttelsesbehovFortrolig(),
            strengtFortrolig = ankebehandling.sakenGjelder.harBeskyttelsesbehovStrengtFortrolig(),
            vergemaalEllerFremtidsfullmakt = ankebehandling.sakenGjelder.harVergemaalEllerFremtidsfullmakt(),
            kvalitetsvurderingId = ankebehandling.kakaKvalitetsvurderingId,
            kvalitetsvurderingReference = BehandlingDetaljerView.KvalitetsvurderingReference(
                id = ankebehandling.kakaKvalitetsvurderingId,
                version = ankebehandling.kakaKvalitetsvurderingVersion,
            ),
            sattPaaVent = ankebehandling.sattPaaVent,
            feilregistrering = ankebehandling.feilregistrering.toView(),
        )
    }

    fun mapAnkeITrygderettenbehandlingToBehandlingDetaljerView(ankeITrygderettenbehandling: AnkeITrygderettenbehandling): BehandlingDetaljerView {
        return BehandlingDetaljerView(
            id = ankeITrygderettenbehandling.id,
            fraNAVEnhet = null,
            fraNAVEnhetNavn = null,
            mottattVedtaksinstans = null,
            sakenGjelder = getSakenGjelderView(ankeITrygderettenbehandling.sakenGjelder),
            klager = getKlagerView(ankeITrygderettenbehandling.klager),
            prosessfullmektig = ankeITrygderettenbehandling.klager.prosessfullmektig?.let { getProsessfullmektigView(it) },
            tema = ankeITrygderettenbehandling.ytelse.toTema().id,
            temaId = ankeITrygderettenbehandling.ytelse.toTema().id,
            ytelse = ankeITrygderettenbehandling.ytelse.id,
            ytelseId = ankeITrygderettenbehandling.ytelse.id,
            type = ankeITrygderettenbehandling.type.id,
            typeId = ankeITrygderettenbehandling.type.id,
            mottatt = ankeITrygderettenbehandling.mottattKlageinstans.toLocalDate(),
            mottattKlageinstans = ankeITrygderettenbehandling.mottattKlageinstans.toLocalDate(),
            tildelt = ankeITrygderettenbehandling.tildeling?.tidspunkt?.toLocalDate(),
            avsluttetAvSaksbehandlerDate = ankeITrygderettenbehandling.currentDelbehandling().avsluttetAvSaksbehandler?.toLocalDate(),
            isAvsluttetAvSaksbehandler = ankeITrygderettenbehandling.currentDelbehandling().avsluttetAvSaksbehandler != null,
            frist = ankeITrygderettenbehandling.frist,
            tildeltSaksbehandlerident = ankeITrygderettenbehandling.tildeling?.saksbehandlerident,
            tildeltSaksbehandler = getSaksbehandlerView(ankeITrygderettenbehandling.tildeling?.saksbehandlerident),
            tildeltSaksbehandlerEnhet = ankeITrygderettenbehandling.tildeling?.enhet,
            medunderskriverident = ankeITrygderettenbehandling.currentDelbehandling().medunderskriver?.saksbehandlerident,
            medunderskriver = getSaksbehandlerView(ankeITrygderettenbehandling.currentDelbehandling().medunderskriver?.saksbehandlerident),
            medunderskriverFlyt = ankeITrygderettenbehandling.currentDelbehandling().medunderskriverFlyt,
            datoSendtMedunderskriver = ankeITrygderettenbehandling.currentDelbehandling().medunderskriver?.tidspunkt?.toLocalDate(),
            hjemler = ankeITrygderettenbehandling.hjemler.map { it.id },
            hjemmelIdList = ankeITrygderettenbehandling.hjemler.map { it.id },
            modified = ankeITrygderettenbehandling.modified,
            created = ankeITrygderettenbehandling.created,
            fraSaksbehandlerident = null,
            resultat = ankeITrygderettenbehandling.currentDelbehandling().mapToVedtakView(),
            kommentarFraVedtaksinstans = null,
            tilknyttedeDokumenter = ankeITrygderettenbehandling.saksdokumenter.map {
                TilknyttetDokument(
                    journalpostId = it.journalpostId,
                    dokumentInfoId = it.dokumentInfoId
                )
            }.toSet(),
            egenAnsatt = ankeITrygderettenbehandling.sakenGjelder.erEgenAnsatt(),
            fortrolig = ankeITrygderettenbehandling.sakenGjelder.harBeskyttelsesbehovFortrolig(),
            strengtFortrolig = ankeITrygderettenbehandling.sakenGjelder.harBeskyttelsesbehovStrengtFortrolig(),
            vergemaalEllerFremtidsfullmakt = ankeITrygderettenbehandling.sakenGjelder.harVergemaalEllerFremtidsfullmakt(),
            kvalitetsvurderingReference = BehandlingDetaljerView.KvalitetsvurderingReference(
                id = null,
                version = 2,
            ),
            sattPaaVent = ankeITrygderettenbehandling.sattPaaVent,
            sendtTilTrygderetten = ankeITrygderettenbehandling.sendtTilTrygderetten,
            kjennelseMottatt = ankeITrygderettenbehandling.kjennelseMottatt,
            feilregistrering = ankeITrygderettenbehandling.feilregistrering.toView()
        )
    }

    private fun getSaksbehandlerView(saksbehandlerident: String?): SaksbehandlerView? {
        return saksbehandlerident?.let {
            SaksbehandlerView(navIdent = it, navn = saksbehandlerRepository.getNameForSaksbehandler(it))
        }
    }

    fun getSakenGjelderView(sakenGjelder: SakenGjelder): BehandlingDetaljerView.SakenGjelderView {
        if (sakenGjelder.erPerson()) {
            val person = pdlFacade.getPersonInfo(sakenGjelder.partId.value)
            return BehandlingDetaljerView.SakenGjelderView(
                person = BehandlingDetaljerView.PersonView(
                    foedselsnummer = person.foedselsnr,
                    navn = person.mapNavnToView(),
                    kjoenn = person.kjoenn,
                ), virksomhet = null
            )
        } else {
            return BehandlingDetaljerView.SakenGjelderView(
                person = null,
                virksomhet = BehandlingDetaljerView.VirksomhetView(
                    virksomhetsnummer = sakenGjelder.partId.value,
                    navn = eregClient.hentOrganisasjon(sakenGjelder.partId.value)?.navn?.sammensattNavn()
                )
            )
        }
    }

    fun getKlagerView(klager: Klager): BehandlingDetaljerView.KlagerView {
        if (klager.erPerson()) {
            val person = pdlFacade.getPersonInfo(klager.partId.value)
            return BehandlingDetaljerView.KlagerView(
                person = BehandlingDetaljerView.PersonView(
                    foedselsnummer = person.foedselsnr,
                    navn = person.mapNavnToView(),
                    kjoenn = person.kjoenn,
                ), virksomhet = null
            )
        } else {
            return BehandlingDetaljerView.KlagerView(
                person = null, virksomhet = BehandlingDetaljerView.VirksomhetView(
                    virksomhetsnummer = klager.partId.value,
                    navn = eregClient.hentOrganisasjon(klager.partId.value)?.navn?.sammensattNavn()
                )
            )
        }
    }

    fun getProsessfullmektigView(prosessfullmektig: Prosessfullmektig): BehandlingDetaljerView.ProsessfullmektigView {
        if (prosessfullmektig.erPerson()) {
            val person = pdlFacade.getPersonInfo(prosessfullmektig.partId.value)
            return BehandlingDetaljerView.ProsessfullmektigView(
                person = BehandlingDetaljerView.PersonView(
                    foedselsnummer = person.foedselsnr,
                    navn = person.mapNavnToView(),
                    kjoenn = person.kjoenn,
                ), virksomhet = null
            )
        } else {
            return BehandlingDetaljerView.ProsessfullmektigView(
                person = null, virksomhet = BehandlingDetaljerView.VirksomhetView(
                    virksomhetsnummer = prosessfullmektig.partId.value,
                    navn = eregClient.hentOrganisasjon(prosessfullmektig.partId.value)?.navn?.sammensattNavn()
                )
            )
        }
    }

    private fun SakenGjelder.harBeskyttelsesbehovFortrolig(): Boolean {
        return if (erVirksomhet()) {
            false
        } else {
            pdlFacade.getPersonInfo(partId.value).harBeskyttelsesbehovFortrolig()
        }
    }

    private fun SakenGjelder.harBeskyttelsesbehovStrengtFortrolig(): Boolean {
        return if (erVirksomhet()) {
            false
        } else {
            pdlFacade.getPersonInfo(partId.value).harBeskyttelsesbehovStrengtFortrolig()
        }
    }

    private fun SakenGjelder.erEgenAnsatt(): Boolean {
        return if (erVirksomhet()) {
            false
        } else {
            egenAnsattService.erEgenAnsatt(partId.value)
        }
    }

    private fun SakenGjelder.harVergemaalEllerFremtidsfullmakt(): Boolean {
        return if (erVirksomhet()) {
            false
        } else {
            pdlFacade.getPersonInfo(partId.value).vergemaalEllerFremtidsfullmakt ?: false
        }
    }

    fun Delbehandling.mapToVedtakView(): VedtakView {
        return VedtakView(
            id = id,
            utfall = utfall?.id,
            utfallId = utfall?.id,
            hjemler = hjemler.map { it.id }.toSet(),
            hjemmelIdSet = hjemler.map { it.id }.toSet(),
        )
    }

    private fun Person?.mapNavnToView(): BehandlingDetaljerView.NavnView? =
        if (this != null) {
            BehandlingDetaljerView.NavnView(
                fornavn = fornavn,
                mellomnavn = mellomnavn,
                etternavn = etternavn
            )
        } else {
            null
        }

    fun mapToBehandlingFullfoertView(behandling: Behandling): BehandlingFullfoertView {
        return BehandlingFullfoertView(
            modified = behandling.modified,
            isAvsluttetAvSaksbehandler = behandling.currentDelbehandling().avsluttetAvSaksbehandler != null
        )
    }

    fun mapToMedunderskriverFlytResponse(behandling: Behandling): MedunderskriverFlytResponse {
        return MedunderskriverFlytResponse(
            navn = if (behandling.medunderskriver?.saksbehandlerident != null) saksbehandlerRepository.getNameForSaksbehandler(
                behandling.medunderskriver?.saksbehandlerident!!
            ) else null,
            navIdent = behandling.medunderskriver?.saksbehandlerident,
            modified = behandling.modified,
            medunderskriverFlyt = behandling.currentDelbehandling().medunderskriverFlyt,
        )
    }

    fun mapToMedunderskriverWrapped(behandling: Behandling): MedunderskriverWrapped {
        return MedunderskriverWrapped(
            medunderskriver = getSaksbehandlerView(behandling.medunderskriver?.saksbehandlerident),
            modified = behandling.modified,
            medunderskriverFlyt = behandling.currentDelbehandling().medunderskriverFlyt,
        )
    }

    fun mapToMedunderskriverFlytView(behandling: Behandling): MedunderskriverFlytView {
        return MedunderskriverFlytView(
            medunderskriverFlyt = behandling.currentDelbehandling().medunderskriverFlyt
        )
    }

    fun toSakenGjelderWrapped(sakenGjelder: SakenGjelder): SakenGjelderWrapped {
        return SakenGjelderWrapped(
            sakenGjelder = SakenGjelderWrapped.PersonView(
                fnr = sakenGjelder.partId.value,
                navn = pdlFacade.getPersonInfo(sakenGjelder.partId.value).sammensattNavn,
            ),
        )
    }

    private fun Feilregistrering?.toView(): BehandlingDetaljerView.FeilregistreringView? {
        return this?.let {
            BehandlingDetaljerView.FeilregistreringView(
                navIdent = it.navIdent,
                registered = it.registered,
                reason = it.reason,
                fagsystemId = it.fagsystem.id
            )
        }
    }
}