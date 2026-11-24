package com.example.gradebook.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseRefs {
    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    val usersRef: CollectionReference get() = db.collection("users")
    val etudiantsRef: CollectionReference get() = db.collection("etudiants")
    val matieresRef: CollectionReference get() = db.collection("matieres")
    val notesRef: CollectionReference get() = db.collection("notes")
}
