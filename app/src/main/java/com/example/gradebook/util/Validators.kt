package com.example.gradebook.util

object Validators {

    fun isValidNote(valeurText: String): Pair<Boolean, Double?> {
        val valeur = valeurText.toDoubleOrNull()
        if (valeur == null) return Pair(false, null)
        if (valeur < 0.0 || valeur > 20.0) return Pair(false, null)
        return Pair(true, valeur)
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return emailRegex.matches(email.trim())
    }

    fun isNotEmpty(vararg fields: String): Boolean {
        return fields.all { it.trim().isNotEmpty() }
    }
}
