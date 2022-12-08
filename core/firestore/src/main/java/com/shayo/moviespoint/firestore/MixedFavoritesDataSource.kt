package com.shayo.moviespoint.firestore

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

interface MixedFavoritesDataSource {
    fun toggleFavorite(id: Int, type: String)

    val favoritesMapFlow: Flow<Map<Int, String>>
}

// TODO: Add Di and all
class MixedFavoritesDataSourceImpl: MixedFavoritesDataSource {

    private val scope = CoroutineScope(SupervisorJob())
    private val db = Firebase.firestore

    override fun toggleFavorite(id: Int, type: String) {
        db.collection("shays_favs").whereEqualTo("id", id)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    db.collection("shays_favs").document("$id").set(hashMapOf("type" to type, "id" to id))
                } else {
                    db.collection("shays_favs").document("$id").delete()
                }
            }
    }

    override val favoritesMapFlow = callbackFlow {
        trySend(emptyMap())

        val registeredObject = db.collection("shays_favs")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    close(e)
                }

                val favs = mutableMapOf<Int, String>()

                for (doc in value!!) {
                    favs[doc.id.toInt()] = doc.getString("type")!! // TODO:
                }

                trySend(favs)
            }

        awaitClose{
            registeredObject.remove()
        }
    }.shareIn(scope, SharingStarted.WhileSubscribed(1_5000), 1)
}