package lappe.xyz.server.controllers

import com.google.inject.Inject
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lappe.xyz.server.model.MailAccount
import lappe.xyz.server.model.Session
import lappe.xyz.server.services.MailService


class MailController @Inject constructor(
    private val mailService: MailService
) : Controller {

    override fun Routing.configure() {
        authenticate {
            route("/mail") {
                get {
                    with(call) {
                        sessions.get<Session>()!!.user
                            .let(mailService::getAllMails)
                            ?.let { respond(OK, it) }
                            ?: respond(InternalServerError)
                    }
                }
                get("/sync") {
                    mailService.syncAllMailAccounts(
                        call.sessions.get<Session>()!!.user
                    )
                    call.respond(OK)
                }

                route("/account") {
                    post {
                        createAccount(call)
                    }

                    route("/{id}") {
                        get {
                            getAccount(call)
                        }

                        get("/sync") {
                            syncAccount(call)
                        }

                        // mail uid of account
                        route("/{uid}") {

                            post("/read") {

                            }

                            delete {
                                deleteMail(call)
                            }
                        }

                    }
                }
            }
        }
    }

    private suspend fun deleteMail(call: ApplicationCall) = with(call) {

    }


    private suspend fun createAccount(call: ApplicationCall) {
        with(call) {
            val user = sessions.get<Session>()!!.user
            val account = receive<MailAccount>()
            kotlin
                .runCatching {
                    mailService.addAccount(user, account)
                }
                .onSuccess { respond(OK) }
                .onFailure {
                    it.printStackTrace()
                    respond(InternalServerError)
                }
        }
    }

    private suspend fun getAccount(call: ApplicationCall) {
        with(call) {
            parameters["id"]
                ?.toIntOrNull()
                ?.let(mailService::getAccountModel)
                ?.let { respond(OK, it) }
                ?: respond(BadRequest)
        }
    }


    private suspend fun syncAccount(call: ApplicationCall) {
        with(call) {
            parameters["id"]
                ?.toIntOrNull()
                ?.let {
                    GlobalScope.launch { mailService.syncMailAccount(it) }
                }
                ?.also { respond(OK) }
                ?: respond(BadRequest, "malformed id parameter")
        }
    }
}
