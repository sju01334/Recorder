package com.nepplus.recorder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class SoundVisualizerView(
    context : Context,
    attr : AttributeSet? = null
) : View(context, attr){

    var onRequestCurrentAmplitude : (() -> Int)? = null

    //paint 모양 세팅
    private val amplitudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.purple_500)
        strokeWidth = LINE_WIDTH
        strokeCap = Paint.Cap.ROUND
    }
    private var drawingWidth : Int = 0
    private var drawingHeight : Int = 0
    private var drawingAmplitudes : List<Int> = emptyList()
    private var isReplaying : Boolean = false
    private var replayingPosition : Int = 0


    private val visualizeRepeatAction : Runnable = object : Runnable{
        override fun run() {

            if(!isReplaying){
                // 소리 int 값 져오기
                val currentAmplitude = onRequestCurrentAmplitude?.invoke() ?: 0
                //가져온 소리 int 리스트에 넣어주기
                drawingAmplitudes = listOf(currentAmplitude) + drawingAmplitudes
            }else{
                replayingPosition++
            }
            //화면 갱신
            invalidate()

            handler?.postDelayed(this, ACTION_INTERVAL)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingWidth = w
        drawingHeight = h

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        val centerY = drawingHeight / 2f
        var offsetX = drawingWidth.toFloat()

        drawingAmplitudes.let {amplitudes ->
                if(isReplaying){
                    //뒤에서부터 하나씩 가져오기
                    amplitudes.takeLast(replayingPosition)
                }else{
                    amplitudes
                }
            }
            .forEach { amplitude ->
            val lineLength = amplitude / MAX_AMPLITUDE * drawingHeight * 0.8F

            offsetX -= LINE_SPACE
            if(offsetX < 0) return@forEach

//          선 그려주기
            canvas.drawLine(
                offsetX,
                centerY - lineLength / 2F,
                offsetX,
                centerY + lineLength/ 2F,
                amplitudePaint
            )
        }
    }

    fun startVisualizing(isReplaying : Boolean) {
        this.isReplaying = isReplaying
        handler?.post(visualizeRepeatAction)
    }

    fun stopVisualizing() {
        replayingPosition = 0
        handler?.removeCallbacks(visualizeRepeatAction)
    }

    fun clearVisualization() {
        drawingAmplitudes = emptyList()
        invalidate()
    }

    companion object{
        private const val LINE_WIDTH = 10F
        private const val LINE_SPACE =15F
        private const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat()
        private const val ACTION_INTERVAL =20L
    }

}