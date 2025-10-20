package com.example.projectmorpheus

// Imports //
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.projectmorpheus.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.launch
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var binding: ActivityLoginBinding
    private var isSignUp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // We skip login process if user is already logged in
        firebaseAuth.currentUser?.let {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Toggle for Login and Sign Up
        binding.toggleSignUpTextView.setOnClickListener{
            isSignUp = !isSignUp
            binding.emailActionButton.text = if (isSignUp) "Sign Up" else "Login"
            binding.toggleSignUpTextView.text =
                if (isSignUp) "Already have an Account? Login"
                else "Don't have an account? Sign Up"
        }

        // TODO: Submit password forms to firebase for verification
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.emailActionButton.performClick()
                true
            } else false
        }
        // Email Login and Sign Up Button
        binding.emailActionButton.setOnClickListener {
            clearErrorMessage()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            if (!validateInputs(email, password)) return@setOnClickListener

            if (isSignUp) createAccount(email, password)
            else signInWithEmail(email, password)
        }

        // Google Button for Signing In
        binding.googleSignInButton.setOnClickListener { signInWithGoogle()}
    }

    // TODO: user input validation
    private fun validateInputs(email: String, password: String): Boolean {
        val emailPattern =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")


        return when {
            email.isEmpty() -> {
                showError("Email is required")
                false
            }
            !emailPattern.matcher(email).matches() -> {
                showError("Enter a valid email address")
                false
            }
            password.isEmpty() -> {
                showError("Password is required")
                false
            }
            password.length < 6 -> {
                showError("Password must be at least 6 characters long")
                false
            }
            else -> true
        }
        /*
        // I fuser did not input an email, return a error
        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is Required"
            binding.emailEditText.requestFocus()
            return false
        }
        // If email is not valid, return error
        if (!emailPattern.matcher(email).matches()) {
            binding.emailEditText.error = "Enter a valid email address"
            binding.emailEditText.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password is Required"
            binding.passwordEditText.requestFocus()
            return false
        }
        // If password is shorter than 6 characters, print a error
        if (password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters long"
            binding.passwordEditText.requestFocus()
            return false
        } */
        //return true
    }

    // Allow Users to Login/SignIn with their google accounts
    private fun signInWithGoogle() {
        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@LoginActivity)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                // This is our web client ID through firebase google
                .setServerClientId("411527177263-bd1afb8rgsshhuk44ue2kgf5rmbgq3rm.apps.googleusercontent.com")
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(this@LoginActivity, request)
                val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val idToken = credential.idToken
                firebaseAuthWithGoogle(idToken)
            } catch (e: GetCredentialException){
                showError("Google sign-in failed. Please try again.")
                Log.e("GoogleSignIn", "Failure", e)
                //e.printStackTrace()
                //Toast.makeText(this@LoginActivity, "Google sign-in failed", Toast.LENGTH_SHORT).show()
            } // end try-catch
        } // end of launch
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        if (idToken == null) return
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    clearErrorMessage()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    firebaseError(task.exception)
                    //showError("Google sign-in failed: ${task.exception?.localizedMessage}")
                    //Log.e("GoogleSignIn", "Failure", task.exception)
                    //val errorMsg = task.exception?.localizedMessage ?: "Google sign-in failed"
                    //Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                    //Log.e("GoogleSignIn", "SignIn Failure", task.exception)
                }
            }
    }
    // Allow User to sign in app with their email
    private fun signInWithEmail(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    clearErrorMessage()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    firebaseError(task.exception)
                }
            }
    }
    private fun createAccount(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    clearErrorMessage()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    firebaseError(task.exception)
                }
            }
    }
    // Allows users to create account to Dream Journal
    // TODO: password must be at least 6 characters long to be accepted by Firebase, we need to
    // add validation to our text input with a visual indicator before submitting our credentials

    // Handling Firebase Login Exceptions
    private fun firebaseError(exception: Exception?) {
        val message = when (exception) {
            is FirebaseAuthInvalidUserException ->
                "No account has been found with this email."
            is FirebaseAuthInvalidCredentialsException ->
                "Invalid Credentials. Please check password."
            is FirebaseAuthUserCollisionException ->
                "This email has already been registered. Please try logging in."
            is FirebaseAuthWeakPasswordException ->
                "Password must be at least 6 characters long. Please try again."
            else ->
                exception?.localizedMessage ?: "Authentication failed. Please try again."
        }
        showError(message)
        //Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e("FirebaseAuthError", "Error: ${exception?.message}", exception)
    }

    private fun showError(message: String) {
        binding.errorTextView.text = message
        binding.errorTextView.alpha = 1f
    }

    private fun clearErrorMessage() {
        binding.errorTextView.text = ""
    }

}