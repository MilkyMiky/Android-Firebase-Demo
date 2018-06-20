package com.example.user.firebaseapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Pair
import android.widget.Button
import android.widget.TextView
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.custom.*
import com.google.firebase.ml.custom.model.FirebaseCloudModelSource
import com.google.firebase.ml.custom.model.FirebaseModelDownloadConditions
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


/**
 * Dimensions of inputs.
 */

const val DIM_BATCH_SIZE = 1
const val DIM_PIXEL_SIZE = 3
const val DIM_IMG_SIZE_X = 224
const val DIM_IMG_SIZE_Y = 224

const val REQUEST_CODE = 1
const val RESULTS_TO_SHOW = 3
const val INTENT_TITLE = "Select image"
const val LABEL_PATH = "labels.txt"

class MainActivity : AppCompatActivity() {


    private val intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)

    private var mLabelList: List<String>? = null
//    private val sortedLabels = PriorityQueue<Map.Entry<String, Float>>(
//        RESULTS_TO_SHOW,
//        Comparator<Any> { o1, o2 -> o1.va
//            .compareTo(o2.value) })

    private lateinit var firebaseModelInterpreter: FirebaseModelInterpreter
    private lateinit var firebaseModelIOOptions: FirebaseModelInputOutputOptions
    private var bitmap: Bitmap? = null
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonPick: Button = findViewById(R.id.btn_pick)
        buttonPick.setOnClickListener { chooseImg() }
        val buttonRecognise: Button = findViewById(R.id.btn_recognise)
        buttonRecognise.setOnClickListener { if (bitmap != null) runModel(bitmap!!) }

        textView = findViewById(R.id.text)

        mLabelList = loadLabelList(this)
        loadModel()
        specifyIO()
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


    private fun loadModel() {
        var conditionsBuilder: FirebaseModelDownloadConditions.Builder =
            FirebaseModelDownloadConditions.Builder().requireWifi()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            conditionsBuilder = conditionsBuilder.requireCharging().requireDeviceIdle()
        val conditions = conditionsBuilder.build()

        // Build a FirebaseCloudModelSource object by specifying the name you assigned the model
        // when you uploaded it in the Firebase console.
        val cloudSource = FirebaseCloudModelSource.Builder("mobilenet")
            .enableModelUpdates(true)
            .setInitialDownloadConditions(conditions)
            .setUpdatesDownloadConditions(conditions)
            .build()

        FirebaseModelManager.getInstance().registerCloudModelSource(cloudSource)

//        val localSource = FirebaseLocalModelSource.Builder("animal-detector")
//            .setAssetFilePath("animal-detector.tflite")  // Or setFilePath if you downloaded from your host
//            .build()

//        FirebaseModelManager.getInstance().registerLocalModelSource(localSource)

        val options = FirebaseModelOptions.Builder()
            .setCloudModelName("mobilenet")
//            .setLocalModelName("animal-detector")
            .build()

        firebaseModelInterpreter = FirebaseModelInterpreter.getInstance(options)!!
    }

    private fun specifyIO() {
        val inputDims = intArrayOf(DIM_BATCH_SIZE, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, DIM_PIXEL_SIZE)
        val outputDims = intArrayOf(DIM_BATCH_SIZE, mLabelList!!.size)


//        val targetedSize = getTargetedWidthHeight()

//        val targetWidth = targetedSize.first
//        val maxHeight = targetedSize.second

        firebaseModelIOOptions = FirebaseModelInputOutputOptions.Builder()
            .setInputFormat(0, FirebaseModelDataType.BYTE, inputDims)
            .setOutputFormat(0, FirebaseModelDataType.BYTE, outputDims)
            .build()
    }

    private fun runModel(bitmap: Bitmap) {
        Log.d("log", "runModel")
//
//        val scaleFactor = Math.max(
//            bitmap.width.toFloat() / targetWidth.toFloat(),
//            bitmap.height.toFloat() / maxHeight.toFloat()
//        )




        val imgData = convertToByteBuffer(bitmap, bitmap.width, bitmap.height)

        try {
            val inputs = FirebaseModelInputs.Builder().add(imgData).build()
            // Here's where the magic happens!!
            firebaseModelInterpreter
                .run(inputs, firebaseModelIOOptions)
                .addOnFailureListener({ e ->
                    Log.d("log", "onFailure ${e.message}")
                    e.printStackTrace()
                })
                .continueWith(
                    { task ->
                        Log.d("log", "continue")
                        val labelProbArray = task.result.getOutput<Array<ByteArray>>(0)
                        getTopLabels(labelProbArray)

                    })
        } catch (e: FirebaseMLException) {
        }
    }

    private fun convertToByteBuffer(bitmap: Bitmap, width: Int, height: Int): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(
            DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE
        )
        imgData.order(ByteOrder.nativeOrder())
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y, true
        )
        imgData.rewind()
        scaledBitmap.getPixels(
            intValues, 0, scaledBitmap.width, 0, 0,
            scaledBitmap.width, scaledBitmap.height
        )
        // Convert the image to int points.
        var pixel = 0
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val value = intValues[pixel++]
                imgData.put((value shr 16 and 0xFF).toByte())
                imgData.put((value shr 8 and 0xFF).toByte())
                imgData.put((value and 0xFF).toByte())
            }
        }
        return imgData
    }

    private fun getTopLabels(labelProbArray: Array<ByteArray>): List<String> {
        Log.d("log", "getTopLabels")
        for (i in mLabelList!!.indices) {
//            sortedLabels.add(
//                AbstractMap.SimpleEntry<String, Float>(
//                    mLabelList.get(i),
//                    (labelProbArray[0][i] and 0xff) / 255.0f
//                )
//            )
//            if (sortedLabels.size > RESULTS_TO_SHOW) {
//                sortedLabels.poll()
//            }
        }
        val result = ArrayList<String>()
//        val size = sortedLabels.size
//        for (i in 0 until size) {
//            val label = sortedLabels.poll()
//            result.add(label.key + ":" + label.value)
//        }
//        Log.d(TAG, "labels: " + result.toString())
        return result
    }

    private fun loadLabelList(activity: Activity): List<String> {
        val labelList = ArrayList<String>()
        try {
            BufferedReader(InputStreamReader(activity.assets.open(LABEL_PATH))).use { reader ->
                var line = reader.readLine()
                while (reader.readLine() != null) {
                    labelList.add(line)
                }
            }
        } catch (e: IOException) {
            Log.e("log", "Failed to read label list.", e)
        }
        return labelList
    }


    // Gets the targeted width / height.
//    private fun getTargetedWidthHeight(): Pair<Int, Int> {
//        val targetWidth: Int
//        val targetHeight: Int
//        val maxWidthForPortraitMode = getImageMaxWidth()!!
//        val maxHeightForPortraitMode = getImageMaxHeight()!!
//        targetWidth = maxWidthForPortraitMode!!
//        targetHeight = maxHeightForPortraitMode!!
//        return Pair(targetWidth, targetHeight)
//    }

    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.


}
