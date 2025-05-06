package com.example.myapp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.core.view.GestureDetectorCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rajat.pdfviewer.PdfViewerActivity
import java.io.File

class ReaderFragment : Fragment() {

    private lateinit var readerRootLayout: CoordinatorLayout
    private lateinit var backButton: ImageButton
    private lateinit var bookTitleTextView: TextView
    private lateinit var pdfBadge: TextView
    private lateinit var shareButton: ImageButton
    private lateinit var downloadButton: ImageButton
    private lateinit var bookInfoTextView: TextView
    private lateinit var bookHeaderTextView: TextView
    private lateinit var contentScrollView: NestedScrollView
    private lateinit var bookContentTextView: TextView
    private lateinit var pageIndicatorTextView: TextView
    private lateinit var leftTouchArea: View
    private lateinit var rightTouchArea: View
    
    // Settings dialog
    private lateinit var settingsDialog: CardView
    private lateinit var fontSizeSeekBar: SeekBar
    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var lightThemeRadioButton: RadioButton
    private lateinit var darkThemeRadioButton: RadioButton
    private lateinit var sepiaThemeRadioButton: RadioButton
    private lateinit var closeSettingsButton: Button
    
    // Translation dialog
    private lateinit var translationDialog: CardView
    private lateinit var selectedWordTextView: TextView
    private lateinit var languageRadioGroup: RadioGroup
    private lateinit var russianRadioButton: RadioButton
    private lateinit var kazakhRadioButton: RadioButton
    private lateinit var translationTextView: TextView
    private lateinit var saveTranslationButton: Button
    private lateinit var closeTranslationDialogButton: Button
    
    // Gesture detection
    private lateinit var gestureDetector: GestureDetectorCompat
    
    // Book data
    private var bookId: String = ""
    private var bookTitle: String = ""
    private var bookFilePath: String = ""
    private var bookContent: String = ""
    private var bookReadTime: String = "16 мин"
    private var bookDate: String = "02.03.2023"
    private var currentPosition: Int = 0
    
    // Page navigation
    private var pageHeight: Int = 0
    private var currentPage: Int = 0
    private var totalPages: Int = 1
    
    // Reader settings
    private var fontSize: Int = 18
    private var fontType: Typeface = Typeface.DEFAULT
    private var textColor: Int = Color.BLACK
    private var backgroundColor: Int = Color.WHITE
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reader, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments
        arguments?.let { args ->
            bookId = args.getString("bookId") ?: ""
            bookTitle = args.getString("bookTitle") ?: ""
            bookFilePath = args.getString("bookFilePath") ?: ""
        }

        // Initialize views
        initViews(view)

        // Set book info
        bookInfoTextView.text = "$bookReadTime · $bookDate"
        bookHeaderTextView.text = bookTitle
        bookTitleTextView.text = "BookLang"
        
        // Set PDF badge visibility based on file type
        if (bookFilePath.endsWith(".pdf", ignoreCase = true)) {
            pdfBadge.visibility = View.VISIBLE
        } else {
            pdfBadge.visibility = View.GONE
        }

        // Setup gesture detector for page turning
        setupGestureDetector()

        // Set up listeners
        setupListeners()

        // Load book content
        loadBookContent()

