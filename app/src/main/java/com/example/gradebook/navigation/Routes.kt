package com.example.gradebook.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
    const val ETUDIANTS = "etudiants"
    const val AJOUT_ETUDIANT = "ajout_etudiant?etudiantId={etudiantId}&nom={nom}&prenom={prenom}&num={num}&classe={classe}&uid={uid}"

    fun createAjoutEtudiantRoute(
        etudiantId: String? = null,
        nom: String = "",
        prenom: String = "",
        num: String = "",
        classe: String = "",
        uid: String = ""
    ): String {
        return "ajout_etudiant?etudiantId=${etudiantId ?: ""}&nom=$nom&prenom=$prenom&num=$num&classe=$classe&uid=$uid"
    }

    const val MATIERES = "matieres"
    const val NOTES = "notes"
    const val RELEVE = "releve"
    const val PROFIL = "profil"
}
