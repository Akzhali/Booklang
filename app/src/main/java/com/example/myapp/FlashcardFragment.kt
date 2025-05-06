package com.example.myapp

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.util.BookManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FlashcardFragment : Fragment() {
    private val words = mutableListOf<String>()
    private lateinit var adapter: FlashcardWordAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_flashcard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        words.addAll(getDummyWords())
        val recyclerView = view.findViewById<RecyclerView>(R.id.flashcardRecyclerView)
        adapter = FlashcardWordAdapter(words) { word ->
            val bundle = Bundle().apply {
                putString("word", word)
                putString("bookTitle", "Преступление и наказание. Ф. Достоевский")
            }
            findNavController().navigate(R.id.action_flashcardFragment_to_flashcardDetailFragment, bundle)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Кнопка 'Учиреніп бастау'
        view.findViewById<View>(R.id.startLearningButton).setOnClickListener {
            if (adapter.itemCount > 0) {
                val firstWord = words.first()
                val bundle = Bundle().apply {
                    putString("word", firstWord)
                    putString("bookTitle", "Преступление и наказание. Ф. Достоевский")
                }
                findNavController().navigate(R.id.action_flashcardFragment_to_flashcardDetailFragment, bundle)
            } else {
                Toast.makeText(context, "Нет слов для изучения", Toast.LENGTH_SHORT).show()
            }
        }

        // FAB для добавления слова
        val fab = FloatingActionButton(requireContext())
        fab.setImageResource(R.drawable.ic_add)
        fab.setColorFilter(resources.getColor(android.R.color.white))
        fab.backgroundTintList = requireContext().getColorStateList(R.color.purple_500)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        (view as ViewGroup).addView(fab, params)
        fab.translationZ = 12f
        fab.elevation = 12f
        fab.setOnClickListener { showAddWordDialog() }
        fab.compatElevation = 12f
        fab.layoutParams = (fab.layoutParams as ViewGroup.LayoutParams).apply {
            width = 140
            height = 140
        }
        fab.x = view.width - 180f
        fab.y = view.height - 260f

        // Нижнее меню
        view.findViewById<ImageView>(R.id.homeNavButton).setOnClickListener {
            findNavController().navigate(R.id.action_flashcardFragment_to_homeFragment)
        }
        view.findViewById<ImageView>(R.id.libraryNavButton).setOnClickListener {
            findNavController().navigate(R.id.action_flashcardFragment_to_libraryFragment)
        }
        view.findViewById<ImageView>(R.id.profileNavButton).setOnClickListener {
            findNavController().navigate(R.id.action_flashcardFragment_to_profileFragment)
        }
    }

    private fun showAddWordDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_flashcard_word)
        val wordEditText = dialog.findViewById<EditText>(R.id.wordEditText)
        val translationEditText = dialog.findViewById<EditText>(R.id.translationEditText)
        val bookSpinner = dialog.findViewById<Spinner>(R.id.bookSpinner)
        val saveButton = dialog.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)

        // Получаем список книг (заглушка)
        val books = BookManager.getAllBooks().map { it.title }
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, books)
        bookSpinner.adapter = adapterSpinner

        saveButton.setOnClickListener {
            val word = wordEditText.text.toString().trim()
            val translation = translationEditText.text.toString().trim()
            val book = bookSpinner.selectedItem?.toString() ?: ""
            if (word.isNotEmpty() && translation.isNotEmpty() && book.isNotEmpty()) {
                words.add("$word — $translation ($book)")
                adapter.notifyItemInserted(words.size - 1)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun getDummyWords(): List<String> {
        return listOf(
            "Преступление и наказание. Ф. Дост...",
            "Неге біз кітап оқу керекпіз?",
            "Неге біз кітап оқу керекпіз?",
            "Неге біз кітап оқу керекпіз?"
        )
    }
} 