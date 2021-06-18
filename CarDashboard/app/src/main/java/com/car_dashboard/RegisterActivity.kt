package com.car_dashboard

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.chaquo.python.Python
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*


class RegisterActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var cameraId = 0
    lateinit var t2s : TextToSpeech

    fun drawable2Bytes(): ByteArray? {
        val bitmap = drawable2Bitmap()
        return bitmap2Bytes(bitmap)
    }

    fun drawable2Bitmap(): Bitmap {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.profile)
        return bitmap
    }

    fun bitmap2Bytes(bm: Bitmap): ByteArray? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        startCamera()

        val progressBar2 :ProgressBar = this.findViewById(R.id.progressBar2)
        val camera_preview2 :FrameLayout = this.findViewById(R.id.camera_preview2)
        val button4 :Button = this.findViewById(R.id.button4)


        val handler = Handler()
        val mPicture = Camera.PictureCallback { data, _ ->
            Thread(Runnable {
                try {
                    val python = Python.getInstance()
                    val pythonFile = python.getModule("main")
                    val storeFace = pythonFile.callAttr("store_face", Values.myID, Values.myName, Values.myPIN, data, "True", drawable2Bytes())
                    if (storeFace.toBoolean()) {
                        handler.post {
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            t2s.stop()
                            t2s.shutdown()

                            releaseCamera()
                            startActivity(intent)
                        }
                    } else {
                        handler.post {
                            releaseCamera()
                            startCamera()
                            progressBar2.visibility = View.INVISIBLE
                            camera_preview2.visibility = View.VISIBLE
                            button4.visibility = View.VISIBLE
                            Toast.makeText(this,"Make sure exactly one face is visible", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: FileNotFoundException) {
                    Log.d(ContentValues.TAG, "File not found: ${e.message}")
                } catch (e: IOException) {
                    Log.d(ContentValues.TAG, "Error accessing file: ${e.message}")
                }
            }).start()
        }

        val captureButton: Button = findViewById(R.id.button4)
        captureButton.setOnClickListener {
            Thread(Runnable {
                mCamera?.takePicture(null, null, mPicture)
            }).start()
            progressBar2.visibility = View.VISIBLE
            camera_preview2.visibility = View.INVISIBLE
            button4.visibility = View.INVISIBLE
        }

        this.findViewById<Button>(R.id.button7).setOnClickListener {
            skipFace()
        }

        t2s = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                t2s.language = Locale.UK;
            }
        })
        t2s.setSpeechRate(0.8F)
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        t2s.speak("Please show your face to the camera. And click register to add your face id", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun skipFace() {
        val python = Python.getInstance()
        val pythonFile = python.getModule("main")
        val storeFace = pythonFile.callAttr("store_without_face", Values.myID, Values.myName, Values.myPIN, "True", drawable2Bytes())
        val handler = Handler()
        if (storeFace.toBoolean()) {
            handler.post {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)

                releaseCamera()
                startActivity(intent)
            }
        } else {
            handler.post {
                Toast.makeText(this,"An Error Occurred, Try Again", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun startCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), 1);
        }

        mCamera = getCameraInstance()
        mCamera!!.setDisplayOrientation(0)
        mPreview = mCamera?.let {
            CameraPreview(this, it)
        }

        mPreview?.also {
            val preview: FrameLayout = findViewById(R.id.camera_preview2)
            preview.addView(it)
        }
    }

    private fun findFrontFacingCamera(): Int {
        var cameraId = -1
        val numberOfCameras = Camera.getNumberOfCameras()
        for (i in 0 until numberOfCameras) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i
                break
            }
        }
        return cameraId
    }

    fun getCameraInstance(): Camera? {
        return try {
            cameraId = findFrontFacingCamera();
            Camera.open(cameraId)
        } catch (e: Exception) {
            null
        }
    }

    override fun onPause() {
        super.onPause()
        releaseCamera()
    }

    override fun onResume() {
        super.onResume()
        startCamera()
    }

    override fun onRestart() {
        super.onRestart()
        startCamera()
    }

    private fun releaseCamera() {
        mCamera?.release()
        mCamera = null
    }

    override fun onBackPressed() {
        if(Values.isFirst) {
            val handler = Handler()
            handler.post {
                val intent = Intent(this, SignUpActivity::class.java)

                startActivity(intent)
            }
        }
    }

}