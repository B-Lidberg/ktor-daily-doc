package com.lid.data

import com.lid.data.collections.Note
import com.lid.data.collections.User
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("NotesDatabase")
private val users = database.getCollection<User>()
private val notes = database.getCollection<Note>()

suspend fun registerUser(user: User): Boolean {
    return users.insertOne(user).wasAcknowledged()
}

suspend fun checkIfUserExists(username: String): Boolean {
    return users.findOne(User::username eq username) != null
}

suspend fun checkPasswordForUser(username: String, passwordToCheck: String): Boolean {
    val actualPassword = users.findOne(User::username eq username)?.password ?: return false
    return actualPassword == passwordToCheck
}

suspend fun getNotesForUser(username: String): List<Note> {
    return notes.find(Note::owner eq username).toList()
}

suspend fun saveNote(note: Note): Boolean {
    val noteExists = notes.findOneById(note.id) != null
    return if (noteExists) {
        notes.updateOneById(note.id, note).wasAcknowledged()
    } else {
        notes.insertOne(note).wasAcknowledged()
    }
}