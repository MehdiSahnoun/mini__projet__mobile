package com.example.gradebook.data.model

data class Matiere(
    val id: String = "",
    val nom: String = "",
    val coefficient: Double = 1.0
) {
    fun toMap(): Map<String, Any> = mapOf(
        "nom" to nom,
        "coefficient" to coefficient
    )
}
