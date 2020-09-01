package lappe.xyz.server.services

import com.google.inject.Inject
import com.google.inject.Singleton
import lappe.xyz.server.model.User
import lappe.xyz.server.model.UserDAO
import lappe.xyz.server.model.UserTable
import org.springframework.security.crypto.bcrypt.BCrypt

@Singleton
class UserService @Inject constructor(private val dbManager: DBManager) {

    fun findUserDAO(user: User) = dbManager.transaction {
        UserDAO.findById(user.id)
    }

    fun createUser(username: String, password: String): Boolean =
        kotlin.runCatching {
            dbManager.transaction {
                UserDAO.new {
                    this.username = username
                    this.password = BCrypt.hashpw(password, BCrypt.gensalt())
                }
            }
        }.onFailure { it.printStackTrace() }
            .isSuccess

    fun authenticate(username: String, password: String): UserDAO? =
        dbManager
            .transaction { UserDAO.find { UserTable.username eq username }.firstOrNull() }
            ?.takeIf { BCrypt.checkpw(password, it.password) }
}
