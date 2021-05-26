package com.lid.data.collections

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Note(
    @BsonId
    val id: String = ObjectId().toString(),
    val date: Long,
    val summary: String,
    val content: String,
    val survey1: String,
    val survey2: String,
    val survey3: String,
    val owner: String,
    val users: List<String>,
)