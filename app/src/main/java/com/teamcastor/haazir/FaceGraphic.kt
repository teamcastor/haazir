/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamcastor.haazir

import android.graphics.*
import android.util.Log
import com.google.mlkit.vision.face.Face

/**
 * Graphic instance for rendering face position within the associated
 * graphic overlay view.
 */
class FaceGraphic(overlay: GraphicOverlay, private val face: Face, private val img: Bitmap?) : GraphicOverlay.Graphic(overlay) {
    private val facePositionPaint: Paint = Paint()

    init {
        facePositionPaint.color = selectedColor
        facePositionPaint.style = Paint.Style.STROKE
        facePositionPaint.strokeWidth = 5.0f
    }

    /** Draws the face annotations for position on the supplied canvas. */
    override fun draw(canvas: Canvas?) {

        // Transform the image coordinates.
        val x = translateX(face.boundingBox.centerX().toFloat())
        val y = translateY(face.boundingBox.centerY().toFloat())

        // Calculate positions.
        val left = x - scale(face.boundingBox.width() / 2.0f)
        val top = y - scale(face.boundingBox.height() / 2.0f)
        val right = x + scale(face.boundingBox.width() / 2.0f)
        val bottom = y + scale(face.boundingBox.height() / 2.0f)

        val rect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        canvas?.drawRect(rect, facePositionPaint)

    }
    companion object {
        var selectedColor = Color.WHITE
    }
}