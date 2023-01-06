package com.shayo.moviespoint.firestore

import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface FirestoreFavoritesDataSource {
    suspend fun toggleFavorite(id: Int, type: String)

    val favoritesMapFlow: Flow<Map<Int, String>>

    fun setCollection(collectionName: String?)

    suspend fun getFavoritesIdsByCollection(collectionName: String): List<Int>
}

/* TODO: Add Di and all, Also add data class and use toObject of Firestore to map favorites.
 *  Use the new suspending and flow operations of Firestore.
 */
class FirestoreFavoritesDataSourceImpl : FirestoreFavoritesDataSource {

    private val scope = CoroutineScope(SupervisorJob())
    private val db = Firebase.firestore

    override suspend fun toggleFavorite(id: Int, type: String) {
        collectionName.value?.let { currentNAme ->
            val result = db.collection(currentNAme).whereEqualTo("id", id)
                .get().await()

            if (result.isEmpty) {
                db.collection(currentNAme).document("$id")
                    .set(hashMapOf("type" to type, "id" to id)).await()
            } else {
                db.collection(currentNAme).document("$id").delete().await()
            }
        }
    }

    private var collectionName = MutableStateFlow<String?>(null)

    override fun setCollection(collectionName: String?) {
        this.collectionName.value = collectionName
    }

    override suspend fun getFavoritesIdsByCollection(collectionName: String) =
        suspendCoroutine { cont ->
            db.collection(collectionName).get(Source.CACHE).addOnSuccessListener {
                cont.resume(
                    it.documents.map {
                        it.id.toInt()
                    }
                )
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val favoritesMapFlow =
        collectionName.flatMapLatest { currentCollection ->
            currentCollection?.let {
                callbackFlow {
                    trySend(emptyMap())

                    val registeredObject = db.collection(currentCollection)
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

                    awaitClose {
                        registeredObject.remove()
                    }
                }
            } ?: emptyFlow<Map<Int, String>>()
        }.shareIn(scope, SharingStarted.WhileSubscribed(1_5000), 1)
}