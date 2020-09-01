package lappe.xyz.server.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import java.lang.IllegalArgumentException

data class Recipient(val address: String, val type: RecipientType)


object RecipientTable : IntIdTable() {
    val address = varchar("address", 200)
//    val personalName = varchar("personal_name", 50)
    val type = varchar("type", 50)
//        .check { it inList listOf("To", "Cc", "Bcc") }
    val mail = reference("mail", MailTable)
}

class RecipientDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RecipientDAO>(RecipientTable)

    var address by RecipientTable.address
//    var personalName by RecipientTable.personalName
    var type by RecipientTable.type
    var mail by MailDAO referencedOn RecipientTable.mail

    fun toModel() = Recipient(address, RecipientType.parse(type))
}


enum class RecipientType {
    To, Cc, Bcc;

    companion object {
        fun parse(value: String) = when (value) {
            "To" -> To
            "Cc" -> Cc
            "Bcc" -> Bcc
            else -> throw IllegalArgumentException("$value cant be cast to recipientType")
        }
    }
}
