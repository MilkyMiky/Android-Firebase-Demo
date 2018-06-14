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
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    lateinit var firebaseStorage: FirebaseStorage

    private var imgPath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initCloudStorage()

        val buttonPick: Button = findViewById(R.id.btn_pick)
        buttonPick.setOnClickListener { chooseImg() }

        val buttonUpload: Button = findViewById(R.id.btn_upload)
        buttonUpload.setOnClickListener { if (imgPath != null) uploadPhoto(imgPath!!) }
    }

    private fun chooseImg() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select image"), 1)
    }

    private fun initCloudStorage() {
        firebaseStorage = FirebaseStorage.getInstance()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                imgPath = data.data
            }
        }
    }

    private fun uploadPhoto(fileUri: Uri) {
        val ref: StorageReference =
            firebaseStorage.reference.child("img/${fileUri.lastPathSegment}")


        val mime = StorageMetadata.Builder()
            .setCustomMetadata("customMeta", "fileMetavalue")
            .build()

        val task = ref.putFile(fileUri, mime)
        task.addOnProgressListener {
            //            OnProgressListener

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
}
