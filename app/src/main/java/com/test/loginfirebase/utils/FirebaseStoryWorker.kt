package com.test.loginfirebase.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase

class FirebaseStoryWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId") ?: return Result.failure()
        val storyId = inputData.getString("storyId") ?: return Result.failure()

        return try {
            // Delete story from Firebase
            val databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users/$userId/story/$storyId")
            databaseReference.removeValue()
            Log.d("StoryDeletionWorker", " from worker Story deleted successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("StoryDeletionWorker", " from workerFailed to delete story: ${e.message}")
            Result.failure()
        }
    }
}