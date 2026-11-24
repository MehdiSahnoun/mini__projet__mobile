package com.example.gradebook.data.repository

import com.example.gradebook.data.firebase.FirebaseRefs
import com.example.gradebook.data.model.Note
import com.example.gradebook.data.model.TypeNote
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NoteRepository {

    private val notesRef = FirebaseRefs.notesRef

    fun getNotesFlow(): Flow<List<Note>> = callbackFlow {
        val listener = notesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                parseNote(doc)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    fun getNotesForEtudiantFlow(etudiantId: String): Flow<List<Note>> = callbackFlow {
        val listener = notesRef.whereEqualTo("etudiantId", etudiantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    parseNote(doc)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addNote(note: Note): Result<String> {
        return try {
            val docRef = notesRef.add(note.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: Note): Result<Unit> {
        return try {
            notesRef.document(note.id).set(note.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            notesRef.document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseNote(doc: com.google.firebase.firestore.DocumentSnapshot): Note {
        val typeStr = doc.getString("type") ?: TypeNote.DS.name
        val type = try { TypeNote.valueOf(typeStr) } catch (e: Exception) { TypeNote.DS }
        return Note(
            id = doc.id,
            etudiantId = doc.getString("etudiantId") ?: "",
            matiereId = doc.getString("matiereId") ?: "",
            type = type,
            valeur = doc.getDouble("valeur") ?: 0.0,
            date = doc.getTimestamp("date") ?: Timestamp.now()
        )
    }
}
