package com.example.user.firebaseapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.File

const val REQUEST_CODE = 1
const val INTENT_TITLE = "Select image"

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseStorage: FirebaseStorage

    private var imgUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseStorage = FirebaseStorage.getInstance()

        val buttonPick: Button = findViewById(R.id.btn_pick)
        buttonPick.setOnClickListener { chooseImg() }

        val buttonUpload: Button = findViewById(R.id.btn_upload)
        buttonUpload.setOnClickListener { if (imgUri != null) uploadPhoto(imgUri!!) }

        val buttonDownload: Button = findViewById(R.id.btn_download)
        buttonDownload.setOnClickListener { deleteFile() }
    }

    private fun chooseImg() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, INTENT_TITLE), REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                imgUri = data.data
            }
        }
    }

    private fun uploadPhoto(fileUri: Uri) {
        val ref = firebaseStorage.reference.child("img/${fileUri.lastPathSegment}")
        val metadata = StorageMetadata.Builder()
            .setCustomMetadata("customMeta", "customMetavalue")
            .build()

        val task = ref.putFile(fileUri, metadata)
        task.addOnProgressListener {
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            Toast.makeText(this, "Upload progress ${progress} %", Toast.LENGTH_SHORT).show()
        }
        task.addOnFailureListener {
            Toast.makeText(this, "OnFailure ${it.message}", Toast.LENGTH_SHORT).show()
        }
        task.addOnCompleteListener {
            Toast.makeText(this, "OnComplete", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadFile() {
        val file = File.createTempFile("img","png")
        firebaseStorage.reference.child("img/image:38807")
            .getFile(file)
            .addOnSuccessListener {
                Toast.makeText(this, "OnSuccess", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteFile() {
        firebaseStorage.reference.child("img/image:38807")
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "OnSuccess", Toast.LENGTH_SHORT).show()
            }
    }
}
