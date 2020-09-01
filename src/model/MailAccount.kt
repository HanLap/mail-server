package lappe.xyz.server.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable


data class MailAccount(
    val user: User?,
    val username: String,
    val password: String,
    val smtpHost: String,
//    val smtpPort: Int,
    val imapHost: String,
//    val imapPort: Int,
    val folders: List<Folder>
)

object MailAccountTable : IntIdTable() {
    val user = reference("user", UserTable)
    val username = varchar("username", 50)
    val password = varchar("password", 50)
    val smtpHost = varchar("smtp_host", 100)
    val imapHost = varchar("imap_host", 100)
}

class MailAccountDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MailAccountDAO>(MailAccountTable)

    var user by UserDAO referencedOn MailAccountTable.user
    var username by MailAccountTable.username
    var password by MailAccountTable.password
    var smtpHost by MailAccountTable.smtpHost
    var imapHost by MailAccountTable.imapHost
    val folders by FolderDAO referrersOn FolderTable.mailAccount

    fun toModel() = MailAccount(
        user.toModel(),
        username,
        password,
        smtpHost,
        imapHost,
        folders.copy().map {
            it.toModel()
        }
    )
}


