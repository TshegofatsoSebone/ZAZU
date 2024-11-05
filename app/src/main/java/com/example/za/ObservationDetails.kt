package com.example.za
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.za.databinding.ActivityObservationDetailsBinding

class ObservationDetails : AppCompatActivity() {

    private lateinit var binding: ActivityObservationDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObservationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.exit.setOnClickListener {
            val back = Intent(this, ObservationList::class.java)
            startActivity(back)
        }

        val observation = intent.getSerializableExtra("observation") as? Observation

        observation?.let {
            // Set the retrieved data below the headings
            binding.detailObservationId.text = it.observationId
            binding.detailSpeciesName.text = it.speciesName
            binding.detailObservationDate.text = it.observationDate
            binding.detailLocation.text = it.location
            binding.detailNotes.text = it.notes ?: "No notes provided"

            // Load the image if the URL is available
            // Load the image in circular shape if the URL is available
            // Load the image in circular shape if the URL is available
            if (!it.imageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(it.imageUrl)
                    .circleCrop()  // This makes the image circular
                    .into(binding.detailImage)
            }


        }
    }
}


