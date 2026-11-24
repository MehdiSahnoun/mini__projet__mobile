package com.example.gradebook.data.model

data class User(
    val uid: String = "",
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val role: Role = Role.ETUDIANT,
    val classe: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "nom" to nom,
        "prenom" to prenom,
        "email" to email,
        "role" to role.name,
        "classe" to classe
    )
}
