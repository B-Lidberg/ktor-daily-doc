package com.lid.routes

import com.lid.data.*
import com.lid.data.collections.Note
import com.lid.data.requests.AddUserRequest
import com.lid.responses.SimpleResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.noteRoutes() {
    route("/getNotes") {
        authenticate {
            get {
                val username = call.principal<UserIdPrincipal>()!!.name
                val notes = getNotesForUser(username)
                call.respond(OK, notes)
            }
        }
    }

    route("/addNote") {
        authenticate {
            post {
                val note = try {
                    call.receive<Note>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (saveNote(note)) {
                    call.respond(OK)
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }

    route("/addUserToNote") {
        authenticate {
            post {
                val request = try {
                    call.receive<AddUserRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (!checkIfUserExists(request.user)) {
                    call.respond(
                        OK,
                        SimpleResponse(false, "The username: ${request.user} does not exist!")
                    )
                    return@post
                }
                if (isOwnerOfNote(request.owner, request.id)) {
                    call.respond(
                        OK,
                        SimpleResponse(false, "You cannot assign yourself!")
                    )
                    return@post
                }
                if (isUserOfNote(request.user, request.id)) {
                    call.respond(
                        OK,
                        SimpleResponse(false, "${request.user} can already view this note")
                    )
                    return@post
                }
                if (addUserToNote(request.user, request.id)) {
                    call.respond(
                        OK,
                        SimpleResponse(true, "${request.user} can now view this note!")
                    )
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }
}