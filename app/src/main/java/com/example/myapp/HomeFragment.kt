package com.example.myapp

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import com.example.myapp.util.BookManager

class HomeFragment : Fragment() {

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                showFileUploadDialog(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up file upload card
        val fileUploadCard = view.findViewById<CardView>(R.id.fileUploadCard)
        val fileUploadButton = view.findViewById<Button>(R.id.fileUploadButton)
        
        fileUploadButton.setOnClickListener {
            openFilePicker()
        }
        
        fileUploadCard.setOnClickListener {
            openFilePicker()
        }
        
        // Set up подписки (subscriptions) card
        val subscriptionButton = view.findViewById<Button>(R.id.subscriptionButton)
        subscriptionButton?.setOnClickListener {
            // Навигация на страницу с реквизитами банковских карт (заглушка)
            findNavController().navigate(R.id.action_homeFragment_to_subscriptionFragment)
        }
        
        // Set up bottom navigation
        val homeNavButton = view.findViewById<ImageView>(R.id.homeNavButton)
        val libraryNavButton = view.findViewById<ImageView>(R.id.libraryNavButton)
        val profileNavButton = view.findViewById<ImageView>(R.id.profileNavButton)
        val flashcardNavButton = view.findViewById<ImageView>(R.id.flashcardNavButton)
        
        homeNavButton.setColorFilter(requireContext().getColor(android.R.color.holo_blue_dark))
        
        libraryNavButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_libraryFragment)
        }
        
        profileNavButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
        
        flashcardNavButton.setOnClickListener {
            findNavController().navigate(R.id.flashcardFragment)
        }
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf", 
                "application/epub+zip", 
                "text/plain"
            ))
        }
        
        getContent.launch(intent)
    }
    
    private fun showFileUploadDialog(uri: Uri) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_file_upload)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        // Get file details
        val contentResolver = requireContext().contentResolver
        var fileName = ""
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        
        // Set up language spinner
        val languageSpinner = dialog.findViewById<Spinner>(R.id.languageSpinner)
        val languages = arrayOf("English", "Қазақша", "Русский")
        val languageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = languageAdapter
        
        // Set up level spinner
        val levelSpinner = dialog.findViewById<Spinner>(R.id.levelSpinner)
        val levels = arrayOf("Бастауыш A1", "Орта B1", "Жоғары C1")
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, levels)
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        levelSpinner.adapter = levelAdapter
        
        // Set file name
        val fileNameEditText = dialog.findViewById<EditText>(R.id.fileNameEditText)
        fileNameEditText.setText(fileName.substringBeforeLast('.'))
        
        // Set up upload button
        val uploadButton = dialog.findViewById<Button>(R.id.uploadButton)
        uploadButton.setOnClickListener {
            val newFileName = fileNameEditText.text.toString()
            if (newFileName.isNotEmpty()) {
                uploadFile(uri, newFileName)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), getString(R.string.file_name_required), Toast.LENGTH_SHORT).show()
            }
        }
        
        // Set up close button
        val closeButton = dialog.findViewById<ImageButton>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun uploadFile(uri: Uri, fileName: String) {
        // Get file details
        val contentResolver = requireContext().contentResolver
        
        // Get file format (более надежное определение формата)
        var fileFormat = ""
        
        // Сначала пытаемся определить формат из MIME-типа
        contentResolver.getType(uri)?.let { mimeType ->
            fileFormat = when {
                mimeType.contains("pdf") -> "pdf"
                mimeType.contains("epub") -> "epub"
                mimeType.contains("text/plain") -> "txt"
                else -> ""
            }
        }
        
        // Если не определили из MIME-типа, пробуем из имени файла
        if (fileFormat.isEmpty()) {
            val name = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) cursor.getString(nameIndex) else null
                } else null
            } ?: uri.lastPathSegment ?: ""
            
            fileFormat = name.substringAfterLast('.', "").lowercase()
        }
        
        // Если всё ещё не определили, пробуем из URI
        if (fileFormat.isEmpty()) {
            fileFormat = uri.toString().substringAfterLast('.', "").lowercase()
        }
        
        // Проверяем поддерживаемые форматы (с учетом разных регистров и некоторых вариаций)
        if (!listOf("pdf", "epub", "txt").contains(fileFormat.lowercase())) {
            Toast.makeText(requireContext(), getString(R.string.unsupported_file_format), Toast.LENGTH_SHORT).show()
            return
        }
        
        // Получаем размер файла
        val fileSize = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) cursor.getLong(sizeIndex) else -1L
            } else -1L
        } ?: -1L
        
        // Create a books directory in the app's private storage
        val booksDir = File(requireContext().filesDir, "books")
        if (!booksDir.exists()) {
            booksDir.mkdirs()
        }
        
        // Create a unique filename to avoid conflicts
        val safeFileName = fileName.replace("[^\\w\\s.-]".toRegex(), "_")
        val uniqueFileName = "${UUID.randomUUID()}_$safeFileName.$fileFormat"
        val destinationFile = File(booksDir, uniqueFileName)
        
        try {
            // Use a buffer for efficient file copying
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }
                    outputStream.flush()
                }
            }
            
            // Log file details for debugging
            android.util.Log.d("HomeFragment", "File saved: ${destinationFile.absolutePath}")
            android.util.Log.d("HomeFragment", "File exists: ${destinationFile.exists()}, Size: ${destinationFile.length()}")
            
            // Добавляем книгу в библиотеку
            val book = BookManager.addBook(
                context = requireContext(),
                title = safeFileName,
                filePath = destinationFile.absolutePath,
                fileFormat = fileFormat,
                fileSize = if (fileSize > 0) fileSize else destinationFile.length()
            )
            
            Toast.makeText(requireContext(), getString(R.string.file_upload_success), Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("HomeFragment", "Error saving file: ${e.message}")
            Toast.makeText(requireContext(), getString(R.string.file_upload_error, e.message), Toast.LENGTH_SHORT).show()
        }
    }
} 