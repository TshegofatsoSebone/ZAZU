package com.example.za

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.za.databinding.ActivityViewProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ViewProfile : AppCompatActivity() {

    private lateinit var binding: ActivityViewProfileBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.reference.child("users").child(firebaseAuth.currentUser?.uid ?: "").child("Profile")

    companion object {
        private const val PROFILE_UPDATE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fetch and display user profile data
        fetchProfileData()

        // Button to update profile, linking to Profile activity
        binding.updateProfileBtn.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivityForResult(intent, PROFILE_UPDATE_REQUEST) // Start Profile activity and wait for result
        }
        binding.s.setOnClickListener {
            val setting = Intent(this, Settings::class.java)
            startActivity(setting)
        }

        binding.h.setOnClickListener {
            val home = Intent(this, Home::class.java)
            startActivity(home)
        }

        binding.a.setOnClickListener {
            val newObservation = Intent(this, NewObservation::class.java)
            startActivity(newObservation)
        }
        binding.v.setOnClickListener {
            val exploreD = Intent(this, ObservationList::class.java)
            startActivity(exploreD)
        }
    }

    private fun fetchProfileData() {
        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val username = snapshot.child("username").getValue(String::class.java)
                val name = snapshot.child("name").getValue(String::class.java)
                val profilePicUrl = snapshot.child("profilePicUrl").getValue(String::class.java)

                // Display profile data in bold headings
                binding.usernameTextView.text = "$username"
                binding.nameTextView.text = "$name"

                // Load profile picture using Glide with circular crop transformation
                Glide.with(this)
                    .load(profilePicUrl)
                    .apply(RequestOptions.circleCropTransform()) // Make image circular
                    .into(binding.profileImageView)
            } else {
                Toast.makeText(this, "Profile data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check if we returned from the Profile activity with updated data
        if (requestCode == PROFILE_UPDATE_REQUEST && resultCode == RESULT_OK) {
            // Refresh profile data after returning from Profile activity
            fetchProfileData()
        }
    }
}
