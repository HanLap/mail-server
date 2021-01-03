package lappe.xyz.server.controllers

import io.ktor.routing.*


interface Controller {

    fun setConfiguration(routing: Routing) = routing.configure()

    fun Routing.configure()
}
