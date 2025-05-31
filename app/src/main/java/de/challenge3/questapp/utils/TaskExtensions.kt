package de.challenge3.questapp.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.coroutines.resumeWithException

/**
 * Wandelt Google Task in Kotlin Coroutine um
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result) {}
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        continuation.invokeOnCancellation {
            // When coroutine gets aborted, don't take other Task into account anymore
            if (isComplete) return@invokeOnCancellation
            isCanceled
        }
    }
}
