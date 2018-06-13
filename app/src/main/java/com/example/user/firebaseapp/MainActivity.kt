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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseDatabase: FirebaseDatabase

    private var adapter: MessageAdapter = MessageAdapter()
    private var message: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setRealtimeListener()

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
    }

    private fun setRealtimeListener() {
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase.reference.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    setAdapterData(dataSnapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("log", "onCancelled")
                }
            }
        )
    }

    private fun setAdapterData(dataSnapshot: DataSnapshot) {
        val list: ArrayList<Message> = ArrayList()
        for (data in dataSnapshot.child("messages").children) {
            val message = data.getValue(Message::class.java)
            if (message != null) list.add(message)
        }
        adapter.messageList = list
        adapter.notifyDataSetChanged()
    }

    private fun saveMessage(messageText: String) {
        val databaseReference = firebaseDatabase.reference
        val message = Message("user", messageText)

        databaseReference
            .child("messages")
            .child(UUID.randomUUID().toString())
            .setValue(message.createMessageObj())
    }
}