        // Calculate page metrics after layout is ready
        view.post {
            calculatePageMetrics()
            updatePageIndicators()
        }
    }
    
    private fun initViews(view: View) {
        readerRootLayout = view.findViewById(R.id.readerRootLayout)
        // Initialize views
        backButton = view.findViewById(R.id.backButton)
        bookTitleTextView = view.findViewById(R.id.bookTitleTextView)
        pdfBadge = view.findViewById(R.id.pdfBadge)
        shareButton = view.findViewById(R.id.shareButton)
        downloadButton = view.findViewById(R.id.downloadButton)
        bookInfoTextView = view.findViewById(R.id.bookInfoTextView)
        bookHeaderTextView = view.findViewById(R.id.bookHeaderTextView)
        contentScrollView = view.findViewById(R.id.contentScrollView)
        bookContentTextView = view.findViewById(R.id.bookContentTextView)
        pageIndicatorTextView = view.findViewById(R.id.pageIndicatorTextView)
        leftTouchArea = view.findViewById(R.id.leftTouchArea)
        rightTouchArea = view.findViewById(R.id.rightTouchArea)
        
        // Initialize settings dialog
        settingsDialog = view.findViewById(R.id.settingsDialog)
        fontSizeSeekBar = view.findViewById(R.id.fontSizeSeekBar)
        themeRadioGroup = view.findViewById(R.id.themeRadioGroup)
        lightThemeRadioButton = view.findViewById(R.id.lightThemeRadioButton)
        darkThemeRadioButton = view.findViewById(R.id.darkThemeRadioButton)
        sepiaThemeRadioButton = view.findViewById(R.id.sepiaThemeRadioButton)
        closeSettingsButton = view.findViewById(R.id.closeSettingsButton)
        
        // Initialize translation dialog
        translationDialog = view.findViewById(R.id.translationDialog)
        selectedWordTextView = view.findViewById(R.id.selectedWordTextView)
        languageRadioGroup = view.findViewById(R.id.languageRadioGroup)
        russianRadioButton = view.findViewById(R.id.russianRadioButton)
        kazakhRadioButton = view.findViewById(R.id.kazakhRadioButton)
        translationTextView = view.findViewById(R.id.translationTextView)
        saveTranslationButton = view.findViewById(R.id.saveTranslationButton)
        closeTranslationDialogButton = view.findViewById(R.id.closeTranslationDialogButton)
        
        // Initialize page indicator
        pageIndicatorTextView.text = "1/1"
    }
    
    private fun setupGestureDetector() {
        gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                // Show/hide the settings dialog when tapping in the middle of the screen
                val screenWidth = resources.displayMetrics.widthPixels
                val x = e.x
                
                if (x > screenWidth * 0.3 && x < screenWidth * 0.7) {
                    // Tap in the middle area of the screen
                    if (settingsDialog.visibility == View.VISIBLE) {
                        settingsDialog.visibility = View.GONE
                    } else {
                        settingsDialog.visibility = View.VISIBLE
                    }
                    return true
                }
                return false
            }
        })
    }
    
    private fun setupListeners() {
        // Set up back button
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // Set up share button
        shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, bookTitle)
                putExtra(Intent.EXTRA_TEXT, "Читаю книгу '$bookTitle' в приложении BookLang")
            }
            startActivity(Intent.createChooser(shareIntent, "Поделиться книгой"))
        }
        
        // Set up download button
        downloadButton.setOnClickListener {
            Toast.makeText(context, "Книга уже загружена на устройство", Toast.LENGTH_SHORT).show()
        }
        
        // Set up left touch area for previous page
        leftTouchArea.setOnClickListener {
            navigateToPreviousPage()
        }
        
        // Set up right touch area for next page
        rightTouchArea.setOnClickListener {
            navigateToNextPage()
        }
        
        // Set up font size seek bar
        fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Ensure minimum font size of 12
                    fontSize = maxOf(12, progress)
                    bookContentTextView.textSize = fontSize.toFloat()
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Set up theme radio group
        themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.lightThemeRadioButton -> {
                    backgroundColor = Color.WHITE
                    textColor = Color.BLACK
                }
                R.id.darkThemeRadioButton -> {
                    backgroundColor = Color.BLACK
                    textColor = Color.WHITE
                }
                R.id.sepiaThemeRadioButton -> {
                    backgroundColor = Color.rgb(250, 240, 230) // Sepia tone
                    textColor = Color.rgb(90, 55, 30) // Dark brown
                }
            }
            applySettings()
        }
        
        // Set up settings dialog close button
        closeSettingsButton.setOnClickListener {
            settingsDialog.visibility = View.GONE
        }
        
        // Set up translation dialog close button
        closeTranslationDialogButton.setOnClickListener {
            translationDialog.visibility = View.GONE
        }
        
        // Set up save translation button
        saveTranslationButton.setOnClickListener {
            val word = selectedWordTextView.text.toString()
            val translation = translationTextView.text.toString()
            val language = if (russianRadioButton.isChecked) "ru" else "kk"
            saveTranslation(word, translation, language)
            translationDialog.visibility = View.GONE
        }
        
        // Set up language radio buttons
        languageRadioGroup.setOnCheckedChangeListener { _, _ ->
            val word = selectedWordTextView.text.toString()
            translateWord(word)
        }
        
        // Set up scroll listener to update page indicators
        contentScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            currentPosition = scrollY
            updateCurrentPage()
            updatePageIndicators()
        }
        
        // Apply gesture detector to content scroll view
        contentScrollView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false // Allow normal touch events to be processed
        }
    }
    
    private fun loadBookContent() {
        try {
            val progressBar = view?.findViewById<View>(R.id.translationProgressBar)
            progressBar?.visibility = View.VISIBLE
            
            if (bookFilePath.endsWith(".pdf", ignoreCase = true)) {
                // Log the file details for debugging
                Log.d("ReaderFragment", "Trying to load PDF: ${bookFilePath}")
                
                val file = File(bookFilePath)
                if (file.exists()) {
                    Log.d("ReaderFragment", "File exists: ${file.exists()}, Size: ${file.length()}")
                    
                    try {
                        // Use the integrated PDF viewer library
                        val uri = FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.fileprovider",
                            file
                        )
                        
                        startActivity(
                            PdfViewerActivity.launchPdfFromPath(
                                context = requireContext(),
                                path = file.absolutePath,
                                pdfTitle = bookTitle,
                                enableDownload = false,
                                directoryName = "",
                                fromAssets = false
                            )
                        )
                        
                        // Hide progress bar
                        progressBar?.visibility = View.GONE
                        
                    } catch (e: Exception) {
                        // If the PDF viewer library fails, try with intent
                        Log.e("ReaderFragment", "Error using PDF viewer: ${e.message}")
                        if (!openPdfWithIntent(file)) {
                            Toast.makeText(context, "Ошибка открытия PDF: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        progressBar?.visibility = View.GONE
                    }
                } else {
                    progressBar?.visibility = View.GONE
                    Toast.makeText(context, "Файл не найден: ${bookFilePath}", Toast.LENGTH_LONG).show()
                    Log.e("ReaderFragment", "File not found: ${bookFilePath}")
                }
            } else {
                // Regular text handling for non-PDF files
                bookContentTextView.visibility = View.VISIBLE
                
                // Use a simple mock content for now
                Thread {
                    try {
                        // Simulate loading time
                        Thread.sleep(100)
                        
                        // Create mock content
                        val mockContent = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                        bookContent = mockContent.repeat(20)
                        
                        // Update UI on main thread
                        activity?.runOnUiThread {
                            try {
                                // Set content text
                                bookContentTextView.text = bookContent
                                
                                // Make words clickable
                                makeWordsClickable()
                                
                                // Hide loading indicator
                                progressBar?.visibility = View.GONE
                                bookContentTextView.visibility = View.VISIBLE
                                
                                // Calculate page metrics after layout is ready
                                contentScrollView.post {
                                    calculatePageMetrics()
                                    updatePageIndicators()
                                }
                            } catch (e: Exception) {
                                // Handle UI update errors
                                progressBar?.visibility = View.GONE
                                bookContentTextView.visibility = View.VISIBLE
                                bookContentTextView.text = "Ошибка отображения: ${e.message}"
                            }
                        }
                    } catch (e: Exception) {
                        // Handle background thread errors
                        activity?.runOnUiThread {
                            progressBar?.visibility = View.GONE
                            bookContentTextView.visibility = View.VISIBLE
                            bookContentTextView.text = "Ошибка загрузки: ${e.message}"
                        }
                    }
                }.start()
            }
        } catch (e: Exception) {
            // Handle any errors in the main thread
            val progressBar = view?.findViewById<View>(R.id.translationProgressBar)
            progressBar?.visibility = View.GONE
            bookContentTextView.visibility = View.VISIBLE
            bookContentTextView.text = "Критическая ошибка: ${e.message}"
            Log.e("ReaderFragment", "Critical error: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun makeWordsClickable() {
        val words = bookContent.split(Regex("\\s+"))
        val spannableString = SpannableString(bookContent)
        
        for (word in words) {
            if (word.length < 2) continue // Skip very short words
            
            var startIndex = 0
            while (startIndex != -1) {
                startIndex = bookContent.indexOf(word, startIndex)
                if (startIndex != -1) {
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            showTranslationDialog(word)
                        }
                        
                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false // No underline
                        }
                    }
                    
                    val endIndex = startIndex + word.length
                    spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    startIndex = endIndex
                }
            }
        }
        
        bookContentTextView.text = spannableString
        bookContentTextView.movementMethod = LinkMovementMethod.getInstance()
    }
    
    private fun showTranslationDialog(word: String) {
        selectedWordTextView.text = word
        translationDialog.visibility = View.VISIBLE
        translateWord(word)
    }
    
    private fun translateWord(word: String) {
        // In a real app, this would call the translation API
        // For now, we'll simulate a translation
        val targetLanguage = if (russianRadioButton.isChecked) "ru" else "kk"
        
        // Simulate translation based on language
        val translation = when (word.lowercase()) {
            "lorem" -> if (targetLanguage == "ru") "боль" else "ауырсыну"
            "ipsum" -> if (targetLanguage == "ru") "сам" else "өзі"
            "dolor" -> if (targetLanguage == "ru") "боль" else "ауырсыну"
            "sit" -> if (targetLanguage == "ru") "сидеть" else "отыру"
            "amet" -> if (targetLanguage == "ru") "любовь" else "махаббат"
            "consectetur" -> if (targetLanguage == "ru") "следствие" else "салдары"
            "adipiscing" -> if (targetLanguage == "ru") "жир" else "май"
            "elit" -> if (targetLanguage == "ru") "элита" else "элита"
            else -> if (targetLanguage == "ru") "Перевод не найден" else "Аударма табылмады"
        }
        
        // Update UI after "API call"
        view?.postDelayed({
            translationTextView.text = translation
        }, 500)
    }
    
    private fun saveTranslation(word: String, translation: String, language: String) {
        // In a real app, this would save to a Room database
        // For now, we'll just show a toast
        val message = "$word — $translation ($language)"
        Toast.makeText(context, "Сохранено: $message", Toast.LENGTH_SHORT).show()
    }
    
    private fun saveBookmark() {
        // In a real app, this would save the current position to a database
        currentPosition = contentScrollView.scrollY
        // For now, we'll just show a toast
        Toast.makeText(context, "Закладка сохранена на позиции $currentPosition", Toast.LENGTH_SHORT).show()
    }
    
    private fun applySettings() {
        // Apply font size
        bookContentTextView.textSize = fontSize.toFloat()
        
        // Apply font type
        bookContentTextView.typeface = fontType
        
        // Apply text color
        bookContentTextView.setTextColor(textColor)
        
        // Apply background color
        readerRootLayout.setBackgroundColor(backgroundColor)
        
        // Adjust text color based on background
        adjustTextColorForBackground()
    }
    
    private fun adjustTextColorForBackground() {
        // If background is dark, make text light
        if (backgroundColor == Color.BLACK) {
            textColor = Color.WHITE
            bookContentTextView.setTextColor(textColor)
            bookTitleTextView.setTextColor(Color.WHITE)
            bookHeaderTextView.setTextColor(Color.WHITE)
            bookInfoTextView.setTextColor(Color.WHITE)
        } else {
            // If we previously had a dark background, reset text color
            if (textColor == Color.WHITE) {
                textColor = Color.BLACK
                bookContentTextView.setTextColor(textColor)
                bookTitleTextView.setTextColor(Color.BLACK)
                bookHeaderTextView.setTextColor(Color.BLACK)
                bookInfoTextView.setTextColor(Color.GRAY)
            }
        }
    }
    
    private fun hideAllDialogs() {
        settingsDialog.visibility = View.GONE
        translationDialog.visibility = View.GONE
    }
    
    // Page navigation methods
    
    private fun calculatePageMetrics() {
        // Get the height of the visible area
        val visibleHeight = contentScrollView.height
        
        // Get the total height of the content
        val totalHeight = bookContentTextView.height
        
        // Calculate page height (visible area minus padding)
        pageHeight = visibleHeight
        
        // Calculate total pages
        totalPages = if (pageHeight > 0) {
            (totalHeight + pageHeight - 1) / pageHeight // Ceiling division
        } else {
            1
        }
        
        // Calculate current page
        updateCurrentPage()
    }
    
    private fun updateCurrentPage() {
        currentPage = if (pageHeight > 0) {
            (currentPosition / pageHeight) + 1
        } else {
            1
        }
    }
    
    private fun updatePageIndicators() {
        // Update page indicator text
        pageIndicatorTextView.text = "$currentPage/$totalPages"
        
        // Show/hide page indicator based on page count
        if (totalPages > 1) {
            pageIndicatorTextView.visibility = View.VISIBLE
        } else {
            pageIndicatorTextView.visibility = View.GONE
        }
    }
    
    private fun navigateToPreviousPage() {
        if (currentPage > 1) {
            val newPosition = (currentPage - 2) * pageHeight
            contentScrollView.smoothScrollTo(0, newPosition)
        }
    }
    
    private fun navigateToNextPage() {
        if (currentPage < totalPages) {
            val newPosition = currentPage * pageHeight
            contentScrollView.smoothScrollTo(0, newPosition)
        }
    }

    /**
     * Opens a PDF file using an external PDF viewer intent
     */
    private fun openPdfWithIntent(file: File): Boolean {
        return try {
            Log.d("ReaderFragment", "Opening PDF with intent: ${file.absolutePath}")
            
            // Create a content URI using FileProvider
            val contentUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
            
            // Create and start the intent
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
                true
            } else {
                Toast.makeText(context, "Нет приложений для просмотра PDF", Toast.LENGTH_LONG).show()
                false
            }
        } catch (e: Exception) {
            Log.e("ReaderFragment", "Error opening PDF with intent: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
