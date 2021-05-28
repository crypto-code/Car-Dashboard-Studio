package com.car_dashboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import java.io.InputStream
import java.net.URL

class DownloadImageTask(bmImage: ImageView) :
    AsyncTask<String?, Void?, Bitmap?>() {

    var bmImage: ImageView = bmImage

    override fun doInBackground(vararg p0: String?): Bitmap? {
        val urldisplay = p0[0]
        var mIcon11: Bitmap? = null
        try {
            val `in`: InputStream = URL(urldisplay).openStream()
            mIcon11 = BitmapFactory.decodeStream(`in`)
        } catch (e: Exception) {
            e.message?.let { Log.e("Error", it) }
            e.printStackTrace()
        }
        return mIcon11
    }

    override fun onPostExecute(result: Bitmap?) {
        bmImage.setImageBitmap(result)
    }

}
