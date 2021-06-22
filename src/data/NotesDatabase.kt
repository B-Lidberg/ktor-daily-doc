package com.lid.data

import com.lid.data.collections.Note
import com.lid.data.collections.User
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue
/*
If testing on local machine only through emulator or physical device.
remove connectionString from KMongo.createClient. I have it set up to run on wifi.
 plus testing for future server potential!
 */
private val client = KMongo.createClient("mongodb+srv://DailyDocUser:xpvgg2W238Tf7mfA@dailydocdatabase.z622f.mongodb.net").coroutine
private val database = client.getDatabase("DailyDocDatabase")
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
    val noteFromDb = notes.findOne(
        Note::noteId eq noteId,
        Note::users contains email
    )
    noteFromDb?.let { note ->
        if (note.users.size > 1) {
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