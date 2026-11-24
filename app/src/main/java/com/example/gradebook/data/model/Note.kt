package com.example.gradebook.data.model

import com.google.firebase.Timestamp

data class Note(
    val id: String = "",
    val etudiantId: String = "",
    val matiereId: String = "",
    val type: TypeNote = TypeNote.DS,
    val valeur: Double = 0.0,
    val date: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "etudiantId" to etudiantId,
        "matiereId" to matiereId,
        "type" to type.name,
        "valeur" to valeur,
        "date" to date
    )
}
