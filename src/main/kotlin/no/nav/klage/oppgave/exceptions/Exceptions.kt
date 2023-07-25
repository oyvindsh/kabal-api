package no.nav.klage.oppgave.exceptions

class DuplicateOversendelseException(msg: String) : RuntimeException(msg)

class OppgaveNotFoundException(msg: String) : RuntimeException(msg)

class JournalpostNotFoundException(msg: String) : ValidationException(msg)

class BehandlingNotFoundException(msg: String) : RuntimeException(msg)

class VedtakNotFoundException(msg: String) : RuntimeException(msg)

class ResultatDokumentNotFoundException(msg: String) : RuntimeException(msg)

class MeldingNotFoundException(msg: String) : RuntimeException(msg)

class SaksdokumentNotFoundException(msg: String) : RuntimeException(msg)

class BehandlingFinalizedException(msg: String) : RuntimeException(msg)

open class ValidationException(msg: String) : RuntimeException(msg)

class NotMatchingUserException(msg: String) : RuntimeException(msg)

class FeatureNotEnabledException(msg: String) : RuntimeException(msg)

class NoSaksbehandlerRoleException(msg: String) : RuntimeException(msg)

class NotOwnEnhetException(msg: String) : RuntimeException(msg)

class MissingTilgangException(msg: String) : RuntimeException(msg)

class OversendtKlageNotValidException(msg: String) : RuntimeException(msg)

class OversendtKlageReceivedBeforeException(msg: String) : RuntimeException(msg)

class KlagebehandlingSamtidigEndretException(msg: String) : RuntimeException(msg)

class BehandlingAvsluttetException(msg: String) : RuntimeException(msg)

class PreviousBehandlingNotFinalizedException(msg: String) : RuntimeException(msg)

class BehandlingManglerMedunderskriverException(msg: String) : RuntimeException(msg)

class BehandlingManglerTildelingException(msg: String) : RuntimeException(msg)

class EnhetNotFoundForSaksbehandlerException(msg: String) : RuntimeException(msg)

class IllegalOperation(msg: String) : RuntimeException(msg)

class FeilregistreringException(msg: String) : RuntimeException(msg)

/**
 * When PDL-api works, but responds with an error for some reason
 */
class PDLErrorException(msg: String) : RuntimeException(msg)

class PDLPersonNotFoundException(msg: String) : RuntimeException(msg)

class EREGOrganizationNotFoundException(msg: String) : RuntimeException(msg)

class EREGOrganizationCeasedException(msg: String) : RuntimeException(msg)

class AttachmentCouldNotBeConvertedException(override val message: String = "FILE_COULD_NOT_BE_CONVERTED") : RuntimeException()

class DocumentsToMergeReferenceNotFoundException(msg: String) : RuntimeException(msg)