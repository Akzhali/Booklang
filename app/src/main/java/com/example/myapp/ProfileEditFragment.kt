package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch


class ProfileEditFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize views
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        
        // Set current user email if available
        val currentUser = auth.currentUser
        if (currentUser != null) {
            emailEditText.setText(currentUser.email)
        }

        // Setup back button
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Setup profile image and camera button
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)
        val cameraButton = view.findViewById<ImageView>(R.id.cameraButton)
        
        cameraButton.setOnClickListener {
            // For now, just show a toast that this feature is coming soon
            Toast.makeText(context, "Сурет жүктеу функциясы әзірленуде", Toast.LENGTH_SHORT).show()
        }
        
        profileImage.setOnClickListener {
            // Also trigger the camera functionality
            cameraButton.performClick()
        }

        // Setup save button
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            updateProfile()
        }

        // Setup bottom navigation
        setupBottomNavigation(view)
    }

    private fun setupBottomNavigation(view: View) {
        val homeNavButton = view.findViewById<ImageView>(R.id.homeNavButton)
        val libraryNavButton = view.findViewById<ImageView>(R.id.libraryNavButton)
        val profileNavButton = view.findViewById<ImageView>(R.id.profileNavButton)

        homeNavButton.setOnClickListener {
            // Navigate to home screen
            findNavController().navigate(R.id.action_profileEditFragment_to_homeFragment)
        }

        libraryNavButton.setOnClickListener {
            // First navigate back to profile
            findNavController().navigateUp()
            // Then navigate to library
            findNavController().navigate(R.id.action_profileFragment_to_libraryFragment)
        }

        // Profile button is already active
        profileNavButton.setColorFilter(requireContext().getColor(android.R.color.holo_green_dark))
    }

    private fun updateProfile() {
        val user = auth.currentUser ?: return
        val newEmail = emailEditText.text.toString().trim()
        val newPassword = passwordEditText.text.toString().trim()
        
        // Validate inputs
        if (newEmail.isEmpty()) {
            emailEditText.error = "Электрондық поштаны енгізіңіз"
            return
        }
        
        // Check if there are actual changes
        var hasChanges = false
        
        // Update email if changed
        if (newEmail != user.email && newEmail.isNotEmpty()) {
            lifecycleScope.launch {
                try {
                    user.updateEmail(newEmail).await()
                    Toast.makeText(context, "Электрондық пошта сәтті жаңартылды", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Қате: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

        }
        
        // Update password if changed and not empty/masked
        if (newPassword.isNotEmpty() && !newPassword.matches(Regex("\\*+"))) {
            if (newPassword.length < 6) {
                passwordEditText.error = "Құпия сөз кемінде 6 таңбадан тұруы керек"
                return
            }
            
            user.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Құпия сөз сәтті жаңартылды", Toast.LENGTH_SHORT).show()
                        hasChanges = true
                    } else {
                        Toast.makeText(context, "Құпия сөзді жаңарту кезінде қате: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
        
        // If no changes were made, still provide feedback and navigate back
        if (!hasChanges) {
            Toast.makeText(context, "Өзгерістер сақталды", Toast.LENGTH_SHORT).show()
        }
        
        // Return to profile screen
        findNavController().navigateUp()
    }
} 