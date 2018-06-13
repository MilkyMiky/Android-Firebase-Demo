package com.example.user.firebaseapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseFirestore: FirebaseFirestore

    private var adapter: MessageAdapter = MessageAdapter()
    private var message: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setFirestoreListener()

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)

        val button: Button = findViewById(R.id.btn_send)
        button.setOnClickListener { if (!message.isEmpty()) saveMessage(message) }

        val editText: EditText = findViewById(R.id.edit_text)
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                message = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        getData()
    }

    private fun setFirestoreListener() {

        firebaseFirestore = FirebaseFirestore.getInstance()
        val collectionReference = firebaseFirestore.collection("messages")
        collectionReference
            .orderBy("date")
            .addSnapshotListener(
                { querySnapshot, firebaseFirestoreException ->
                    if (querySnapshot != null) setAdapterData(querySnapshot)
                }
            )
    }

    private fun getData() {
        val collectionReference = firebaseFirestore.collection("messages")
        collectionReference
            .orderBy("date")
            .get()
            .addOnCompleteListener {
                setAdapterData(it.result)
            }
    }

    private fun setAdapterData(querySnapshot: QuerySnapshot) {
        val list: ArrayList<Message> = ArrayList()
        for (documentSnapshot in querySnapshot)
            list.add(Message(documentSnapshot.data as Map<String, Object>))
        adapter.messageList = list
        adapter.notifyDataSetChanged()
    }

    private fun saveMessage(messageText: String) {
        val message = Message("user", messageText, Date())
        firebaseFirestore.collection("messages")
            .add(message.createMessageObj())
            .addOnSuccessListener {
                Log.d("log", "onSuccess")
            }
            .addOnFailureListener {
                Log.d("log", "onFailure")
            }
    }
}
