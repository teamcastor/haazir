package com.teamcastor.haazir

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.os.Environment
import android.renderscript.*
import android.util.Log
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.view.animation.AnimationUtils.loadInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.view.drawToBitmap
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


class Utils {
    /**
     * Helper class used to convert a [Image] object from
     * [ImageFormat.YUV_420_888] format to an RGB [Bitmap] object, it has equivalent
     * functionality to https://github
     * .com/androidx/androidx/blob/androidx-main/camera/camera-core/src/main/java/androidx/camera/core/ImageYuvToRgbConverter.java
     *
     * NOTE: This has been tested in a limited number of devices and is not
     * considered production-ready code. It was created for illustration purposes,
     * since this is not an efficient camera pipeline due to the multiple copies
     * required to convert each frame. For example, this
     * implementation
     * (https://stackoverflow.com/questions/52726002/camera2-captured-picture-conversion-from-yuv-420-888-to-nv21/52740776#52740776)
     * might have better performance.
     * Lorem: Performance is satisfactory in my experience. Able to maintain 60fps at 720p on my Snapdragon 636.
     */
    @Suppress("DEPRECATION")
    class YuvToRgbConverter(context: Context) {
        private val rs = RenderScript.create(context)
        private val scriptYuvToRgb =
            ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

        // Do not add getters/setters functions to these private variables
        // because yuvToRgb() assume they won't be modified elsewhere
        private var yuvBits: ByteBuffer? = null
        private var bytes: ByteArray = ByteArray(0)
        private var inputAllocation: Allocation? = null
        private var outputAllocation: Allocation? = null

        @Synchronized
        fun yuvToRgb(image: Image, output: Bitmap) {
            val yuvBuffer = YuvByteBuffer(image, yuvBits)
            yuvBits = yuvBuffer.buffer

            if (needCreateAllocations(image, yuvBuffer)) {
                val yuvType = Type.Builder(rs, Element.U8(rs))
                    .setX(image.width)
                    .setY(image.height)
                    .setYuvFormat(yuvBuffer.type)
                inputAllocation = Allocation.createTyped(
                    rs,
                    yuvType.create(),
                    Allocation.USAGE_SCRIPT
                )
                bytes = ByteArray(yuvBuffer.buffer.capacity())
                val rgbaType = Type.Builder(rs, Element.RGBA_8888(rs))
                    .setX(image.width)
                    .setY(image.height)
                outputAllocation = Allocation.createTyped(
                    rs,
                    rgbaType.create(),
                    Allocation.USAGE_SCRIPT
                )
            }

            yuvBuffer.buffer.get(bytes)
            inputAllocation!!.copyFrom(bytes)

            // Convert NV21 or YUV_420_888 format to RGB
            inputAllocation!!.copyFrom(bytes)
            scriptYuvToRgb.setInput(inputAllocation)
            scriptYuvToRgb.forEach(outputAllocation)
            outputAllocation!!.copyTo(output)
            // Strict mode logs these otherwise
            inputAllocation!!.destroy()
            outputAllocation!!.destroy()
            scriptYuvToRgb.destroy()
        }

        private fun needCreateAllocations(image: Image, yuvBuffer: YuvByteBuffer): Boolean {
            return (inputAllocation == null ||               // the very 1st call
                    inputAllocation!!.type.x != image.width ||   // image size changed
                    inputAllocation!!.type.y != image.height ||
                    inputAllocation!!.type.yuv != yuvBuffer.type || // image format changed
                    bytes.size == yuvBuffer.buffer.capacity())
        }
    }

    companion object {

        fun convertBitmapToByteBuffer(bp: Bitmap, capacity: Int, width: Int, imgMean: Float, imgStd: Float, bgr: Boolean= false): ByteBuffer {
            val current = System.currentTimeMillis()
            val imageBuffer = ByteBuffer.allocate(capacity)
            imageBuffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(width * width)
            bp.getPixels(intValues, 0, bp.width, 0, 0, bp.width, bp.height)
            // Convert the image to floating point.
            for (pixel in 0 until ((width * width) - 1)) {                                    //
                val value = intValues[pixel]
                // Do normalization if needed (255f imgStd leads to 0 to 1.0)
                // Recognition model needs RGB
                if (!bgr) {
                    imageBuffer.putFloat(((value shr 16 and 0xFF) - imgMean) / imgStd) // Red
                    imageBuffer.putFloat(((value shr 8 and 0xFF) - imgMean) / imgStd)  // Green
                    imageBuffer.putFloat(((value and 0xFF) - imgMean) / imgStd)       // Blue
                }
                // Spoofing model needs BGR
                else {
                    imageBuffer.putFloat(((value and 0xFF) - imgMean) / imgStd)     // Blue
                    imageBuffer.putFloat(((value shr 8 and 0xFF) - imgMean) / imgStd)  // Green
                    imageBuffer.putFloat(((value shr 16 and 0xFF) - imgMean) / imgStd) // Red
                }
            }
            val end = System.currentTimeMillis()
            Log.v("FAS", "Time-taken for convertBitmapTBB : ${end - current}")
            return imageBuffer
        }

        fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
            val width = bm.width
            val height = bm.height
            val scaleWidth = newWidth.toFloat() / width
            val scaleHeight = newHeight.toFloat() / height
            // CREATE A MATRIX FOR THE MANIPULATION
            val matrix = Matrix()
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight)
            // "RECREATE" THE NEW BITMAP
            // Disable the filter as it smooths out the image
            return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false
            )
        }

        fun cropFace(rect: Rect, img: Bitmap, width: Int): Bitmap {
            val croppedImage =
                Bitmap.createBitmap(img, rect.left, rect.top, rect.width(), rect.height())
            return getResizedBitmap(croppedImage, width, width)
        }

        fun saveImage(bitmap: Bitmap?) {
            var outStream: FileOutputStream? = null
            // Write to SD Card
            try {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        .toString() + File.separator + "Haazir/"
                )
                dir.mkdirs()
                val fileName = String.format("%s_%d.jpg", "Image", System.currentTimeMillis())
                val outFile = File(dir, fileName)
                outStream = FileOutputStream(outFile)
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                outStream.flush()
                outStream.close()
            } catch (e: FileNotFoundException) {
                Log.i("FileNotFoundError", e.toString())
            } catch (e: IOException) {
                Log.i("IOException", e.toString())
            } finally {
            }
        }


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