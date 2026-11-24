package com.example.gradebook

import com.example.gradebook.data.model.Matiere
import com.example.gradebook.data.model.Note
import com.example.gradebook.data.model.TypeNote
import com.example.gradebook.util.MoyenneCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoyenneCalculatorTest {

    @Test
    fun testCalculMoyenneMatiereSimple() {
        val notes = listOf(
            Note(id = "1", etudiantId = "e1", matiereId = "m1", type = TypeNote.DS, valeur = 14.0),
            Note(id = "2", etudiantId = "e1", matiereId = "m1", type = TypeNote.EXAMEN, valeur = 16.0)
        )
        val moyenne = MoyenneCalculator.calculerMoyenneMatiere(notes)
        assertEquals(15.0, moyenne!!, 0.01)
    }

    @Test
    fun testCalculMoyenneMatiereVide() {
        val moyenne = MoyenneCalculator.calculerMoyenneMatiere(emptyList())
        assertNull(moyenne)
    }

    @Test
    fun testCalculMoyenneGeneralePonderee() {
        val matieres = listOf(
            Matiere(id = "m1", nom = "Maths", coefficient = 3.0),
            Matiere(id = "m2", nom = "Physique", coefficient = 2.0)
        )
        val notes = listOf(
            Note(id = "1", etudiantId = "e1", matiereId = "m1", type = TypeNote.DS, valeur = 10.0),
            Note(id = "2", etudiantId = "e1", matiereId = "m2", type = TypeNote.DS, valeur = 15.0)
        )
        val moyGen = MoyenneCalculator.calculerMoyenneGenerale(notes, matieres)
        assertEquals(12.0, moyGen!!, 0.01)
    }

    @Test
    fun testMentionAdmisEtAjourne() {
        assertEquals("Admis", MoyenneCalculator.obtenirMention(10.0))
        assertEquals("Admis", MoyenneCalculator.obtenirMention(14.5))
        assertEquals("Ajourné", MoyenneCalculator.obtenirMention(9.99))
        assertEquals("Non évalué", MoyenneCalculator.obtenirMention(null))
    }
}
