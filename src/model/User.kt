package lappe.xyz.server.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable


data class User(val id: Int, val username: String)

object UserTable : IntIdTable() {
    val username = varchar("name", 50).uniqueIndex()
    val password = varchar("password", 200)
}

class UserDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDAO>(UserTable)

    var username by UserTable.username
    var password by UserTable.password
    val mailAccounts by MailAccountDAO referrersOn MailAccountTable.user

    fun toModel() = User(id.value, username)
}


