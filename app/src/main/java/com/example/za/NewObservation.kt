package com.example.za
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.za.databinding.ActivityNewObservationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class NewObservation : AppCompatActivity() {

    private lateinit var binding: ActivityNewObservationBinding
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityNewObservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication, Database, and Storage
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Observations")
        storageReference = FirebaseStorage.getInstance().getReference("BirdImages")

        // Set onClickListener for the birdPic button to pick an image
        binding.birdPic.setOnClickListener {
            pickImage()
        }

        binding.set.setOnClickListener {
            val setting = Intent(this, Settings::class.java)
            startActivity(setting)
        }

        binding.pro.setOnClickListener {
            val profile = Intent(this, ViewProfile::class.java)
            startActivity(profile)
        }

        binding.add.setOnClickListener {
            val newObservation = Intent(this, NewObservation::class.java)
            startActivity(newObservation)
        }
        binding.ho.setOnClickListener {
            val home = Intent(this, Home::class.java)
            startActivity(home)
        }
        // Set onClickListener for the Save button
        binding.SaveBirdObservationbtn.setOnClickListener {
            saveObservation()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                imageUri = data.data
                binding.pic.setImageURI(imageUri)
            }
        }
    }

    private fun saveObservation() {
        // Get the currently logged-in user ID
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Collect data from input fields
        val observationId = binding.ID.text.toString().trim()
        val speciesName = binding.sname.text.toString().trim()
        val observationDate = binding.date.text.toString().trim()
        val location = binding.location.text.toString().trim()
        val notes = binding.notes.text.toString().trim()

        if (observationId.isEmpty() || speciesName.isEmpty() || observationDate.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            // Upload image to Firebase Storage
            val fileReference = storageReference.child(System.currentTimeMillis().toString() + ".jpg")
            fileReference.putFile(imageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the image download URL
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        // Save observation data along with image URL under the user's UID
                        saveObservationToDatabase(userId, observationId, speciesName, observationDate, location, notes, imageUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Save observation data without an image under the user's UID
            saveObservationToDatabase(userId, observationId, speciesName, observationDate, location, notes, null)
        }
    }

    private fun saveObservationToDatabase(
        userId: String,
        observationId: String,
        speciesName: String,
        observationDate: String,
        location: String,
        notes: String,
        imageUrl: String?
    ) {
        val observation = mapOf(
            "observationId" to observationId,
            "speciesName" to speciesName,
            "observationDate" to observationDate,
            "location" to location,
            "notes" to notes,
            "imageUrl" to imageUrl
        )

        // Save data under user's UID in Firebase Realtime Database
        database.child(userId).child(observationId).setValue(observation)
            .addOnSuccessListener {
                Toast.makeText(this, "Observation saved successfully", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save observation: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        binding.ID.text.clear()
        binding.sname.text.clear()
        binding.date.text.clear()
        binding.location.text.clear()
        binding.notes.text.clear()
        binding.pic.setImageResource(0) // Clear image
    }
}