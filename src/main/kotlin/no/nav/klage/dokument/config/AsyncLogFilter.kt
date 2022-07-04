package no.nav.klage.dokument.config

import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class AsyncLogFilter : Filter {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val secureLogger = getSecureLogger()
    }

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        if (request != null && request.dispatcherType == DispatcherType.ASYNC) {
            if (request is HttpServletRequest) {
                printHeaderData(request)
                secureLogger.debug("path = " + request.pathInfo)
                secureLogger.debug("requestURI = " + request.requestURI)
            }
        }

        chain?.doFilter(request, response)
    }

    private fun printHeaderData(request: HttpServletRequest) {
        var headers = ""
        for (headerName in request.headerNames) {
            headers += headerName + ": " + request.getHeader(headerName) + "\n"
        }
        secureLogger.debug(headers)
    }

}