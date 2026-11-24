package com.example.gradebook.util

import com.example.gradebook.data.model.Matiere
import com.example.gradebook.data.model.Note
import kotlin.math.round

object MoyenneCalculator {

    /**
     * Calcule la moyenne simple des notes d'une matière donnée.
     * Retourne null si aucune note n'est présente pour cette matière.
     */
    fun calculerMoyenneMatiere(notesMatiere: List<Note>): Double? {
        if (notesMatiere.isEmpty()) return null
        val somme = notesMatiere.sumOf { it.valeur }
        return roundToTwoDecimals(somme / notesMatiere.size)
    }

    /**
     * Calcule la moyenne générale pondérée d'un étudiant.
     * Uniquement sur les matières ayant au moins une note.
     * Retourne null si aucune matière n'a de note.
     */
    fun calculerMoyenneGenerale(
        notes: List<Note>,
        matieres: List<Matiere>
    ): Double? {
        var sommePonderee = 0.0
        var sommeCoefficients = 0.0

        val notesParMatiere = notes.groupBy { it.matiereId }

        matieres.forEach { matiere ->
            val notesMatiere = notesParMatiere[matiere.id] ?: emptyList()
            val moyMatiere = calculerMoyenneMatiere(notesMatiere)
            if (moyMatiere != null) {
                sommePonderee += moyMatiere * matiere.coefficient
                sommeCoefficients += matiere.coefficient
            }
        }

        if (sommeCoefficients == 0.0) return null
        return roundToTwoDecimals(sommePonderee / sommeCoefficients)
    }

    /**
     * Détermine la mention ("Admis" ou "Ajourné") en fonction de la moyenne générale.
     */
    fun obtenirMention(moyenneGenerale: Double?): String {
        if (moyenneGenerale == null) return "Non évalué"
        return if (moyenneGenerale >= 10.0) "Admis" else "Ajourné"
    }

    /**
     * Arrondit une valeur à 2 décimales.
     */
    fun roundToTwoDecimals(valeur: Double): Double {
        return round(valeur * 100.0) / 100.0
    }
}
