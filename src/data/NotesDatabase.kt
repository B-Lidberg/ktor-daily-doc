package com.lid.data

import com.lid.data.collections.Note
import com.lid.data.collections.User
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue

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
    val noteExists = notes.findOneById(note.noteId) != null
    return if (noteExists) {
        notes.updateOneById(note.noteId, note).wasAcknowledged()
    } else {
        notes.insertOne(note).wasAcknowledged()
    }
}

suspend fun isOwnerOfNote(username: String, noteId: String): Boolean {
    val note = notes.findOneById(noteId) ?: return false
    return username in note.owner
}

suspend fun isUserOfNote(username: String, noteId: String): Boolean {
    val note = notes.findOneById(noteId) ?: return false
    return username in note.users
}

suspend fun addUserToNote(username: String, noteId: String): Boolean {
    val users = notes.findOneById(noteId)?.users ?: return false
    return notes.updateOneById(noteId, setValue(Note::users, users + username)).wasAcknowledged()
}

suspend fun deleteNoteForUser(email: String, noteId: String): Boolean {
    val note = notes.findOne(
        Note::noteId eq noteId,
        Note::users contains email
    )
    note?.let { note ->
        if (note.users.size > 1) {
            // the note has multiple owners, so we just delete the email from the owners list
            val newOwners = note.users - email
            val updateResult = notes.updateOne(
                filter = Note::noteId eq MongoOperator.id,
                setValue(Note::users, newOwners)
            )
            return updateResult.wasAcknowledged()
        }
        return notes.deleteOneById(MongoOperator.id).wasAcknowledged()
    } ?: return false
}