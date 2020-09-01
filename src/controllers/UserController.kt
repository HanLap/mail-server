package lappe.xyz.server.controllers

import com.google.inject.Inject
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import lappe.xyz.server.model.Session
import lappe.xyz.server.services.UserService


class UserController @Inject constructor(private val userService: UserService) : Controller {

    override fun Routing.configure() {
        route("user") {
            post("/create") {
                create(call)
            }

            post("/login") {
                login(call)
            }

            authenticate {
                post("/logout") {
                    logout(call)
                }
            }
        }
    }


    private suspend fun create(call: ApplicationCall) = with(call) {
        receive<Map<String, String>>()
            .let { it["username"] to it["password"] }
            .let { (username, password) ->
                requireNotNull(username) { respond(BadRequest, "no username given") }
                requireNotNull(password) { respond(BadRequest, "no password given") }

                userService
                    .createUser(username, password)
                    .takeIf { it }
                    ?.let { respond(OK) }
                    ?: respond(BadRequest)
            }
    }


    private suspend fun login(call: ApplicationCall) = with(call) {
        receive<Map<String, String>>()
            .let { it["username"] to it["password"] }
            .let { (username, password) ->
                requireNotNull(username) { respond(BadRequest, "no username given") }
                requireNotNull(password) { respond(BadRequest, "no password given") }

                userService
                    .authenticate(username, password)
                    ?.let {
                        sessions.set(Session(it.toModel(), true))
                        respond(OK)
                    }
                    ?: respond(Unauthorized)
            }
    }


    private suspend fun logout(call: ApplicationCall) = with(call) {
        sessions.clear<Session>()
        respond(OK)
    }


}
