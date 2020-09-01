package lappe.xyz.server

import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.ktor.application.*
import io.ktor.server.netty.*
import lappe.xyz.server.services.DBManager
import lappe.xyz.server.services.MailService
import lappe.xyz.server.services.UserService

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {
    Guice.createInjector(
        MainModule(this),
        RouterModule()
    )
}

class MainModule(private val application: Application) : AbstractModule() {
    override fun configure() {
        bind(Application::class.java).toInstance(application)
        bind(PluginConfiguration::class.java).asEagerSingleton()

        bind(DBManager::class.java).asEagerSingleton()
    }
}

