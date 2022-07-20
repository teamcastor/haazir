package com.teamcastor.haazir

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Environment
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.io.*

class Utils {
    companion object {
        private fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
            val width = bm.width
            val height = bm.height
            val scaleWidth = newWidth.toFloat() / width
            val scaleHeight = newHeight.toFloat() / height
            // CREATE A MATRIX FOR THE MANIPULATION
            val matrix = Matrix()
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight)

            // "RECREATE" THE NEW BITMAP
            return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, true
            )
        }
        fun cropFace(rect: Rect, img: Bitmap): Bitmap {
            val croppedImage =
                Bitmap.createBitmap(img, rect.left, rect.top, rect.width(), rect.height())
            return getResizedBitmap(croppedImage, 256, 256)
        }

//        fun saveImage(bitmap: Bitmap?) {
//            var outStream: FileOutputStream? = null
//            // Write to SD Card
//            try {
//                val dir = File(
//                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
//                        .toString() + File.separator + "Haazir/"
//                )
//                dir.mkdirs()
//                val fileName = String.format("%s_%d.jpg", "Image", System.currentTimeMillis())
//                val outFile = File(dir, fileName)
//                outStream = FileOutputStream(outFile)
//                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
//                outStream.flush()
//                outStream.close()
//            } catch (e: FileNotFoundException) {
//                Log.i("FileNotFoundError", e.toString())
//            } catch (e: IOException) {
//                Log.i("IOException", e.toString())
//            } finally {
//            }
//        }

        fun showSnackbar(
            view: View,
            msg: String,
            length: Int,
            actionMessage: CharSequence?,
            action: (View) -> Unit
        ) {
            val snackbar = Snackbar.make(view, msg, length)
            if (actionMessage != null) {
                snackbar.setAction(actionMessage) {
                    action(view)
                }.show()
            } else {
                snackbar.show()
            }
        }

    }
}