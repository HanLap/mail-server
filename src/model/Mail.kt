package lappe.xyz.server.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime
import java.util.*

data class Mail(
    val uid: Long,
    val from: String,
    val subject: String,
    val body: String,
    val recipients: List<Recipient>,
    val dateSent: LocalDateTime,
    val unread: Boolean
)

object MailTable : IntIdTable() {
    val uid = long("uid")
    val from = varchar("from", 200)
    val subject = varchar("subject", 500)
    val body = varchar("body", Int.MAX_VALUE)
    val folder = reference("folder", FolderTable)
    val dateSent = datetime("date_sent")
    val unread = bool("unread")
}

class MailDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MailDAO>(MailTable)

    var uid by MailTable.uid
    var from by MailTable.from
    var subject by MailTable.subject
    var body by MailTable.body
    val recipients by RecipientDAO referrersOn RecipientTable.mail
    var folder by FolderDAO referencedOn MailTable.folder
    var dateSent by MailTable.dateSent
    var unread by MailTable.unread

    fun toModel() = Mail(uid, from, subject, body, recipients.map { it.toModel() }, dateSent, unread)
}
