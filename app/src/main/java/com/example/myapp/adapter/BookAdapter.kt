package com.example.myapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.model.Book
import java.text.SimpleDateFormat
import java.util.Locale

class BookAdapter(private val onBookClick: (Book) -> Unit) : 
    ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view, onBookClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookViewHolder(
        itemView: View,
        private val onBookClick: (Book) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val coverImageView: ImageView = itemView.findViewById(R.id.bookThumbnail)
        private val titleTextView: TextView = itemView.findViewById(R.id.bookTitleText)
        private val detailsTextView: TextView = itemView.findViewById(R.id.bookDetailsText)
        
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        
        fun bind(book: Book) {
            titleTextView.text = book.title
            
            // Format file size
            val fileSizeFormatted = when {
                book.fileSize < 1024 -> "${book.fileSize} B"
                book.fileSize < 1024 * 1024 -> "${book.fileSize / 1024} KB"
                else -> String.format("%.1f MB", book.fileSize / (1024.0 * 1024.0))
            }
            
            // Set formatted details text
            val formatText = book.fileFormat.uppercase()
            val dateText = if (book.lastReadDate != null) {
                dateFormat.format(book.lastReadDate)
            } else {
                ""
            }
            
            if (dateText.isNotEmpty()) {
                detailsTextView.text = "$formatText • $fileSizeFormatted, $dateText"
            } else {
                detailsTextView.text = "$formatText • $fileSizeFormatted"
            }
            
            // Load book cover if available, otherwise show placeholder
            if (book.coverPath != null) {
                // Here you would use an image loading library like Glide or Picasso
                // For example with Glide:
                // Glide.with(itemView.context).load(book.coverPath).into(coverImageView)
            } else {
                coverImageView.setImageResource(android.R.drawable.ic_menu_agenda)
            }
            
            // Set click listener for the entire item
            itemView.setOnClickListener {
                onBookClick(book)
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
} 