package com.example.za

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.za.databinding.ActivityObservationListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ObservationList : AppCompatActivity() {

    private lateinit var binding: ActivityObservationListBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var observationList: MutableList<Observation>
    private lateinit var adapter: ObservationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObservationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        database = FirebaseDatabase.getInstance().getReference("Observations").child(userId!!)

        observationList = mutableListOf()

        // Set up RecyclerView
        binding.observationRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ObservationAdapter(observationList) { Observation ->
            // Handle click on observation
            val intent = Intent(this, ObservationDetails::class.java)
            intent.putExtra("observation", Observation)
            startActivity(intent)

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
        binding.observationRecyclerView.adapter = adapter

        fetchObservations()
    }

    private fun fetchObservations() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                observationList.clear()
                for (data in snapshot.children) {
                    val observation = data.getValue(Observation::class.java)
                    observation?.let {
                        observationList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // RecyclerView Adapter
    class ObservationAdapter(
        private val observationList: List<Observation>,
        private val clickListener: (Observation) -> Unit
    ) : RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.observation_list_item, parent, false)
            return ObservationViewHolder(view)
        }

        override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
            val observation = observationList[position]
            holder.bind(observation, clickListener)
        }

        override fun getItemCount() = observationList.size



        class ObservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val observationHeading: TextView = itemView.findViewById(R.id.observationHeading)
            private val observationId: TextView = itemView.findViewById(R.id.itemObservationId)
            private val imageView: ImageView = itemView.findViewById(R.id.itemImage)

            fun bind(observation: Observation, clickListener: (Observation) -> Unit) {
                // Set the Observation ID below the heading
                observationId.text = observation.observationId

                // Load image with circular crop if available
                if (!observation.imageUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(observation.imageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageView)
                }

                itemView.setOnClickListener {
                    clickListener(observation)
                }
            }
        }


    }
}
