package com.example.za

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.util.Locale

class FindBirdHotstops : AppCompatActivity(), OnMapReadyCallback {

    private var mGoogleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var database: DatabaseReference
    private var preferredDistance: Double = Double.MAX_VALUE  // Default to no limit

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_bird_hotstops)

        // Initialize the Places SDK
        Places.initialize(applicationContext, getString(R.string.google_zazu_api_key))

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        database = FirebaseDatabase.getInstance().reference

        // Fetch the user's preferred distance from Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            database.child("users").child(userId).child("Settings").child("distance")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val distanceStr = dataSnapshot.getValue(String::class.java)
                        distanceStr?.let {
                            try {
                                preferredDistance = it.toDouble() * 1000 // Convert km to meters
                                Log.d("FindBirdHotstops", "Preferred distance: $preferredDistance")
                            } catch (e: NumberFormatException) {
                                Toast.makeText(this@FindBirdHotstops, "Invalid distance setting", Toast.LENGTH_SHORT).show()
                                preferredDistance = Double.MAX_VALUE
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@FindBirdHotstops, "Failed to load settings", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap?.uiSettings?.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        mGoogleMap?.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mGoogleMap?.addMarker(
                    MarkerOptions()
                        .position(currentLatLng)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )
                zoomOnMap(currentLatLng)
                filterAndAddMarkers(currentLatLng)
            }
        }

        mGoogleMap?.setOnMarkerClickListener { marker ->
            val destination = marker.position
            getPlaceNameFromCoordinates(destination)
            true
        }
    }

    private fun filterAndAddMarkers(currentLatLng: LatLng) {
        val hotspots = listOf(
            Pair(LatLng(-26.1878, 27.6953), "Randfontein Bird Sanctuary"),
            Pair(LatLng(-26.8426, 27.8256), "Sasol Dam Bird Hide"),
            Pair(LatLng(-26.2041, 28.0473), "Africa Bird Tours"),
            Pair(LatLng(-29.6114, 30.4009), "Cumberland Bird Sanctuary"),
            Pair(LatLng(-26.0811, 27.9732), "President Ridge Bird Sanctuary"),
            Pair(LatLng(-26.1952, 28.0328), "Witwatersrand Bird Club"),
            Pair(LatLng(-25.2953, 27.9039), "Finfoot Bird Hide"),
            Pair(LatLng(-26.1769, 28.3153), "Korsman Bird Sanctuary"),
            Pair(LatLng(-26.0945, 28.0453), "Bishop Bird Park"),
            Pair(LatLng(-26.1751, 28.0547), "Luvies Landing - The Bird Haven"),
            Pair(LatLng(-26.0257, 28.0122), "Montecasino Bird Gardens"),
            Pair(LatLng(-26.1007, 28.1554), "Modderfontein Bird and Sculpture Park"),
            Pair(LatLng(-25.9542, 28.0376), "Beaulieu Bird Sanctuary"),
            Pair(LatLng(-26.1749, 28.0629), "The Wilds Nature Reserve"),
            Pair(LatLng(-25.9012, 28.2643), "Rietvlei Nature Reserve"),
            Pair(LatLng(-26.3295, 28.8339), "Devon Grasslands"),
            Pair(LatLng(-26.1544, 27.8319), "Walter Sisulu National Botanical Garden"),
            Pair(LatLng(-26.3342, 28.5300), "Marievale Bird Sanctuary"),
            Pair(LatLng(-26.4724, 28.3021), "Suikerbosrand Nature Reserve"),
            Pair(LatLng(-25.9591, 28.1811), "Cradle Moon: Green Trail"),
            Pair(LatLng(-25.8325, 27.9846), "Hennops Krokodilberg Route"),
            Pair(LatLng(-25.8381, 28.2912), "Moreleta Kloof Nature Reserve Trail"),
            Pair(LatLng(-26.0939, 27.8452), "Walter Sisulu Botanical Gardens Geological Trail"),
            Pair(LatLng(-25.8077, 28.1267), "Groenkloof Yellow Trail"),
            Pair(LatLng(-26.1137, 28.1611), "Modderfontein Dam Loop"),
            Pair(LatLng(-26.1153, 28.1650), "Modderfontein Dam Trail"),
            Pair(LatLng(-25.8322, 27.9974), "Hennops Zebra Trail"),
            Pair(LatLng(-25.9423, 28.1690), "Dassie, Gifbol, Kiepersol, Ruins Loop"),
            Pair(LatLng(-25.7887, 27.7423), "Rustig Hiking Trail")
        )
        for (hotspot in hotspots) {
            val distanceToHotspot = FloatArray(1)
            Location.distanceBetween(
                currentLatLng.latitude, currentLatLng.longitude,
                hotspot.first.latitude, hotspot.first.longitude,
                distanceToHotspot
            )

            if (distanceToHotspot[0] <= preferredDistance) {
                addMarker(hotspot.first, hotspot.second)
            }
        }
    }

    private fun addMarker(position: LatLng, title: String) {
        mGoogleMap?.addMarker(
            MarkerOptions()
                .position(position)
                .title(title)
        )
    }

    private fun zoomOnMap(latLng: LatLng) {
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng, 12f)
        mGoogleMap?.animateCamera(newLatLngZoom)
    }

    private fun getPlaceNameFromCoordinates(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val placeName = address.featureName ?: address.locality ?: "Unknown Location"
                Toast.makeText(this, "Place: $placeName", Toast.LENGTH_LONG).show()
                launchGoogleMapsDirections(latLng)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Unable to get place name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchGoogleMapsDirections(destination: LatLng) {
        if (::lastLocation.isInitialized) {
            val originLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            val gmmIntentUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, "Google Maps not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                onMapReady(mGoogleMap!!)
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
