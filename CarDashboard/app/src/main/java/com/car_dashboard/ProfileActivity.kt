package com.car_dashboard

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.chaquo.python.Python
import java.io.ByteArrayOutputStream


class ProfileActivity : AppCompatActivity() {

    lateinit var imageView :ImageView;
    var chosen :Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        this.findViewById<Button>(R.id.chooseBtn).setOnClickListener {
            selectImage(this)
        }

        this.findViewById<Button>(R.id.uploadBtn).setOnClickListener {
            uploadImage()
        }

        imageView = this.findViewById(R.id.imageView7)
    }

    fun drawable2Bytes(): ByteArray? {
        val bitmap = drawable2Bitmap()
        return bitmap2Bytes(bitmap)
    }

    fun drawable2Bitmap(): Bitmap {
        val bitmap = imageView.drawable.toBitmap()
        return bitmap
    }

    fun bitmap2Bytes(bm: Bitmap): ByteArray? {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    private fun uploadImage() {
        if (!chosen) {
            Toast.makeText(this, "Please Choose An Image First", Toast.LENGTH_LONG).show()
            return
        }

        val python = Python.getInstance()
        val pythonFile = python.getModule("main")
        val imageUpload = pythonFile.callAttr("upload_profile_pic", Values.myID, Values.myName, drawable2Bytes()).toBoolean()
        if (imageUpload) {
            this.onBackPressed()
        } else {
            Toast.makeText(this, "An Error Occurred, Try Again", Toast.LENGTH_LONG).show()
        }
    }

    private fun selectImage(context: Context) {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose your profile picture")
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Take Photo") {
                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePicture, 0)
            } else if (options[item] == "Choose from Gallery") {
                val pickPhoto = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(pickPhoto, 1)
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    imageView.setImageBitmap(selectedImage)
                    chosen = true
                }
                1 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage: Uri? = data.data
                    Log.d("Data", selectedImage.toString())
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        val cursor: Cursor? = contentResolver.query(
                            selectedImage,
                            filePathColumn, null, null, null
                        )
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                            val picturePath: String = cursor.getString(columnIndex)
                            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath))
                            chosen = true
                            cursor.close()
                        }
                    }
                }
            }
        }
    }

}