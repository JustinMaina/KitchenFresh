package com.example.foodbanklocator

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
class FirestoreRepository {

    // Firestore instance â€” automatically uses google-services.json config
    private val db = FirebaseFirestore.getInstance()

    // The Firestore collection that stores all food banks
    private val collection = db.collection("food_banks")

    /**
     * Fetches all food banks once from Firestore.
     */
    suspend fun getAllFoodBanks(): List<FoodBank> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { FoodBank.fromMap(doc.id, it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    fun foodBanksFlow(): Flow<List<FoodBank>> = callbackFlow {
        val registration: ListenerRegistration = collection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Closes the flow on error
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { FoodBank.fromMap(doc.id, it) }
                } ?: emptyList()
                trySend(list) // Emit the updated list
            }
        // Cancel the Firestore listener when the flow is no longer collected
        awaitClose { registration.remove() }
    }

    // ------------------------------------------------------------------
    // CREATE â€” add a new food bank
    // ------------------------------------------------------------------

    /**
     * Adds a new food bank to Firestore.
     * Firestore auto-generates the document ID.
     * Returns the new document ID on success, or null on failure.
     */
    suspend fun addFoodBank(foodBank: FoodBank): String? {
        return try {
            val docRef = collection.add(foodBank.toMap()).await()
            docRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ------------------------------------------------------------------
    // UPDATE â€” change the name (and optionally other fields)
    // ------------------------------------------------------------------

    /**
     * Updates specific fields of an existing food bank document.
     * Only the fields you pass in [updates] will change â€” others stay untouched.
     *
     * Example:
     *   updateFoodBank(id, mapOf("name" to "New Name", "phone" to "0712345678"))
     */
    suspend fun updateFoodBank(documentId: String, updates: Map<String, Any>): Boolean {
        return try {
            collection.document(documentId).update(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Convenience shortcut â€” update just the name.
     */
    suspend fun updateFoodBankName(documentId: String, newName: String): Boolean {
        return updateFoodBank(documentId, mapOf("name" to newName))
    }

    // ------------------------------------------------------------------
    // DELETE â€” remove a food bank
    // ------------------------------------------------------------------

    /**
     * Permanently deletes a food bank document from Firestore.
     * Returns true on success, false on failure.
     */
    suspend fun deleteFoodBank(documentId: String): Boolean {
        return try {
            collection.document(documentId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ------------------------------------------------------------------
    // SEED â€” populate Firestore with initial Nairobi data (run once)
    // ------------------------------------------------------------------

    /**
     * Seeds the Firestore collection with sample food banks.
     * Call this ONCE (e.g. only when the collection is empty) to avoid duplicates.
     *
     * You can trigger this from MainActivity on first launch:
     *   if (repository.getAllFoodBanks().isEmpty()) repository.seedInitialData()
     */
    suspend fun seedInitialData() {
        val sampleData = listOf(
            FoodBank(
                name = "Community Food Bank A",
                latitude = -1.2921, longitude = 36.8219,
                address = "Nairobi CBD", phone = "0700000001",
                openingHours = "Monâ€“Fri 08:00â€“17:00",
                items = listOf("flour", "sugar", "salt", "rice")
            ),
            FoodBank(
                name = "Community Food Bank B",
                latitude = -1.3032, longitude = 36.8070,
                address = "Lang'ata Road", phone = "0700000002",
                openingHours = "Monâ€“Sat 07:00â€“16:00",
                items = listOf("maize", "beans", "cooking oil")
            ),
            FoodBank(
                name = "Eastlands Support Centre",
                latitude = -1.2841, longitude = 36.8890,
                address = "Eastlands, Nairobi", phone = "0700000003",
                openingHours = "Tueâ€“Sun 09:00â€“15:00",
                items = listOf("bread", "milk", "sugar", "salt")
            ),
            FoodBank(
                name = "Westlands Relief Hub",
                latitude = -1.2676, longitude = 36.8108,
                address = "Westlands, Nairobi", phone = "0700000004",
                openingHours = "Monâ€“Fri 08:30â€“16:30",
                items = listOf("flour", "grain", "lentils", "rice")
            )
        )
        sampleData.forEach { addFoodBank(it) }
    }
}
