package no.nav.klage.dokument.api.controller

import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException


@Component
class SocketTextHandler : TextWebSocketHandler() {

    @Throws(InterruptedException::class, IOException::class)
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload: String = message.payload
        session.sendMessage(TextMessage("The payload was $payload"));
    }
}