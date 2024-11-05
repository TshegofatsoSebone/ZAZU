package com.example.za

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.za.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.SignUpbtn.setOnClickListener {
            val email = binding.SignupEmail.text.toString()
            val password = binding.SignupPassword.text.toString()
            val confirmPassword = binding.SignupConPassword.text.toString()

            if (password == confirmPassword) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Signup successful
                            Toast.makeText(this, "SignUp Successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, SignIn::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Signup failed, log the error message
                            val errorMessage = task.exception?.message
                            Toast.makeText(this, "SignUp Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signIn.setOnClickListener {
            Toast.makeText(this@SignUp,"Now Moving to Sign In ", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, SignIn::class.java))
            finish()
        } }
}