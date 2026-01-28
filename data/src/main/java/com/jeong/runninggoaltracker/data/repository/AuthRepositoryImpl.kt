package com.jeong.runninggoaltracker.data.repository

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.kakao.sdk.auth.AuthCodeClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.jeong.runninggoaltracker.data.contract.FirestorePaths
import com.jeong.runninggoaltracker.data.contract.UserFirestoreFields
import com.jeong.runninggoaltracker.data.contract.UsernameFirestoreFields
import com.jeong.runninggoaltracker.domain.model.AuthError
import com.jeong.runninggoaltracker.domain.model.AuthResult
import com.jeong.runninggoaltracker.domain.repository.AuthRepository
import com.jeong.runninggoaltracker.domain.util.NicknameNormalizer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import dagger.hilt.android.qualifiers.ApplicationContext

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val runningDatabase: com.jeong.runninggoaltracker.data.local.RunningDatabase
) : AuthRepository {
    override suspend fun signInAnonymously(): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInAnonymously()
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { error ->
                    continuation.resume(Result.failure(error))
                }
        }

    override suspend fun signInWithKakao(): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    continuation.resume(Result.failure(error))
                } else if (token != null) {
                    continuation.resume(Result.success(token.accessToken))
                } else {
                    continuation.resume(Result.failure(IllegalStateException()))
                }
            }
            val client = UserApiClient.instance
            if (client.isKakaoTalkLoginAvailable(context)) {
                client.loginWithKakaoTalk(context, AuthCodeClient.DEFAULT_REQUEST_CODE, callback = callback)
            } else {
                client.loginWithKakaoAccount(context, null, callback = callback)
            }
        }

    override suspend fun reserveNicknameAndCreateUserProfile(nickname: String): AuthResult<Unit> {
        val user = firebaseAuth.currentUser ?: return AuthResult.Failure(AuthError.PermissionDenied)
        val uid = user.uid
        val normalizedNickname = NicknameNormalizer.normalize(nickname)
        return try {
            val userDocRef = firestore.collection(FirestorePaths.COLLECTION_USERS).document(uid)
            val usernameDocRef =
                firestore.collection(FirestorePaths.COLLECTION_USERNAMES)
                    .document(normalizedNickname)
            firestore.runTransaction { transaction ->
                val usernameSnapshot = transaction.get(usernameDocRef)
                if (usernameSnapshot.exists()) {
                    throw NicknameTakenException()
                }
                val now = Timestamp.now()
                val usernameData = mapOf(
                    UsernameFirestoreFields.UID to uid,
                    UsernameFirestoreFields.CREATED_AT to now,
                    UsernameFirestoreFields.LAST_ACTIVE_AT to now,
                    UsernameFirestoreFields.IS_ANONYMOUS to user.isAnonymous
                )
                val userData = mapOf(
                    UserFirestoreFields.UID to uid,
                    UserFirestoreFields.NICKNAME to nickname,
                    UserFirestoreFields.NORMALIZED_NICKNAME to normalizedNickname,
                    UserFirestoreFields.CREATED_AT to now,
                    UserFirestoreFields.LAST_ACTIVE_AT to now,
                    UserFirestoreFields.IS_ANONYMOUS to user.isAnonymous
                )
                transaction.set(usernameDocRef, usernameData)
                transaction.set(userDocRef, userData, SetOptions.merge())
            }.awaitResult()
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(nickname)
                .build()
            user.updateProfile(profileUpdate).awaitResult()
            AuthResult.Success(Unit)
        } catch (error: Exception) {
            AuthResult.Failure(error.toAuthError())
        }
    }

    override suspend fun checkNicknameAvailability(nickname: String): AuthResult<Boolean> {
        val normalizedNickname = NicknameNormalizer.normalize(nickname)
        return try {
            val snapshot = firestore.collection(FirestorePaths.COLLECTION_USERNAMES)
                .document(normalizedNickname)
                .get()
                .awaitResult()
            AuthResult.Success(!snapshot.exists())
        } catch (error: Exception) {
            AuthResult.Failure(error.toAuthError())
        }
    }

    override suspend fun deleteAccountAndReleaseNickname(): AuthResult<Unit> {
        val user = firebaseAuth.currentUser ?: return AuthResult.Failure(AuthError.PermissionDenied)
        val uid = user.uid
        return try {
            val userDocRef = firestore.collection(FirestorePaths.COLLECTION_USERS).document(uid)
            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userDocRef)
                val normalizedNickname =
                    userSnapshot.getString(UserFirestoreFields.NORMALIZED_NICKNAME)
                        ?: userSnapshot.getString(UserFirestoreFields.NICKNAME)
                            ?.let { NicknameNormalizer.normalize(it) }
                        ?: user.displayName?.let { NicknameNormalizer.normalize(it) }
                normalizedNickname?.let { nickname ->
                    val usernameDocRef =
                        firestore.collection(FirestorePaths.COLLECTION_USERNAMES).document(nickname)
                    val usernameSnapshot = transaction.get(usernameDocRef)
                    val usernameOwner = usernameSnapshot.getString(UsernameFirestoreFields.UID)
                    if (usernameSnapshot.exists() && usernameOwner == uid) {
                        transaction.delete(usernameDocRef)
                    }
                }
                if (userSnapshot.exists()) {
                    transaction.delete(userDocRef)
                }
            }.awaitResult()
            withContext(Dispatchers.IO) {
                runningDatabase.clearAllTables()
            }
            user.delete().awaitResult()
            AuthResult.Success(Unit)
        } catch (error: Exception) {
            AuthResult.Failure(error.toAuthError())
        }
    }

    override suspend fun upgradeAnonymousWithCustomToken(customToken: String): AuthResult<Unit> {
        val user = firebaseAuth.currentUser ?: return AuthResult.Failure(AuthError.PermissionDenied)
        val currentUid = user.uid
        return try {
            val authResult = firebaseAuth.signInWithCustomToken(customToken).awaitResult()
            val updatedUid = authResult.user?.uid
            if (updatedUid == null || updatedUid != currentUid) {
                AuthResult.Failure(AuthError.Unknown)
            } else {
                AuthResult.Success(Unit)
            }
        } catch (error: Exception) {
            AuthResult.Failure(error.toAuthError())
        }
    }

    override fun observeIsAnonymous(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.isAnonymous == true)
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(firebaseAuth.currentUser?.isAnonymous == true)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override fun observeUserNickname(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.displayName)
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(firebaseAuth.currentUser?.displayName)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    private fun Exception.toAuthError(): AuthError = when (this) {
        is NicknameTakenException -> AuthError.NicknameTaken
        is FirebaseNetworkException -> AuthError.NetworkError
        is FirebaseFirestoreException -> when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> AuthError.PermissionDenied
            FirebaseFirestoreException.Code.UNAVAILABLE -> AuthError.NetworkError
            else -> AuthError.Unknown
        }

        is FirebaseAuthException -> AuthError.PermissionDenied
        else -> AuthError.Unknown
    }

    private class NicknameTakenException : IllegalStateException()

    private suspend fun <T> Task<T>.awaitResult(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                continuation.resume(result)
            }
            addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }

}
