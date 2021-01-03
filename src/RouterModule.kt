package lappe.xyz.server

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.multibindings.Multibinder
import io.ktor.application.*
import io.ktor.routing.*
import lappe.xyz.server.controllers.MailController
import lappe.xyz.server.controllers.Controller
import lappe.xyz.server.controllers.UserController
import org.reflections.Reflections


class RouterModule : AbstractModule() {
    override fun configure() {
        bind(MainRouter::class.java).asEagerSingleton()
        val moduleBinder = Multibinder.newSetBinder(binder(), Controller::class.java)

        val reflections = Reflections("lappe.xyz.server")

        with(moduleBinder) {
            reflections.getSubTypesOf(Controller::class.java)
                .forEach { addBinding() to it }
        }
    }
}


class MainRouter @Inject constructor(application: Application, controllers: Set<@JvmSuppressWildcards Controller>) {
    init {
        application.routing {
            controllers.forEach { it.setConfiguration(this) }
        }
    }

}

