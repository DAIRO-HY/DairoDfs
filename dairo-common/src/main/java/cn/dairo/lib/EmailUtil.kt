package cn.dairo.lib

import java.util.*
import javax.mail.Address
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeUtility

/**
 * 使用SMTP协议发送电子邮件
 */
object EmailUtil {
    /**
     * qq邮箱服务器主机
     */
    private const val QQ_MAIL_HOST = "smtp.qq.com"

    /**
     * 通过qq邮箱发送邮件
     *
     * @param from:发件人地址
     * @param password:密码
     * @param to:收件人
     * @param title:标题
     * @param body:内容
     * @return
     */
    fun sendByQQ(from: String, password: String?, to: String?, title: String?, body: String?): Boolean {
        return sendByQQ(from, password, to, title, title, body)
    }

    /**
     * 通过qq邮箱发送邮件
     *
     * @param from:发件人地址
     * @param password:密码
     * @param to:收件人
     * @param title:标题
     * @param displayName:显示名称
     * @param body:内容
     * @return
     */
    fun sendByQQ(
        from: String, password: String?, to: String?, title: String?, displayName: String?,
        body: String?
    ): Boolean {
        return send(from, from, password, to, displayName, title, body, true, QQ_MAIL_HOST)
    }

    /**
     * @param from:发件人邮箱
     * @param userName:登录名
     * @param password:密码
     * @param to:收件人
     * @param displayName:显示名称
     * @param title:标题
     * @param body:内容
     * @param isHtml:是否html格式发送
     * @param host:邮件服务器主机
     * @return
     */
    fun send(
        from: String, userName: String?, password: String?, to: String?, displayName: String?,
        title: String?, body: String?, isHtml: Boolean, host: String?
    ): Boolean {
        var transport: Transport? = null
        val props = Properties()

        // 开启debug调试
        props.setProperty("mail.debug", "false")

        // 发送服务器需要身份验证
        props.setProperty("mail.smtp.auth", "false")

        // 设置邮件服务器主机名
        props.setProperty("mail.host", host)

        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", "smtp")
        return try {

            // 设置环境信息
            val session = Session.getInstance(props)

            // 创建邮件对象
            val msg = MimeMessage(session)

            // 邮件标题
            msg.subject = title

            // 设置发件人
            msg.setFrom(
                InternetAddress(MimeUtility.encodeText(displayName) + "<" + from + ">")
            )
            if (isHtml) {

                // html格式
                msg.setContent(body, "text/html;charset=gb2312")
            } else {
                msg.setText(body)
            }
            transport = session.transport

            // 连接邮件服务器
            transport.connect(userName, password)

            // 发送邮件
            transport.sendMessage(msg, arrayOf<Address>(InternetAddress(to)))
            true
        } catch (e: Exception) {
            false
        } finally {

            // 关闭连接
            try {
                transport!!.close()
            } catch (e: Exception) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
    }
}