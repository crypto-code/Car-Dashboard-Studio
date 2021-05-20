package com.example.cardashboard

import android.content.ContentValues.TAG
import android.content.Intent
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class LoginActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var cameraId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Create an instance of Camera
        startCamera()

        val handler = Handler()
        val mPicture = Camera.PictureCallback { data, _ ->
            Thread(Runnable {
                try {
                    val python = Python.getInstance()
                    val pythonFile = python.getModule("face")
                    val checkFace = pythonFile.callAttr("check_match", data)
                    handler.post(object : Runnable {
                        override fun run() {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent)
                        }
                    })
                    Log.d("Car Dashboard", "file written")
                } catch (e: FileNotFoundException) {
                    Log.d(TAG, "File not found: ${e.message}")
                } catch (e: IOException) {
                    Log.d(TAG, "Error accessing file: ${e.message}")
                } finally {
                    handler.post(object : Runnable {
                        override fun run() {
                            releaseCamera()
                            startCamera()
                            progressBar.visibility = View.INVISIBLE
                            camera_preview.visibility = View.VISIBLE
                            button3.visibility = View.VISIBLE
                            textView3.setText("")
                        }
                    })
                }
            }).start()
        }

        val captureButton: Button = findViewById(R.id.button3)
        captureButton.setOnClickListener {
            // get an image from the camera
            progressBar.visibility = View.VISIBLE
            camera_preview.visibility = View.INVISIBLE
            button3.visibility = View.INVISIBLE
            textView3.text = "Processing......"
            Thread(Runnable {
                mCamera?.takePicture(null, null, mPicture)
            }).start()
        }
    }

    private fun startCamera() {
        mCamera = getCameraInstance()
        mCamera?.setDisplayOrientation(90);
        mPreview = mCamera?.let {
            // Create our Preview view
            CameraPreview(this, it)
        }

        // Set the Preview view as the content of our activity.
        mPreview?.also {
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(it)
        }
    }

    override fun onPause() {
        super.onPause()
        releaseCamera() // release the camera immediately on pause event
    }

    private fun releaseCamera() {
        mCamera?.release() // release the camera for other applications
        mCamera = null
    }

    private fun findFrontFacingCamera(): Int {
        var cameraId = -1
        // Search for the front facing camera
        val numberOfCameras = Camera.getNumberOfCameras()
        for (i in 0 until numberOfCameras) {
            val info = CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i
                break
            }
        }
        return cameraId
    }

    fun getCameraInstance(): Camera? {
        return try {
            cameraId = findFrontFacingCamera();
            Camera.open(cameraId) // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }


    val MEDIA_TYPE_IMAGE = 1
    val MEDIA_TYPE_VIDEO = 2

    /** Create a file Uri for saving an image or video */
    private fun getOutputMediaFileUri(type: Int): Uri {
        return Uri.fromFile(getOutputMediaFile(type))
    }

    /** Create a File for saving an image or video */
    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Car Dashboard"
        )
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    Log.d("Car Dashboard", "failed to create directory")
                    return null
                }
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat.getDateTimeInstance().format(Date())
        return when (type) {
            MEDIA_TYPE_IMAGE -> {
                File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
            }
            MEDIA_TYPE_VIDEO -> {
                File("${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4")
            }
            else -> null
        }
    }

    override fun onBackPressed() {

    }

}