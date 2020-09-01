package lappe.xyz.server.model


data class Session(val user: User, val isAuthenticated: Boolean = false)
