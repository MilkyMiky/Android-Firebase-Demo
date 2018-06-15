package com.example.user.firebaseapp


import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseFunctions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseFunctions = FirebaseFunctions.getInstance()
        helloWorld()
        addMessage()
    }

    private fun helloWorld(): Task<String> {
        return firebaseFunctions
            .getHttpsCallable("helloWorld")
            .call()
            .continueWith {
                val result = it.result.data as Map<String, Any>
                result["message"] as String
            }
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "OnComplete ${it.result}", Toast.LENGTH_SHORT).show()
                    Log.d("log", "OnComplete ${it.result}")
                }
            }
    }

    private fun addMessage(): Task<String> {
        val data = HashMap<String, Any>()
        data["text"] = "Hi, How are you?"
        data["author"] = "Jack"

        return firebaseFunctions
            .getHttpsCallable("addMessage")
            .call(data)
            .continueWith {
                val result = it.result.data as Map<String, Any>
                result["message"] as String
            }
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "OnComplete ${it.result}", Toast.LENGTH_SHORT).show()
                    Log.d("log", "OnComplete ${it.result}")
                }
            }
    }
}


