package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapp.util.NetworkUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        
        // Disable reCAPTCHA verification for testing - this is crucial for avoiding the error
        try {
            val firebaseAuthSettings: FirebaseAuthSettings = auth.firebaseAuthSettings
            firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to disable reCAPTCHA: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        val emailEditText = view.findViewById<TextInputEditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.passwordEditText)
        val confirmPasswordEditText = view.findViewById<TextInputEditText>(R.id.confirmPasswordEditText)
        val signUpButton = view.findViewById<Button>(R.id.signUpButton)
        val loginTextView = view.findViewById<TextView>(R.id.loginTextView)
        val googleSignInButton = view.findViewById<AppCompatButton>(R.id.googleSignInButton)

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Check network connectivity first
            if (!NetworkUtil.isNetworkAvailable(requireContext())) {
                Toast.makeText(context, "No internet connection. Please check your network settings and try again.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword) {
                // Show loading indicator
                signUpButton.isEnabled = false
                signUpButton.text = "Signing up..."

                // Добавляем лог перед запросом
                Toast.makeText(context, "Attempting to register...", Toast.LENGTH_SHORT).show()
                android.util.Log.d("RegisterFragment", "Attempting to register with email: $email")
                
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                            android.util.Log.d("RegisterFragment", "Registration successful, navigating to login")
                            // Navigate to login screen
                            try {
                                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                                android.util.Log.d("RegisterFragment", "Navigation completed")
                            } catch (e: Exception) {
                                android.util.Log.e("RegisterFragment", "Navigation error: ${e.message}")
                                Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            android.util.Log.e("RegisterFragment", "Registration failed: ${task.exception?.message}")
                            // Show error message with better handling for network errors
                            val errorMessage = when (task.exception) {
                                is FirebaseNetworkException -> 
                                    "Network error. Please check your internet connection and try again."
                                is FirebaseAuthInvalidCredentialsException ->
                                    "Invalid credentials. Please check your email format and password."
                                else -> when {
                                    task.exception?.message?.contains("CONFIGURATION_NOT_FOUND") == true -> 
                                        "Firebase configuration error. Please contact the developer."
                                    task.exception?.message?.contains("email address is already in use") == true ->
                                        "This email is already in use."
                                    task.exception?.message?.contains("password is invalid") == true ->
                                        "Password must be at least 6 characters."
                                    task.exception?.message?.contains("RecaptchaAction") == true ->
                                        "reCAPTCHA verification failed. Please try again later."
                                    else -> task.exception?.message ?: "Unknown error"
                                }
                            }
                            
                            Toast.makeText(context, "Registration error: $errorMessage", Toast.LENGTH_LONG).show()
                            
                            // Reset button
                            signUpButton.isEnabled = true
                            signUpButton.text = "Sign Up"
                        }
                    }
            } else {
                if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loginTextView.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        
        googleSignInButton.setOnClickListener {
            Toast.makeText(context, "Google Sign-In not implemented yet", Toast.LENGTH_SHORT).show()
            // TODO: Implement Google Sign-In
        }
    }
} 