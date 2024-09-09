package cn.dairo.dfs.controller.app.sync

import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler


class SyncWebSocketHandler : TextWebSocketHandler(), WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(this, "/ws").setAllowedOrigins("*")
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("连接已建立: " + session.getId());
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload;
        println("收到消息: $payload");
        session.sendMessage(TextMessage("服务器返回: $payload"));
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        System.out.println("连接已关闭: " + session.getId());
    }
}