package com.example.user.firebaseapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage


const val REQUEST_CODE = 1
const val INTENT_TITLE = "Select image"

class MainActivity : AppCompatActivity() {

    private var bitmap: Bitmap? = null
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonPick: Button = findViewById(R.id.btn_pick)
        buttonPick.setOnClickListener { chooseImg() }
        val buttonRecognise: Button = findViewById(R.id.btn_recognise)
        buttonRecognise.setOnClickListener { if (bitmap != null) recognisePhoto(bitmap!!) }

        textView = findViewById(R.id.text)
    }

    private fun chooseImg() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, INTENT_TITLE), REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
            if (data != null && data.data != null)
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
    }

    private fun recognisePhoto(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val detector = FirebaseVision.getInstance().visionTextDetector

        detector.detectInImage(image)
            .addOnSuccessListener {
                var text = ""
                for (block in it.blocks)
                    for (line in block.lines)
                        for (element in line.elements)
                            text += element.text
                textView.text = text
            }
            .addOnFailureListener { }
    }


    private fun cloudRecognition(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val options = FirebaseVisionCloudDetectorOptions.Builder()
            .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
            .setMaxResults(15)
            .build()

        val detector = FirebaseVision.getInstance().visionCloudTextDetector

        detector.detectInImage(image)
            .addOnSuccessListener {
                Log.d("log", "success $it")
                val text = it.text
                textView.text = text
            }
            .addOnFailureListener {
                Log.d("log", "error ${it.message}")

            }
    }
}
