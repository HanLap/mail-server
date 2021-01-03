package lappe.xyz.server.services

import com.google.inject.Singleton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lappe.xyz.server.model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.concurrent.thread


@Singleton
class 2DBManager {
    private lateinit var dbConnection: Database

    init {
        GlobalScope.launch {
            kotlin.runCatching {
                dbConnection = Database.connect("jdbc:sqlite:data.db", driver = "org.sqlite.JDBC")

                transaction {
//                    SchemaUtils.drop(
//                        FolderTable,
//                        MailTable,
//                        RecipientTable
//                    )

                    SchemaUtils.create(
                        UserTable,
                        MailAccountTable,
                        FolderTable,
                        MailTable,
                        RecipientTable
                    )
                }
            }.onFailure { throw it }
        }
    }

    fun <R> transaction(transaction: Transaction.() -> R) =
        transaction(dbConnection, transaction)


}
