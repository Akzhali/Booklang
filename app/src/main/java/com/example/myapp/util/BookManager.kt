package com.example.myapp.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object BookManager {
    
    private const val PREF_NAME = "book_manager_prefs"
    private const val KEY_BOOKS = "saved_books"
    
    /**
     * Модель книги для хранения
     */
    data class Book(
        val id: String,
        val title: String,
        val filePath: String,
        val fileFormat: String,
        val fileSize: Long,
        val addedDate: Date = Date()
    ) {
        fun getFormattedDetails(): String {
            val sizeFormatted = when {
                fileSize < 1024 -> "$fileSize B"
                fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
                else -> String.format("%.1f MB", fileSize / (1024.0 * 1024.0))
            }
            
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            return "$sizeFormatted, ${dateFormat.format(addedDate)}"
        }
    }
    
    // Private mutable list to store books
    private val booksList: MutableList<Book> = mutableListOf()
    private var initialized: Boolean = false
    
    /**
     * Инициализация менеджера книг
     */
    @Synchronized
    fun initialize(context: Context) {
        if (!initialized) {
            try {
                loadBooks(context)
                initialized = true
            } catch (e: Exception) {
                e.printStackTrace()
                booksList.clear() // Используем пустой список в случае ошибки
                initialized = true
            }
        }
    }
    
    /**
     * Добавление новой книги
     */
    @Synchronized
    fun addBook(context: Context, title: String, filePath: String, fileFormat: String, fileSize: Long): Book {
        val book = Book(
            id = UUID.randomUUID().toString(),
            title = title,
            filePath = filePath,
            fileFormat = fileFormat,
            fileSize = fileSize,
            addedDate = Date()
        )
        
        booksList.add(book)
        saveBooks(context)
        
        return book
    }
    
    /**
     * Получение всех книг
     */
    @Synchronized
    fun getAllBooks(): List<Book> {
        return booksList.toList()
    }
    
    /**
     * Получение книг, отсортированных по заголовку
     */
    @Synchronized
    fun getBooksSortedByTitle(): List<Book> {
        return booksList.sortedBy { it.title }
    }
    
    /**
     * Получение книг, отсортированных по дате
     */
    @Synchronized
    fun getBooksSortedByDate(): List<Book> {
        return booksList.sortedByDescending { it.addedDate }
    }
    
    /**
     * Удаление книги
     */
    @Synchronized
    fun removeBook(context: Context, bookId: String) {
        val book = booksList.find { it.id == bookId }
        if (book != null) {
            booksList.remove(book)
            
            // Удаляем файл
            try {
                val file = File(book.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            saveBooks(context)
        }
    }
    
    /**
     * Сохранение списка книг
     */
    @Synchronized
    private fun saveBooks(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val gson = Gson()
            val json = gson.toJson(booksList)
            prefs.edit().putString(KEY_BOOKS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Загрузка списка книг
     */
    @Synchronized
    private fun loadBooks(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val gson = Gson()
            val json = prefs.getString(KEY_BOOKS, null)
            val loadedBooks = if (json != null) {
                val type = object : TypeToken<List<Book>>() {}.type
                gson.fromJson<List<Book>>(json, type) ?: emptyList()
            } else {
                emptyList<Book>()
            }
            
            // Проверяем, существуют ли файлы книг
            booksList.clear()
            val validBooks = loadedBooks.filter { 
                val file = File(it.filePath)
                file.exists() 
            }
            booksList.addAll(validBooks)
        } catch (e: Exception) {
            e.printStackTrace()
            booksList.clear()
        }
    }
}