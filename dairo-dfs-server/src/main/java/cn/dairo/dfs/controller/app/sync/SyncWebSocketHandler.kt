package cn.dairo.dfs.controller.app.sync

import cn.dairo.dfs.extension.toJson
import cn.dairo.dfs.sync.bean.SyncInfo
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler

@Configuration
@EnableWebSocket
class SyncWebSocketHandler : TextWebSocketHandler(), WebSocketConfigurer {

    /**
     * 最后一次链接的session
     */
    private var session: WebSocketSession? = null

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(this, "/ws/app/sync")//.setAllowedOrigins("*")
    }

    /**
     * 客户端建立链接时
     */
    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("连接已建立: " + session.getId());
        this.session = session
    }

//    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
//        val payload = message.payload;
//        println("收到消息: $payload");
//        session.sendMessage(TextMessage("服务器返回: $payload"));
//    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        System.out.println("连接已关闭: " + session.getId())
        this.session = null
    }

    /**
     * 发送消息
     */
    fun send(info: SyncInfo) = try {
        this.session?.sendMessage(TextMessage(info.toJson))
    } catch (e: Exception) {
    }
}