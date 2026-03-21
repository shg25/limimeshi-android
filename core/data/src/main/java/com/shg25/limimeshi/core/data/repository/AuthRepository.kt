package com.shg25.limimeshi.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.shg25.limimeshi.core.model.AuthUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.let {
            AuthUser(uid = it.uid, displayName = it.displayName, email = it.email)
        }

    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    val isLoggedIn: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogleIdToken(idToken: String): AuthUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val user = result.user ?: throw IllegalStateException("Firebase user is null after sign-in")
        return AuthUser(uid = user.uid, displayName = user.displayName, email = user.email)
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
