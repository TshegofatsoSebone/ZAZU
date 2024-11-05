package com.example.za

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.za.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // Initialize the binding object
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use binding to reference views instead of findViewById
        binding.nextp.setOnClickListener {
            Toast.makeText(this@MainActivity, "Now moving to sign up", Toast.LENGTH_SHORT).show()

            val intent2 = Intent(this, SignUp::class.java)
            startActivity(intent2)
        }
    }
}