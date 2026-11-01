package com.projectcapital.android

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import kotlin.math.hypot

class TouchControlsActivity : Activity() {
    private lateinit var preview: ControlPreview
    private lateinit var sizeLabel: TextView
    private lateinit var opacityLabel: TextView
    private var controlSize = 1.0f
    private var controlOpacity = 0.78f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        controlSize = prefs().getFloat("control_size", 1f)
        controlOpacity = prefs().getFloat("control_opacity", .78f)
        buildEditor()
    }

    private fun prefs() = getSharedPreferences("project_capital", MODE_PRIVATE)

    private fun buildEditor() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.rgb(14, 18, 20))
        }
        preview = ControlPreview().apply {
            layoutParams = LinearLayout.LayoutParams(0, -1, 1.65f)
            setBackgroundColor(Color.rgb(28, 35, 36))
            loadPositions(prefs().getString("control_positions", null))
        }
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(8), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(0, -1, 1f)
        }
        panel.addView(label("TOUCH CONTROL SETUP", 18, Color.rgb(126, 196, 157)))
        panel.addView(label("drag buttons in the preview to move them", 11, Color.LTGRAY))

        panel.addView(label("CONTROL PROFILE", 12, Color.WHITE))
        val profiles = Spinner(this).apply {
            adapter = ArrayAdapter(this@TouchControlsActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Default", "Compact", "Shooter", "Left-handed"))
            val saved = prefs().getInt("control_profile", 0).coerceIn(0, 3)
            setSelection(saved)
        }
        panel.addView(profiles)

        sizeLabel = label("BUTTON SIZE: ${(controlSize * 100).toInt()}%", 12, Color.WHITE)
        panel.addView(sizeLabel)
        panel.addView(SeekBar(this).apply {
            max = 100
            progress = ((controlSize - .65f) / .85f * 100).toInt().coerceIn(0, 100)
            setOnSeekBarChangeListener(slider { value ->
                controlSize = .65f + value / 100f * .85f
                sizeLabel.text = "BUTTON SIZE: ${(controlSize * 100).toInt()}%"
                preview.invalidate()
            })
        })

        opacityLabel = label("OPACITY: ${(controlOpacity * 100).toInt()}%", 12, Color.WHITE)
        panel.addView(opacityLabel)
        panel.addView(SeekBar(this).apply {
            max = 100
            progress = (controlOpacity * 100).toInt()
            setOnSeekBarChangeListener(slider { value ->
                controlOpacity = value / 100f
                opacityLabel.text = "OPACITY: ${value}%"
                preview.invalidate()
            })
        })

        panel.addView(Button(this).apply {
            text = "RESET LAYOUT"
            setOnClickListener {
                preview.resetPositions()
                controlSize = 1f
                controlOpacity = .78f
                preview.invalidate()
                Toast.makeText(this@TouchControlsActivity, "layout reset", Toast.LENGTH_SHORT).show()
            }
        })
        panel.addView(Button(this).apply {
            text = "SAVE PROFILE"
            setOnClickListener {
                prefs().edit()
                    .putFloat("control_size", controlSize)
                    .putFloat("control_opacity", controlOpacity)
                    .putInt("control_profile", profiles.selectedItemPosition)
                    .putString("control_positions", preview.serializePositions())
                    .apply()
                Toast.makeText(this@TouchControlsActivity, "control layout saved", Toast.LENGTH_SHORT).show()
            }
        })
        panel.addView(Button(this).apply {
            text = "BACK"
            setOnClickListener { finish() }
        })
        root.addView(preview)
        root.addView(panel)
        setContentView(root)
    }

    private fun slider(change: (Int) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = change(progress)
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun label(value: String, size: Int, color: Int) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(color)
        setPadding(0, dp(8), 0, dp(8))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private inner class ControlPreview : View(this@TouchControlsActivity) {
        private val names = arrayOf("MOVE", "LOOK", "FIRE", "AIM", "JUMP", "PIP-BOY", "V.A.T.S.", "USE")
        private val points = arrayOf(
            floatArrayOf(.13f, .68f), floatArrayOf(.48f, .52f),
            floatArrayOf(.87f, .66f), floatArrayOf(.75f, .40f),
            floatArrayOf(.91f, .30f), floatArrayOf(.10f, .25f),
            floatArrayOf(.27f, .25f), floatArrayOf(.87f, .86f)
        )
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var selected = -1
        private var lastX = 0f
        private var lastY = 0f

        fun loadPositions(raw: String?) {
            if (raw.isNullOrBlank()) return
            runCatching {
                raw.split(";").forEachIndexed { index, pair ->
                    val parts = pair.split(",")
                    if (index < points.size && parts.size == 2) {
                        points[index][0] = parts[0].toFloat().coerceIn(.04f, .96f)
                        points[index][1] = parts[1].toFloat().coerceIn(.08f, .92f)
                    }
                }
            }
        }

        fun resetPositions() {
            val defaults = arrayOf(
                floatArrayOf(.13f, .68f), floatArrayOf(.48f, .52f),
                floatArrayOf(.87f, .66f), floatArrayOf(.75f, .40f),
                floatArrayOf(.91f, .30f), floatArrayOf(.10f, .25f),
                floatArrayOf(.27f, .25f), floatArrayOf(.87f, .86f)
            )
            defaults.forEachIndexed { i, p -> points[i] = p.copyOf() }
        }

        fun serializePositions() = points.joinToString(";") { "${it[0]},${it[1]}" }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            paint.color = Color.rgb(52, 65, 63)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.rgb(126, 196, 157)
            paint.textSize = 13f * resources.displayMetrics.density
            canvas.drawText("DRAG CONTROLS TO REPOSITION", dp(14).toFloat(), dp(24).toFloat(), paint)
            val radius = dp((34 * controlSize).toInt()).toFloat()
            names.forEachIndexed { i, name ->
                val x = points[i][0] * width
                val y = points[i][1] * height
                paint.color = Color.argb((controlOpacity * 210).toInt(), 20, 31, 29)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(x, y, radius, paint)
                paint.color = if (i == selected) Color.WHITE else Color.rgb(126, 196, 157)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = dp(2).toFloat()
                canvas.drawCircle(x, y, radius, paint)
                paint.style = Paint.Style.FILL
                paint.textSize = dp(10).toFloat()
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(name, x, y + dp(4), paint)
            }
            paint.textAlign = Paint.Align.LEFT
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    selected = points.indices.minByOrNull { i ->
                        hypot(event.x - points[i][0] * width, event.y - points[i][1] * height)
                    } ?: -1
                    lastX = event.x
                    lastY = event.y
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_MOVE -> if (selected >= 0) {
                    points[selected][0] = (event.x / width).coerceIn(.04f, .96f)
                    points[selected][1] = (event.y / height).coerceIn(.08f, .92f)
                    lastX = event.x
                    lastY = event.y
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    selected = -1
                    invalidate()
                    return true
                }
            }
            return true
        }
    }
}
