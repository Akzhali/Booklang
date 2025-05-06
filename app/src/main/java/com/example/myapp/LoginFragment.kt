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

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
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
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val registerTextView = view.findViewById<TextView>(R.id.registerTextView)
        val forgotPasswordTextView = view.findViewById<TextView>(R.id.forgotPasswordTextView)
        val googleSignInButton = view.findViewById<AppCompatButton>(R.id.googleSignInButton)

        loginButton.setOnClickListener {
    Toast.makeText(context, "Login button pressed", Toast.LENGTH_SHORT).show()
    android.util.Log.d("LoginFragment", "Login button pressed")
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            android.util.Log.d("LoginFragment", "Email: $email, Password: ${if (password.isNotEmpty()) "[HIDDEN]" else "EMPTY"}")

            // Check network connectivity first
            if (!NetworkUtil.isNetworkAvailable(requireContext())) {
                Toast.makeText(context, "No internet connection. Please check your network settings and try again.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty()) {
                android.util.Log.d("LoginFragment", "Email and password are not empty, proceeding with login")
                // Show loading indicator
                loginButton.isEnabled = false
                loginButton.text = "Logging in..."

                // Добавляем лог перед запросом
                Toast.makeText(context, "Attempting to login...", Toast.LENGTH_SHORT).show()
                android.util.Log.d("LoginFragment", "Attempting to login with email: $email")
                
                android.util.Log.d("LoginFragment", "Calling signInWithEmailAndPassword")
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                            android.util.Log.d("LoginFragment", "Login successful, navigating to home")
                            // Navigate to home screen
                            try {
                                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                                android.util.Log.d("LoginFragment", "Navigation completed")
                            } catch (e: Exception) {
                                android.util.Log.e("LoginFragment", "Navigation error: ${e.message}")
                                Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            // Login failed
                            android.util.Log.e("LoginFragment", "signInWithEmailAndPassword:failure: ${task.exception?.message}")
                            // Show error message with better handling for network errors
                            val errorMessage = when (task.exception) {
                                is FirebaseNetworkException -> 
                                    "Network error. Please check your internet connection and try again."
                                is FirebaseAuthInvalidCredentialsException ->
                                    "Invalid credentials. Please check your email and password."
                                else -> when {
                                    task.exception?.message?.contains("CONFIGURATION_NOT_FOUND") == true -> 
                                        "Firebase configuration error. Please contact the developer."
                                    task.exception?.message?.contains("no user record") == true ->
                                        "No user found with this email."
                                    task.exception?.message?.contains("password is invalid") == true ->
                                        "Invalid password."
                                    task.exception?.message?.contains("RecaptchaAction") == true ->
                                        "reCAPTCHA verification failed. Please try again later."
                                    else -> task.exception?.message ?: "Unknown error"
                                }
                            }
                            
                            Toast.makeText(context, "Login error: $errorMessage", Toast.LENGTH_SHORT).show()
                            // Reset button
                            loginButton.isEnabled = true
                            loginButton.text = "Log in"
                        }
                    }
            } else {
                android.util.Log.w("LoginFragment", "Email or password is empty")
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        registerTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        
        forgotPasswordTextView.setOnClickListener {
            val email = emailEditText.text.toString()
            
            // Check network connectivity first
            if (!NetworkUtil.isNetworkAvailable(requireContext())) {
                Toast.makeText(context, "No internet connection. Please check your network settings and try again.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorMessage = when (task.exception) {
                                is FirebaseNetworkException -> 
                                    "Network error. Please check your internet connection."
                                else -> task.exception?.message ?: "Unknown error"
                            }
                            Toast.makeText(context, "Failed to send reset email: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please enter your email address", Toast.LENGTH_SHORT).show()
            }
        }
        
        googleSignInButton.setOnClickListener {
            Toast.makeText(context, "Google Sign-In not implemented yet", Toast.LENGTH_SHORT).show()
            // TODO: Implement Google Sign-In
        }
    }
} 