package no.nav.klage.oppgave.clients.pdl.graphql

data class SoekPersonGraphsqlQuery(
    val query: String,
    val variables: SoekPersonVariables
)

data class SoekPersonVariables(
    val paging: Paging,
    val criteria: List<Criteria>
)

data class Paging(
    val pageNumber: Int,
    val resultsPerPage: Int
)

data class Criteria(
    val fieldName: String,
    val searchRule: SearchRule
)

abstract class SearchRule

class ContainsSearchRule(val contains: String) : SearchRule()

class EqualsSearchRule(val equals: String, val caseSensitive: Boolean = false) : SearchRule()

fun soekPersonNavnContainsQuery(searchString: String): SoekPersonGraphsqlQuery {
    val query = SoekPersonGraphsqlQuery::class.java.getResource("/pdl/soekPerson.graphql").cleanForGraphql()
    return SoekPersonGraphsqlQuery(
        query = query,
        variables = SoekPersonVariables(
            paging = Paging(
                pageNumber = 1,
                resultsPerPage = 30
            ),
            criteria = listOf(
                Criteria(
                    fieldName = "fritekst.navn",
                    searchRule = ContainsSearchRule(searchString)
                )
            )
        )
    )
}
