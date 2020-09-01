package lappe.xyz.server.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable


data class Folder(val name: String, val mails: List<Mail>)

object FolderTable : IntIdTable() {
    val name = varchar("name", 50)
    val mailAccount = reference("mailAccount", MailAccountTable)
}

class FolderDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FolderDAO>(FolderTable)

    var name by FolderTable.name
    var mailAccount by MailAccountDAO referencedOn FolderTable.mailAccount
    val mails by MailDAO referrersOn MailTable.folder

    fun toModel(): Folder = Folder(
        name,
        mails.copy().map {
            it.toModel()
        })
}
