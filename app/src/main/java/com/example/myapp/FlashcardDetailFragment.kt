package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class FlashcardDetailFragment : Fragment() {
    private var word: String? = null
    private var bookTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            word = it.getString("word")
            bookTitle = it.getString("bookTitle")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_flashcard_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.bookTitleTextView).text = bookTitle
        view.findViewById<TextView>(R.id.wordTextView).text = word

        // Кнопка 'Сөзді сақтау' (заглушка)
        view.findViewById<Button>(R.id.saveWordButton).setOnClickListener {
            // TODO: Реализовать сохранение слова
        }

        // Стрелки навигации (заглушка)
        view.findViewById<ImageButton>(R.id.prevButton).setOnClickListener {
            // TODO: Реализовать переход к предыдущему слову
        }
        view.findViewById<ImageButton>(R.id.nextButton).setOnClickListener {
            // TODO: Реализовать переход к следующему слову
        }

        // Нижнее меню
        view.findViewById<ImageView>(R.id.homeNavButton).setOnClickListener {
            findNavController().navigate(R.id.action_flashcardDetailFragment_to_homeFragment)
        }
        view.findViewById<ImageView>(R.id.libraryNavButton).setOnClickListener {
            findNavController().navigate(R.id.action_flashcardDetailFragment_to_libraryFragment)
        }
        view.findViewById<ImageView>(R.id.flashcardNavButton).setOnClickListener {
            findNavController().navigate(R.id.action_flashcardDetailFragment_to_flashcardFragment)
        }
        view.findViewById<ImageView>(R.id.profileNavButton).setOnClickListener {
            findNavController().navigate(R.id.action_flashcardDetailFragment_to_profileFragment)
        }
    }
} 