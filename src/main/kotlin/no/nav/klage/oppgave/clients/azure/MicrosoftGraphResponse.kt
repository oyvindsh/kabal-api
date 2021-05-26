package no.nav.klage.oppgave.clients.azure

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class MicrosoftGraphIdentResponse(val onPremisesSamAccountName: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MicrosoftGraphNameResponse(val value: List<Value>?) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Value(val onPremisesSamAccountName: String?, val displayName: String?)
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MicrosoftGraphUsersResponse(val value: List<Value>?) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Value(val userPrincipalName: String)
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MicrosoftGraphGroupMembersResponse(val value: List<Value>?) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Value(val id: String, val mail: String?)
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MicrosoftGraphMemberOfResponse(val value: List<Group>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Group(
    val id: String,
    val displayName: String? = null,
    val mailNickname: String? = null,
)