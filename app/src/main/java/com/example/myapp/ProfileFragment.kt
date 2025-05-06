package com.example.myapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapp.util.LanguageUtil
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        // Setup UI elements
        setupUI(view)
    }

    private fun setupUI(view: View) {
        // Set user email
        val emailTextView = view.findViewById<TextView>(R.id.emailTextView)
        val currentUser = auth.currentUser
        emailTextView.text = currentUser?.email ?: "210103251@stu.sdu.edu.kz"

        // Setup back button
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Profile Settings Item
        val profileSettingsItem = view.findViewById<ConstraintLayout>(R.id.profileSettingsItem)
        profileSettingsItem.setOnClickListener {
            // Navigate to profile edit screen
            findNavController().navigate(R.id.action_profileFragment_to_profileEditFragment)
        }

        // Setup Language Settings Item
        val languageSettingsItem = view.findViewById<ConstraintLayout>(R.id.languageSettingsItem)
        val selectedLanguage = view.findViewById<TextView>(R.id.selectedLanguage)
        
        // Устанавливаем текущий язык
        val currentLanguage = LanguageUtil.getLanguage(requireContext())
        selectedLanguage.text = when(currentLanguage) {
            LanguageUtil.Language.KAZAKH.code -> getString(R.string.language_kz)
            LanguageUtil.Language.RUSSIAN.code -> getString(R.string.language_ru)
            LanguageUtil.Language.ENGLISH.code -> getString(R.string.language_en)
            else -> getString(R.string.language_kz)
        }
        
        languageSettingsItem.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // Setup About Us Settings Item
        val aboutUsSettingsItem = view.findViewById<ConstraintLayout>(R.id.aboutUsSettingsItem)
        aboutUsSettingsItem.setOnClickListener {
            showAboutUsDialog()
        }

        // Setup Notifications Settings Item
        val notificationsSwitch = view.findViewById<SwitchCompat>(R.id.notificationsSwitch)
        notificationsSwitch.isChecked = sharedPreferences.getBoolean("notifications_enabled", false)
        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply()
            Toast.makeText(
                context,
                if (isChecked) "Хабарламалар қосылды" else "Хабарламалар өшірілді",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Setup Logout Button
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Setup Bottom Navigation
        setupBottomNavigation(view)

        // Setup Flashcard Navigation
        val flashcardNavButton = view.findViewById<ImageView>(R.id.flashcardNavButton)
        flashcardNavButton.setOnClickListener {
            findNavController().navigate(R.id.flashcardFragment)
        }
    }

    private fun setupBottomNavigation(view: View) {
        val homeNavButton = view.findViewById<ImageView>(R.id.homeNavButton)
        val libraryNavButton = view.findViewById<ImageView>(R.id.libraryNavButton)
        val profileNavButton = view.findViewById<ImageView>(R.id.profileNavButton)

        homeNavButton.setOnClickListener {
            // Navigate to home screen
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }

        libraryNavButton.setOnClickListener {
            // Navigate to library screen
            findNavController().navigate(R.id.action_profileFragment_to_libraryFragment)
        }

        // Profile button is already active
        profileNavButton.setColorFilter(requireContext().getColor(android.R.color.holo_blue_dark))
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            getString(R.string.language_kz),
            getString(R.string.language_ru),
            getString(R.string.language_en)
        )
        
        // Получаем текущий индекс языка
        val currentLanguage = LanguageUtil.getLanguage(requireContext())
        val currentIndex = when(currentLanguage) {
            LanguageUtil.Language.KAZAKH.code -> 0
            LanguageUtil.Language.RUSSIAN.code -> 1
            LanguageUtil.Language.ENGLISH.code -> 2
            else -> 0
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.language))
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                dialog.dismiss()
                
                // Устанавливаем новый язык в зависимости от выбора
                val languageCode = when(which) {
                    0 -> LanguageUtil.Language.KAZAKH.code
                    1 -> LanguageUtil.Language.RUSSIAN.code
                    2 -> LanguageUtil.Language.ENGLISH.code
                    else -> LanguageUtil.Language.KAZAKH.code
                }
                
                // Если выбранный язык отличается от текущего
                if (languageCode != currentLanguage) {
                    // Применяем новый язык
                    setNewLocale(languageCode)
                }
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun setNewLocale(languageCode: String) {
        // Применяем новый язык
        LanguageUtil.applyLanguage(requireContext(), languageCode)
        
        // Просто пересоздаем активность без перенаправления
        requireActivity().recreate()
    }

    private fun showAboutUsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.about_us))
            .setMessage("BookLang - бұл тілдерді кітаптар арқылы оқып үйренуге арналған қосымша. " +
                    "Біз тілді үйренудің ең жақсы тәсілі - ол тілде оқу деп сенеміз.")
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirm))
            .setPositiveButton(android.R.string.yes) { _, _ ->
                logout()
            }
            .setNegativeButton(android.R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logout() {
        // Sign out from Firebase
        auth.signOut()
        
        // Clear preferences if needed
        // sharedPreferences.edit().clear().apply()
        
        // Navigate to login screen
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        
        Toast.makeText(context, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
    }
} 