package lappe.xyz.server

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.inject.Inject
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.sessions.*
import lappe.xyz.server.model.Session
import lappe.xyz.server.services.UserService
import java.text.DateFormat
import java.time.format.DateTimeFormatter

class PluginConfiguration @Inject constructor(application: Application) {
    init {
        application.apply {
            install(CORS) {
                method(HttpMethod.Options)
                method(HttpMethod.Put)
                method(HttpMethod.Delete)
                method(HttpMethod.Patch)
                header(HttpHeaders.Authorization)
                header("MyCustomHeader")
                allowCredentials = true
                anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
            }

            install(CallLogging)

            install(Sessions) {
                cookie<Session>("SERVER_SESSION_ID", SessionStorageMemory()) {
                    serializer = object : SessionSerializer<Session> {
                        val jackson = jacksonObjectMapper()
                        override fun deserialize(text: String) = jackson.readValue(text, Session::class.java)
                        override fun serialize(session: Session): String = jackson.writeValueAsString(session)
                    }
                }

            }

            install(Authentication) {
                session<Session> {
                    challenge {
                        call.respond(Unauthorized)
                    }
                    validate { session -> session.takeIf { it.isAuthenticated }?.let { object : Principal {} } }
                }
            }

            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)

                    registerModule(JavaTimeModule())
                    enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    dateFormat = DateFormat.getDateTimeInstance()
                }
            }
        }
    }
}
