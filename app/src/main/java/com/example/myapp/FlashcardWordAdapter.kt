package com.example.myapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FlashcardWordAdapter(
    private val words: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<FlashcardWordAdapter.WordViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flashcard_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(words[position])
    }

    override fun getItemCount(): Int = words.size

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val wordTextView: TextView = itemView.findViewById(R.id.wordTextView)
        private val progressTextView: TextView = itemView.findViewById(R.id.progressTextView)
        fun bind(word: String) {
            wordTextView.text = word
            progressTextView.text = "0%"
            itemView.setOnClickListener { onClick(word) }
        }
    }
} 