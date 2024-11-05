package com.example.za

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.za.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance() // Realtime Database reference
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.AddPP.setOnClickListener {
            openImageChooser()
        }

        binding.SavePPbtn.setOnClickListener {
            saveProfileData()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            binding.profilePic.setImageURI(imageUri)
        }
    }

    private fun saveProfileData() {
        val username = binding.Username.text.toString().trim()
        val name = binding.Name.text.toString().trim()

        if (username.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please select a profile picture", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = firebaseAuth.currentUser?.uid ?: return

        // Upload profile picture to Firebase Storage
        val storageRef = storage.reference.child("profile_pictures/$userId/${UUID.randomUUID()}.jpg")
        val uploadTask = storageRef.putFile(imageUri!!)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Save user profile data to Realtime Database
                val userProfile = mapOf(
                    "username" to username,
                    "name" to name,
                    "profilePicUrl" to uri.toString()
                )

                val userRef = database.reference.child("users").child(userId).child("Profile")
                userRef.setValue(userProfile)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        // Go back to ViewProfile with the updated data
                        setResult(Activity.RESULT_OK)
                        finish() // This closes the Profile activity and goes back to ViewProfile
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }
}
