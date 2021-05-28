package com.car_dashboard

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import java.io.FileNotFoundException
import java.io.IOException

class FaceAddActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var cameraId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_add)

        startCamera()

        val progressBar4 : ProgressBar = this.findViewById(R.id.progressBar4)
        val camera_preview3 :FrameLayout = this.findViewById(R.id.camera_preview3)
        val button24 : Button = this.findViewById(R.id.button24)
        val personName : TextView = this.findViewById(R.id.personName2)
        val pin : TextView = this.findViewById(R.id.pin2)


        val handler = Handler()
        val mPicture = Camera.PictureCallback { data, _ ->
            Thread(Runnable {
                try {
                    if (checkValid()) {
                        val python = Python.getInstance()
                        val pythonFile = python.getModule("main")
                        val checkPinValid = pythonFile.callAttr("check_valid_pin", Values.myID, Values.myName, Values.myPIN)
                        if (checkPinValid.toBoolean()) {
                            val storeFace = pythonFile.callAttr(
                                "add_face",
                                Values.myID,
                                Values.myName,
                                Values.myPIN,
                                data
                            )
                            if (storeFace.toBoolean()) {
                                handler.post {
                                    val intent =
                                        Intent(this@FaceAddActivity, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY;
                                    releaseCamera()
                                    startActivity(intent)
                                }
                            } else {
                                handler.post {
                                    releaseCamera()
                                    startCamera()
                                    progressBar4.visibility = View.INVISIBLE
                                    camera_preview3.visibility = View.VISIBLE
                                    button24.visibility = View.VISIBLE
                                    personName.isEnabled = true
                                    pin.isEnabled = true
                                    Toast.makeText(
                                        this,
                                        "Make sure exactly one face is visible",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } else {
                            handler.post {
                                releaseCamera()
                                startCamera()
                                progressBar4.visibility = View.INVISIBLE
                                camera_preview3.visibility = View.VISIBLE
                                button24.visibility = View.VISIBLE
                                personName.isEnabled = true
                                pin.isEnabled = true
                                Toast.makeText(
                                    this,
                                    "Make sure the provided details are correct",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                } catch (e: FileNotFoundException) {
                    Log.d(ContentValues.TAG, "File not found: ${e.message}")
                } catch (e: IOException) {
                    Log.d(ContentValues.TAG, "Error accessing file: ${e.message}")
                }
            }).start()
        }

        val captureButton: Button = findViewById(R.id.button24)
        captureButton.setOnClickListener {
            Thread(Runnable {
                mCamera?.takePicture(null, null, mPicture)
            }).start()
            progressBar4.visibility = View.VISIBLE
            camera_preview3.visibility = View.INVISIBLE
            button24.visibility = View.INVISIBLE
            personName.isEnabled = false
            pin.isEnabled = false
        }

        this.findViewById<Button>(R.id.button2).setOnClickListener {
            handler.post {
                val intent =
                    Intent(this@FaceAddActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY;
                releaseCamera()
                startActivity(intent)
            }
        }
    }

    fun checkValid() :Boolean {
        val personName : TextView = this.findViewById(R.id.personName2)
        val pin : TextView = this.findViewById(R.id.pin2)
        return if (personName.text.toString().isBlank() || pin.text.toString().isBlank()) {
            Toast.makeText(this,"Name and PIN are Required", Toast.LENGTH_SHORT).show()
            false
        } else if (personName.text.length < 3) {
            Toast.makeText(this,"Name must be minimum 3 characters long", Toast.LENGTH_LONG).show()
            false
        } else if (pin.text.length < 4) {
            Toast.makeText(this,"Pin must be minimum 4 numbers long", Toast.LENGTH_LONG).show()
            false
        } else {
            Values.myName = personName.text.toString()
            Values.myPIN = pin.text.toString()
            true
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
            val preview: FrameLayout = findViewById(R.id.camera_preview3)
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

    }
}