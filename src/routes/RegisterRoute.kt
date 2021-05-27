package com.lid.routes

import com.lid.data.checkIfUserExists
import com.lid.data.collections.User
import com.lid.data.registerUser
import com.lid.data.requests.AccountRequest
import com.lid.responses.SimpleResponse
import io.ktor.application.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.registerRoute() {
    route("/register") {
        post {
            val request = try {
                call.receive<AccountRequest>()
            } catch (e: ContentTransformationException) {
                call.respond(BadRequest)
                return@post
            }
            val userExists = checkIfUserExists(request.username)
            if (!userExists) {
                if (registerUser(User(request.username, request.password))) {
                    call.respond(OK, SimpleResponse(true, "Successfully created account!"))
                } else {
                    call.respond(OK, SimpleResponse(false, "An unknown error occurred"))
                }
            } else {
                call.respond(OK, SimpleResponse(false, "That username already exists"))
            }
        }
    }
}