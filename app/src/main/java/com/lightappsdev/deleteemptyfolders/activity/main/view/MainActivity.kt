package com.lightappsdev.deleteemptyfolders.activity.main.view

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.view.View
import android.widget.TextView
import android.widget.TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lightappsdev.deleteemptyfolders.R
import com.lightappsdev.deleteemptyfolders.activity.main.viewmodel.MainActivityViewModel
import com.lightappsdev.deleteemptyfolders.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainActivityViewModel by viewModels()

    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    val s = "Se necesita el permiso para eliminar las carpetas"
                    Toast.makeText(this, s, LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.button.setOnClickListener {
            if (checkStoragePermissions()) {
                viewModel.deleteEmptyFolders()
            } else {
                requestStoragePermissions()
            }
        }

        viewModel.foldersDeleted.observe(this) { strings ->
            binding.linearLayout.removeAllViews()
            strings.forEach { s -> binding.linearLayout.addView(generateTextView(s)) }
        }

        viewModel.isLoading.observe(this) { b ->
            binding.button.isEnabled = !b
        }

        if (!checkStoragePermissions()) {
            requestStoragePermissions()
        }

        setContentView(binding.root)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.none { granted -> granted == PERMISSION_GRANTED }) {
            Toast.makeText(this, "Se necesita el permiso para eliminar las carpetas", LENGTH_SHORT)
                .show()
        }
    }

    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            checkIOPermissions()
        }
    }

    private fun checkIOPermissions(): Boolean {
        val hasReadPermission = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
        val hasWritePermission = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)

        return hasReadPermission == PERMISSION_GRANTED && hasWritePermission == PERMISSION_GRANTED
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent().apply {
                    action = ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    data = Uri.fromParts("package", packageName, null)
                }
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent().apply {
                    action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                }
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            val permissionsArray = arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissionsArray, 1)
        }
    }

    private fun generateTextView(string: String): TextView {
        return TextView(this).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                id = View.generateViewId()
            }
            textSize = 16F
            setTextColor(ContextCompat.getColor(context, R.color.text_color))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setAutoSizeTextTypeWithDefaults(AUTO_SIZE_TEXT_TYPE_UNIFORM)
            } else {
                textSize = 14F
            }
            text = string
        }
    }
}