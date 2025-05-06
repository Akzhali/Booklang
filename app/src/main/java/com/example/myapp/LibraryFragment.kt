package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.example.myapp.util.BookManager
import java.text.SimpleDateFormat
import java.util.*

class LibraryFragment : Fragment() {

    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: View
    private lateinit var booksTabTextView: TextView
    private lateinit var timeTabTextView: TextView
    private lateinit var bookAdapter: BookAdapter
    private val booksList = mutableListOf<BookManager.Book>()
    
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Initialize views
        booksRecyclerView = view.findViewById(R.id.booksRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        booksTabTextView = view.findViewById(R.id.booksTabTextView)
        timeTabTextView = view.findViewById(R.id.timeTabTextView)

        // Setup profile icon click
        val profileIconButton = view.findViewById<ImageView>(R.id.profileIconButton)
        profileIconButton.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_profileFragment)
        }

        // Setup tabs
        booksTabTextView.setOnClickListener {
            booksTabTextView.setBackgroundResource(R.drawable.tab_selected)
            booksTabTextView.setTextColor(resources.getColor(android.R.color.black, null))
            booksTabTextView.setTypeface(null, android.graphics.Typeface.BOLD)
            
            timeTabTextView.setBackgroundResource(R.drawable.tab_unselected)
            timeTabTextView.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            timeTabTextView.setTypeface(null, android.graphics.Typeface.NORMAL)
            
            // Reload books by name sorting
            loadBooks(false)
        }
        
        timeTabTextView.setOnClickListener {
            timeTabTextView.setBackgroundResource(R.drawable.tab_selected)
            timeTabTextView.setTextColor(resources.getColor(android.R.color.black, null))
            timeTabTextView.setTypeface(null, android.graphics.Typeface.BOLD)
            
            booksTabTextView.setBackgroundResource(R.drawable.tab_unselected)
            booksTabTextView.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            booksTabTextView.setTypeface(null, android.graphics.Typeface.NORMAL)
            
            // Reload books by time sorting
            loadBooks(true)
        }

        // Setup RecyclerView
        setupRecyclerView()
        
        // Load books
        loadBooks(false)
        
        // Setup bottom navigation
        setupBottomNavigation(view)

        val flashcardNavButton = view.findViewById<ImageView>(R.id.flashcardNavButton)
        flashcardNavButton.setOnClickListener {
            findNavController().navigate(R.id.flashcardFragment)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем список книг при возвращении на фрагмент
        loadBooks(timeTabTextView.background.constantState == resources.getDrawable(R.drawable.tab_selected, null).constantState)
    }
    
    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(booksList) { book ->
            // Simple short toast
            Toast.makeText(context, "Открываю: ${book.title}", Toast.LENGTH_SHORT).show()
            
            try {
                // Create simple bundle with minimal data
                val bundle = Bundle().apply {
                    putString("bookId", book.id)
                    putString("bookTitle", book.title)
                    putString("bookFilePath", book.filePath)
                }
                
                // Direct navigation without delay
                findNavController().navigate(R.id.action_libraryFragment_to_readerFragment, bundle)
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        booksRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookAdapter
        }
    }
    
    private fun loadBooks(sortByTime: Boolean) {
        // Clear existing books
        booksList.clear()
        
        // Получаем книги из BookManager
        val books = if (sortByTime) {
            BookManager.getBooksSortedByDate()
        } else {
            BookManager.getBooksSortedByTitle()
        }
        
        if (books.isNotEmpty()) {
            booksList.addAll(books)
            showBooksList()
        } else {
            showEmptyState()
        }
        
        // Notify adapter of changes
        bookAdapter.notifyDataSetChanged()
    }
    
    private fun showBooksList() {
        booksRecyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
    }
    
    private fun showEmptyState() {
        booksRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
    }

    private fun setupBottomNavigation(view: View) {
        val homeNavButton = view.findViewById<ImageView>(R.id.homeNavButton)
        val libraryNavButton = view.findViewById<ImageView>(R.id.libraryNavButton)
        val profileNavButton = view.findViewById<ImageView>(R.id.profileNavButton)

        homeNavButton.setOnClickListener {
            // Navigate to home screen
            findNavController().navigate(R.id.action_libraryFragment_to_homeFragment)
        }

        // Library button is already active
        libraryNavButton.setColorFilter(requireContext().getColor(android.R.color.holo_green_dark))

        profileNavButton.setOnClickListener {
            // Navigate to profile screen
            findNavController().navigate(R.id.action_libraryFragment_to_profileFragment)
        }
    }
    
    // Book adapter class
    inner class BookAdapter(
        private val books: List<BookManager.Book>,
        private val onBookClick: (BookManager.Book) -> Unit
    ) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_book, parent, false)
            return BookViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            val book = books[position]
            holder.bind(book)
            holder.itemView.setOnClickListener { onBookClick(book) }
        }
        
        override fun getItemCount(): Int = books.size
        
        inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.bookTitleText)
            private val detailsTextView: TextView = itemView.findViewById(R.id.bookDetailsText)
            
            fun bind(book: BookManager.Book) {
                titleTextView.text = book.title
                detailsTextView.text = book.getFormattedDetails()
            }
        }
    }
} 