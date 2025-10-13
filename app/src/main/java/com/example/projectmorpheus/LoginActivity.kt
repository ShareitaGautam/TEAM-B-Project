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
import kotlinx.coroutines.launch

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

        // Email Login and Sign Up Button
        binding.emailActionButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            if (isSignUp) createAccount(email, password)
            else signInWithEmail(email, password)
        }

        // Google Button for Signing In
        binding.googleSignInButton.setOnClickListener { signInWithGoogle()}
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
                e.printStackTrace()
            } // end try-catch
        } // end of launch
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        if (idToken == null) return
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.e("GoogleSignIn", "SignIn Failure", task.exception)
                }
            }
    }
    // Allow User to sign in app with their email
    private fun signInWithEmail(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.e("EmailLogin", "Login Up Failure", task.exception)
                }
            }
    }
    // Allows users to create account to Dream Journal
    // TODO: password must be at least 6 characters long to be accepted by Firebase, we need to
    // add validation to our text input with a visual indicator before submitting our credentials
    private fun createAccount(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.e("EmailSignUp", "Sign Up Failure", task.exception)
                }
            }
    }

}