package com.lid

import com.lid.data.checkPasswordForUser
import com.lid.routes.loginRoute
import com.lid.routes.noteRoutes
import com.lid.routes.registerRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Authentication) {
        configureAuth()
    }
    install(Routing) {
        registerRoute()
        loginRoute()
        noteRoutes()
    }
}

private fun Authentication.Configuration.configureAuth() {
    basic {
        realm = "DailyDoc Server"
        validate { credentials ->
            val username = credentials.name
            val password = credentials.password
            if (checkPasswordForUser(username, password)) {
                UserIdPrincipal(username)
            } else {
                null
            }
        }
    }
}


