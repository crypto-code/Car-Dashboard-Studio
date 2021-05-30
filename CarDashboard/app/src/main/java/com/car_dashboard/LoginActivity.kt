package com.car_dashboard

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var cameraId = 0
    lateinit var t2s :TextToSpeech
    var checkPresent :Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        startCamera()

        val progressBar3 : ProgressBar = this.findViewById(R.id.progressBar3)
        val camera_preview :FrameLayout = this.findViewById(R.id.camera_preview)
        val button3 :Button = this.findViewById(R.id.button3)

        val python = Python.getInstance()
        val pythonFile = python.getModule("main")
        checkPresent = pythonFile.callAttr("check_if_encoding", Values.myID).toBoolean()

        if (checkPresent) {
            val handler = Handler()
            val mPicture = Camera.PictureCallback { data, _ ->
                Thread(Runnable {
                    try {
                        val checkFace = pythonFile.callAttr("check_face", Values.myID, data)
                        if (!checkFace.toString().isEmpty()) {
                            Values.myName = checkFace.toString()
                            handler.post {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY;
                                releaseCamera()
                                startActivity(intent)
                            }
                        } else {
                            handler.post {
                                releaseCamera()
                                startCamera()
                                progressBar3.visibility = View.INVISIBLE
                                camera_preview.visibility = View.VISIBLE
                                button3.visibility = View.VISIBLE
                                Toast.makeText(this, "Invalid Face", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Log.d("Car Dashboard", "file written")
                    } catch (e: FileNotFoundException) {
                        Log.d(ContentValues.TAG, "File not found: ${e.message}")
                    } catch (e: IOException) {
                        Log.d(ContentValues.TAG, "Error accessing file: ${e.message}")
                    }
                }).start()
            }

            val captureButton: Button = findViewById(R.id.button3)
            captureButton.setOnClickListener {
                Thread(Runnable {
                    mCamera?.takePicture(null, null, mPicture)
                }).start()
                progressBar3.visibility = View.VISIBLE
                camera_preview.visibility = View.INVISIBLE
                button3.visibility = View.INVISIBLE
            }
        } else {
            findViewById<Button>(R.id.button3).isEnabled = false
            camera_preview.visibility = View.INVISIBLE
            findViewById<Button>(R.id.button).visibility = View.VISIBLE
            Values.isFirst = false
            findViewById<Button>(R.id.button).setOnClickListener{
                val handler = Handler()
                handler.post {
                    val intent = Intent(this@LoginActivity, FaceAddActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY;
                    releaseCamera()
                    startActivity(intent)
                }
            }
        }
        findViewById<Button>(R.id.button6).setOnClickListener{
            val handler = Handler()
            handler.post {
                val intent = Intent(this@LoginActivity, PinActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY;
                releaseCamera()
                startActivity(intent)
            }
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
        if (checkPresent) {
            t2s.speak(
                "Please show your face to the camera. And click to start the engine",
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
        } else {
            t2s.speak(
                "Please add your face I D. Or use your backup pin",
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
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
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(it)
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

    private fun getCameraInstance(): Camera? {
        return try {
            cameraId = findFrontFacingCamera();
            Camera.open(cameraId)
        } catch (e: Exception) {
            null
        }
    }

    override fun onBackPressed() {

    }
}