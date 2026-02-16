package com.example.lablablab

import kotlinx.serialization.Serializable

@Serializable
data class House(
    val light: String,
    val door: String,
    val window: String
)