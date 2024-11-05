package com.example.za

import java.io.Serializable

data class Observation(
    val observationId: String = "",
    val speciesName: String = "",
    val observationDate: String = "",
    val location: String = "",
    val notes: String? = null,
    val imageUrl: String? = null
) : Serializable
