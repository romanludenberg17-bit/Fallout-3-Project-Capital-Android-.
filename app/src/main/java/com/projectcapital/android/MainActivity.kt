package com.projectcapital.android

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("project_capital", MODE_PRIVATE) }
    private lateinit var status: TextView
    private lateinit var folderLabel: TextView

    companion object {
        private const val REQUEST_GAME_FOLDER = 4102
        private const val KEY_GAME_TREE = "game_tree_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        buildUi()
        restoreInstallation()
    }

    private fun buildUi() {
        val background = Color.rgb(16, 19, 22)
        val panel = Color.rgb(29, 35, 39)
        val accent = Color.rgb(126, 196, 157)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(background)
            setPadding(dp(28), dp(22), dp(28), dp(22))
        }

        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, dp(24), 0)
        }
        left.layoutParams = LinearLayout.LayoutParams(0, -1, 1.15f)

        val eyebrow = text("VAULT-TEC FIELD SYSTEM  /  ANDROID", 11, accent)
        left.addView(eyebrow)
        left.addView(text("PROJECT\nCAPITAL", 34, Color.WHITE, true).apply {
            setPadding(0, dp(12), 0, dp(8))
        })
        left.addView(text("fallout 3 · native android port project", 14, Color.LTGRAY))
        left.addView(text("prototype 0.1.0", 11, Color.GRAY).apply {
            setPadding(0, dp(18), 0, 0)
        })

        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(20))
            setBackgroundColor(panel)
        }
        right.layoutParams = LinearLayout.LayoutParams(0, -1, 1f)

        right.addView(text("GAME INSTALLATION", 12, accent, true))
        folderLabel = text("no game folder selected", 15, Color.WHITE).apply {
            setPadding(0, dp(12), 0, dp(14))
        }
        right.addView(folderLabel)

        val choose = button("SELECT GAME FOLDER", accent)
        choose.setOnClickListener { openFolderPicker() }
        right.addView(choose)

        val launch = button("LAUNCH GAME", Color.rgb(75, 88, 83))
        launch.isEnabled = false
        launch.alpha = 0.55f
        launch.setOnClickListener {
            setStatus("engine runtime is not integrated yet")
        }
        right.addView(launch.apply { setPadding(0, dp(8), 0, dp(8)) })

        val settings = button("TOUCH CONTROLS  ·  COMING SOON", Color.rgb(62, 72, 77))
        settings.isEnabled = false
        settings.alpha = 0.65f
        right.addView(settings)

        status = text("select your Fallout 3 installation folder to begin", 12, Color.LTGRAY)
        status.setPadding(0, dp(16), 0, 0)
        right.addView(status)

        root.addView(left)
        root.addView(right)
        setContentView(root)
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, REQUEST_GAME_FOLDER)
    }

    @Deprecated("Using the document-tree result API for broad Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_GAME_FOLDER || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        val flags = data.flags and
            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            contentResolver.takePersistableUriPermission(uri, flags)
            prefs.edit().putString(KEY_GAME_TREE, uri.toString()).apply()
            validateInstallation(uri)
        } catch (e: SecurityException) {
            setStatus("android did not grant persistent access: ${e.localizedMessage}")
        }
    }

    private fun restoreInstallation() {
        val saved = prefs.getString(KEY_GAME_TREE, null) ?: return
        runCatching { validateInstallation(Uri.parse(saved)) }
            .onFailure { setStatus("saved folder access expired — select it again") }
    }

    private fun validateInstallation(uri: Uri) {
        val tree = DocumentFile.fromTreeUri(this, uri)
        if (tree == null || !tree.canRead()) {
            setStatus("cannot read this folder; select the game directory again")
            return
        }

        val files = tree.listFiles()
        val hasExe = files.any { it.name.equals("Fallout3.exe", ignoreCase = true) }
        val hasData = files.any { it.isDirectory && it.name.equals("Data", ignoreCase = true) }

        folderLabel.text = tree.name ?: "selected game folder"
        when {
            hasExe && hasData -> setStatus("installation layout detected · engine port not yet integrated")
            else -> setStatus("folder saved, but Fallout3.exe and/or Data were not found at its root")
        }
    }

    private fun setStatus(message: String) {
        status.text = message
    }

    private fun text(value: String, size: Int, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun button(label: String, color: Int) =
        Button(this).apply {
            text = label
            textSize = 11f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundColor(color)
            gravity = Gravity.CENTER
            minHeight = dp(48)
            setPadding(dp(12), dp(8), dp(12), dp(8))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(10)
            }
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
