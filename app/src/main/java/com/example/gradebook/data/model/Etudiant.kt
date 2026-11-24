package com.example.gradebook.data.model

data class Etudiant(
    val id: String = "",
    val nom: String = "",
    val prenom: String = "",
    val numeroEtudiant: String = "",
    val classe: String = "",
    val uid: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "nom" to nom,
        "prenom" to prenom,
        "numeroEtudiant" to numeroEtudiant,
        "classe" to classe,
        "uid" to uid
    )
}
