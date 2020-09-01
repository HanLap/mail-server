package lappe.xyz.server.services

import com.google.inject.Inject
import com.google.inject.Singleton
import com.sun.mail.imap.IMAPFolder
import io.ktor.util.*
import lappe.xyz.server.model.*
import org.jetbrains.exposed.dao.load
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.mail.*
import javax.mail.Folder
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart


@Singleton
class MailService @Inject constructor(
    private val dbManager: DBManager,
    private val userService: UserService
) {

    fun addAccount(user: User, mailAccount: MailAccount) {
        userService.findUserDAO(user)
            ?.let { userDAO ->

                dbManager.transaction {
                    MailAccountDAO.new {
                        this.user = userDAO
                        username = mailAccount.username
                        password = mailAccount.password
                        smtpHost = mailAccount.smtpHost
                        imapHost = mailAccount.imapHost
                    }
                }.let(::syncMailAccount)

            }
    }

    fun getAllMails(user: User) =
        userService.findUserDAO(user)
            ?.let { dbManager.transaction { it.mailAccounts.map(MailAccountDAO::toModel) } }


    fun getAccountModel(mailAccountId: Int) =
        dbManager.transaction {
            MailAccountDAO.findById(mailAccountId)
                ?.load(MailAccountDAO::folders, FolderDAO::mails)
                ?.toModel()

        }


    fun getAccount(mailAccountId: Int) =
        dbManager.transaction { MailAccountDAO.findById(mailAccountId) }

    fun syncAllMailAccounts(user: User) {
        userService.findUserDAO(user)
            ?.mailAccounts
            ?.forEach(::syncMailAccount)
    }

    fun syncMailAccount(mailAccountId: Int) {
        getAccount(mailAccountId)
            ?.let(::syncMailAccount)
    }

    fun syncMailAccount(mailAccountDAO: MailAccountDAO) {

        val props = Properties()
            .apply {
                put("mail.store.protocol", "imaps")
//                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//                put("mail.smtp.socketFactory.fallback", "false");
            }

        val session = Session.getDefaultInstance(props, null)
        val store = session.getStore("imaps");
        store.connect(mailAccountDAO.imapHost, mailAccountDAO.username, mailAccountDAO.password)

        store.defaultFolder.list().forEach { syncFolder(it, mailAccountDAO) }

        store.close()
    }


    private fun syncFolder(folder: Folder, mailAccountDAO: MailAccountDAO) {
        folder as UIDFolder
        folder.open(Folder.READ_ONLY)

        dbManager.transaction {
            val folderDAO = FolderDAO.new {
                name = folder.name
                mailAccount = mailAccountDAO
            }

            folder.messages.forEach { message ->

                val mailDAO =
                    MailDAO.new {
                        uid = folder.getUID(message)
                        this.folder = folderDAO
                        from = (message.from[0] as InternetAddress).toUnicodeString()
                        subject = message.subject
                        body = parseMailContent(message.content)
                        dateSent = LocalDateTime.ofInstant(message.sentDate.toInstant(), ZoneId.systemDefault())
                        unread = !message.isSet(Flags.Flag.SEEN)
                    }

                message.getRecipients(Message.RecipientType.TO)?.forEach { re ->
                    RecipientDAO.new {
                        mail = mailDAO
                        this.address = (re as InternetAddress).toUnicodeString()
                        this.type = "To"
                    }
                }
                message.getRecipients(Message.RecipientType.CC)?.forEach { re ->
                    RecipientDAO.new {
                        mail = mailDAO
                        this.address = (re as InternetAddress).toUnicodeString()
                        this.type = "Cc"
                    }
                }
                message.getRecipients(Message.RecipientType.BCC)?.forEach { re ->
                    RecipientDAO.new {
                        mail = mailDAO
                        this.address = (re as InternetAddress).toUnicodeString()
                        this.type = "Bcc"
                    }
                }
            }
        }

    }


    private fun parseMailContent(content: Any): String {
        if (content !is MimeMultipart) return ""

        var result = ""
        val count: Int = content.count
        for (i in 0..(count + 1)) {
            val bodyPart: BodyPart = content.getBodyPart(i)
            if (bodyPart.isMimeType("text/plain")) {
                result += "\n${bodyPart.content}"
                break // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                val html = (bodyPart.content as String).let(Jsoup::parse).text()
                result += "\n$html"
            } else if (bodyPart.content is MimeMultipart) {
                result += parseMailContent(bodyPart.content as MimeMultipart)
            }
        }
        return result
    }


    fun sendMail() {
        val props = Properties()
            .apply {
                put("mail.smtp.host", "smtp.web.de")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.socketFactory.port", 587)
//                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//                put("mail.smtp.socketFactory.fallback", "false");
            }


        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("florian.lappe", "eh87q2f5lw")
            }
        })


        kotlin.runCatching {
            MimeMessage(session)
                .apply {
                    setFrom(InternetAddress("florian.lappe@web.de"))
                    addRecipient(Message.RecipientType.TO, InternetAddress("florian.lappe@gmail.com"))
                    subject = "test"
                    setText("alot of test")
                }
                .let(Transport::send)
        }.onFailure { it.printStackTrace() }


        println("finished")
    }

    fun readMails(mailAccountDAO: MailAccountDAO) {
    }

}
