package com.teamcastor.haazir

import android.util.Log
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.teamcastor.haazir.data.Attendance
import com.teamcastor.haazir.data.User
import com.teamcastor.haazir.data.model.AppViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class FirebaseRepository(
    private val firebaseDb: DatabaseReference = db,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    companion object {
        val db = Firebase.database.reference
        val today = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000)
        val month = MaterialDatePicker.thisMonthInUtcMilliseconds()
    }

    val latestUserInfo: Flow<User?> = callbackFlow {
        val userDetailPath = Firebase.auth.uid?.let { firebaseDb.child("users").child(it) }
        val listener = userDetailPath?.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    trySend(dataSnapshot.getValue<User>())
                        .onSuccess {
                            Log.i("trySend", "succesfully notified user info change")
                        }
                        .onFailure {
                            Log.w("trySend", "failed to notify user info change")
                        }
                        .onClosed {
                            Log.w("trySend", "cancelled notifying user info change")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(AppViewModel.TAG, "lastestUserInfo:onCancelled")
                }
            })
        awaitClose {
            if (listener != null) {
                userDetailPath.removeEventListener(listener)
            }
        }

    }
        .flowOn(ioDispatcher)

    val todayAttendance: Flow<Attendance?> = callbackFlow {
        val todayAttendancePath = Firebase.auth.uid?.let {
            firebaseDb.child("attendance").child(it).child(
                today.toString()
            )
        }
        val listener = todayAttendancePath?.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    trySend(dataSnapshot.getValue<Attendance>())
                        .onSuccess {
                            Log.i("trySend", "succesfully notified attendance")
                        }
                        .onFailure {
                            Log.w("trySend", "failed to notify attendance")
                        }
                        .onClosed {
                            Log.w("trySend", "cancelled notifying attendance")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(AppViewModel.TAG, "lastestUserInfo:onCancelled", error.toException())
                }
            })
        awaitClose {
            if (listener != null) {
                todayAttendancePath.removeEventListener(listener)
            }
        }
    }
        .flowOn(ioDispatcher)

    fun markAttendance(event: String?) {
        try {
            Firebase.auth.uid?.let { path ->
                event?.let { ev ->
                    db.child("attendance").child(path)
                        .child(today.toString())
                        .child(ev)
                        .setValue(ServerValue.TIMESTAMP)
                        .addOnFailureListener {
                            Log.w("ma", "failed", it)
                        }
                }
            }
        } catch (e: Exception) {
            Log.w("markattendance", "error", e)
        }
    }


        val attendanceHistory: Flow<Map<String, Attendance>?> = callbackFlow {
            val attendanceHistoryPath =
                Firebase.auth.uid?.let { firebaseDb.child("attendance").child(it) }
            val listener =
                attendanceHistoryPath?.orderByKey()
                    ?.addValueEventListener(
                        object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                trySend(dataSnapshot.getValue<Map<String, Attendance>>())
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.w("", "loadPost:onCancelled", databaseError.toException())
                            }
                        })
            awaitClose {
                if (listener != null) {
                    attendanceHistoryPath.removeEventListener(listener)
                }
            }
        }


//    val isConnected: Flow<Boolean> = callbackFlow {
//        val connectedRef = Firebase.database.getReference(".info/connected")
//        val listener = connectedRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                snapshot.getValue<Boolean>()?.let { trySend(it) }
//            }
//            override fun onCancelled(error: DatabaseError) {
//                Log.w("getConnectionState", "Listener was cancelled")
//            }
//        })
//        awaitClose {
//            connectedRef.removeEventListener(listener)
//        }
//    }
//        .flowOn(ioDispatcher)
}