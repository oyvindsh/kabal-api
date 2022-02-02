package no.nav.klage.dokument.domain

import java.util.*

data class SmartEditorDokument(val smartEditorId: UUID, val mellomlagretDokument: OpplastetMellomlagretDokument) :
    MellomlagretDokument by mellomlagretDokument