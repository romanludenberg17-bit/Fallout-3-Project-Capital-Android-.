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
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import org.json.JSONObject

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("project_capital", MODE_PRIVATE) }
    private lateinit var status: TextView
    private lateinit var folderLabel: TextView
    private lateinit var launchButton: Button

    companion object {
        private const val REQUEST_GAME_FOLDER = 4102
        private const val KEY_GAME_TREE = "game_tree_uri"
        private const val KEY_REGISTRY_PROFILE = "virtual_registry_profile"
        private const val RUNTIME_MOUNT = """Z:\game\"""
    }

    private data class GameInstall(
        val id: String,
        val title: String,
        val executable: String,
        val registrySubKey: String
    )

    private val supportedGames = listOf(
        GameInstall(
            id = "fallout3",
            title = "Fallout 3",
            executable = "Fallout3.exe",
            registrySubKey = """SOFTWARE\Wow6432Node\Bethesda Softworks\Fallout3"""
        ),
        GameInstall(
            id = "falloutnv",
            title = "Fallout: New Vegas",
            executable = "FalloutNV.exe",
            registrySubKey = """SOFTWARE\Wow6432Node\Bethesda Softworks\FalloutNV"""
        )
    )

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

        left.addView(text("VAULT-TEC FIELD SYSTEM  /  ANDROID", 11, accent))
        left.addView(text("PROJECT\\nCAPITAL", 34, Color.WHITE, true).apply {
            setPadding(0, dp(12), 0, dp(8))
        })
        left.addView(text("fallout 3 · native android port project", 14, Color.LTGRAY))
        left.addView(text("prototype 0.2.0 · registry bridge groundwork", 11, Color.GRAY).apply {
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

        launchButton = button("LAUNCH GAME", Color.rgb(75, 88, 83)).apply {
            isEnabled = false
            alpha = 0.55f
            setOnClickListener { setStatus("engine runtime is not integrated yet; registry profile is prepared") }
        }
        right.addView(launchButton.apply { setPadding(0, dp(8), 0, dp(8)) })

        val settings = button("CUSTOMIZE TOUCH CONTROLS", Color.rgb(62, 88, 77))
        settings.setOnClickListener {
            startActivity(Intent(this, TouchControlsActivity::class.java))
        }
        right.addView(settings)

        status = text("select your Fallout installation folder to begin", 12, Color.LTGRAY)
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
            launchButton.isEnabled = false
            launchButton.alpha = 0.55f
            setStatus("cannot read this folder; select the game directory again")
            return
        }

        val files = tree.listFiles()
        val detected = supportedGames.mapNotNull { game ->
            val executableFound = files.any { it.isFile && it.name.equals(game.executable, true) }
            val dataFound = files.any { it.isDirectory && it.name.equals("Data", true) }
            if (executableFound && dataFound) game else null
        }

        folderLabel.text = tree.name ?: "selected game folder"
        if (detected.isEmpty()) {
            prefs.edit().remove(KEY_REGISTRY_PROFILE).apply()
            launchButton.isEnabled = false
            launchButton.alpha = 0.55f
            setStatus("no supported game found at this folder root; expected Fallout3.exe or FalloutNV.exe and Data/")
            return
        }

        // SAF exposes a content:// URI, not a stable POSIX path. The registry bridge
        // therefore records the selected tree and a runtime mount target separately.
        // The future engine/runtime must mount this tree at Z:\\game before consuming
        // the Installed Path registry value. This does NOT write to Android's registry.
        val profiles = JSONObject()
        detected.forEach { game ->
            profiles.put(game.id, JSONObject().apply {
                put("title", game.title)
                put("registryRoot", "HKEY_LOCAL_MACHINE\\${game.registrySubKey}")
                put("valueName", "Installed Path")
                put("valueType", "REG_SZ")
                put("valueData", RUNTIME_MOUNT)
                put("executable", game.executable)
            })
        }
        val profile = JSONObject().apply {
            put("schemaVersion", 1)
            put("sourceTreeUri", uri.toString())
            put("runtimeMount", RUNTIME_MOUNT)
            put("games", profiles)
            put("requiresRuntimeRegistryAdapter", true)
        }
        prefs.edit().putString(KEY_REGISTRY_PROFILE, profile.toString()).apply()

        launchButton.isEnabled = false
        launchButton.alpha = 0.55f
        val names = detected.joinToString(" + ") { it.title }
        setStatus("$names detected · virtual Installed Path profiles prepared · runtime integration still required")
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
