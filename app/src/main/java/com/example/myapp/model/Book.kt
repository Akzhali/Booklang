package com.example.myapp.model

import java.util.Date

data class Book(
    val id: String,
    val title: String,
    val filePath: String,
    val fileFormat: String,
    val fileSize: Long,
    val coverPath: String? = null,
    val lastReadDate: Date? = null,
    val lastReadPosition: Int = 0
) 