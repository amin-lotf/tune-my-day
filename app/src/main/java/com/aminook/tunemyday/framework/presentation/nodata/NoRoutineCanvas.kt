package com.aminook.tunemyday.framework.presentation.nodata

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import androidx.core.content.ContextCompat
import com.aminook.tunemyday.R

class NoRoutineCanvas(context: Context):View(context) {

    val paint=Paint().apply {
        isFilterBitmap=true
        isAntiAlias=true
        color=ContextCompat.getColor(context, R.color.colorFontDark)
    }
    val path = Path()

    var cWidth:Float = 0f
    var cHeight:Float=0f
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

       canvas?.let {
           val arrX=cWidth/2
           val arrY=cHeight-150
           path.moveTo(arrX-30, arrY)
           path.lineTo(arrX+30, arrY)
           path.lineTo(arrX, arrY+50)
           path.close()
           canvas.drawPath(path, paint)
           paint.strokeWidth=2f
           canvas.drawLine(arrX,arrY,arrX,cHeight/2+45,paint)

       }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cWidth= w.toFloat()
        cHeight=h.toFloat()
    }

}