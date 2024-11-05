package com.example.za

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.za.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Settings : AppCompatActivity() {

    // Declare view binding variable
    private lateinit var binding: ActivitySettingsBinding

    // Firebase Database reference
    private lateinit var database: DatabaseReference

    // Firebase Auth reference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Set switch listener
        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            val currentDistance = binding.max.text.toString()

            if (currentDistance.isNotEmpty()) {
                val distance = currentDistance.toDouble()

                if (isChecked) {
                    // Convert from kilometers to miles
                    val miles = kilometersToMiles(distance)
                    binding.max.setText(miles.toString())
                    binding.switch1.text = "Switch to Kilometers"
                    Toast.makeText(this, "Converted to Miles", Toast.LENGTH_SHORT).show()
                } else {
                    // Convert from miles to kilometers
                    val kilometers = milesToKilometers(distance)
                    binding.max.setText(kilometers.toString())
                    binding.switch1.text = "Switch to Miles"
                    Toast.makeText(this, "Converted to Kilometers", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.hom.setOnClickListener {
            val home = Intent(this, Home::class.java)
            startActivity(home)
        }

        binding.p.setOnClickListener {
            val profile = Intent(this, ViewProfile::class.java)
            startActivity(profile)
        }

        binding.added.setOnClickListener {
            val newObservation = Intent(this, NewObservation::class.java)
            startActivity(newObservation)
        }
        binding.vie.setOnClickListener {
            val exploreD = Intent(this, ObservationList::class.java)
            startActivity(exploreD)
        }
        // Save data to Firebase when the Save button is clicked
        binding.saveSettingsbtn.setOnClickListener {
            saveSettingsToFirebase()
        }
    }

    // Conversion functions
    private fun kilometersToMiles(km: Double): Double {
        return km * 0.621371
    }

    private fun milesToKilometers(miles: Double): Double {
        return miles / 0.621371
    }

    // Save settings to Firebase under the authenticated user's UID
    private fun saveSettingsToFirebase() {
        val distance = binding.max.text.toString()

        // Ensure the user is authenticated
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the authenticated user's UID
        val userId = currentUser.uid

        if (distance.isNotEmpty()) {
            val isMiles = binding.switch1.isChecked

            // Create a map with the settings data
            val settingsData = mapOf(
                "distance" to distance,
                "unit" to if (isMiles) "miles" else "kilometers"
            )

            // Save settings under the user's UID in the database
           database.child("users").child(userId).child("Settings").setValue(settingsData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save settings", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please enter a distance", Toast.LENGTH_SHORT).show()
        }
    }
}
