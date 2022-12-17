package com.weiner.writefiles

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val CREATE_FILE = 1
        const val OPEN_FILE = 2
    }

    lateinit var editTextFileName: EditText
    lateinit var editTextFileContents: EditText

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
                if (permissionsStatusMap.containsValue(false)) {
                    Toast.makeText(
                        applicationContext,
                        "Приложению нужно разрешение для записи/чтения на SD-карту",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        fun checkPermissions(): Boolean {
            val results = permissions.map {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
            return results.all { it }
        }

        fun shouldShowRequestPermissionsRationale(): Boolean {
            val results = permissions.map {
                shouldShowRequestPermissionRationale(it)
            }
            return results.all { it }
        }

        when {
            checkPermissions() -> {}
            shouldShowRequestPermissionsRationale() -> {
                Toast.makeText(
                    applicationContext,
                    "Приложению нужно разрешение для записи/чтения на SD-карту",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                requestPermissionLauncher.launch(permissions)
            }
        }

        val buttonWritePrivate = findViewById<Button>(R.id.buttonWritePrivate)
        buttonWritePrivate.setOnClickListener {
            writeToPrivateStorage()
        }

        val buttonReadPrivate = findViewById<Button>(R.id.buttonReadPrivate)
        buttonReadPrivate.setOnClickListener {
            readFromPrivateStorage()
        }

        val buttonWriteShared = findViewById<Button>(R.id.buttonWriteShared)
        buttonWriteShared.setOnClickListener {
            writeToSharedStorage()
        }

        val buttonReadShared = findViewById<Button>(R.id.buttonReadShared)
        buttonReadShared.setOnClickListener {
            readFromSharedStorage()
        }

        val buttonWriteIntent = findViewById<Button>(R.id.buttonWriteIntent)
        buttonWriteIntent.setOnClickListener {
            writeViaIntent()
        }

        val buttonReadIntent = findViewById<Button>(R.id.buttonReadIntent)
        buttonReadIntent.setOnClickListener {
            readViaIntent()
        }

        val textViewPrivateStorage = findViewById<TextView>(R.id.textViewPrivateStorage)
        textViewPrivateStorage.text = "${resources.getString(R.string.private_storage)} ${filesDir.absolutePath}"

        val textViewSharedStorage = findViewById<TextView>(R.id.textViewSharedStorage)
        val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.getExternalStoragePublicDirectory(
                // тут можно выбрать, в какой каталог писать
                Environment.DIRECTORY_DOCUMENTS
            ).absolutePath
        } else {
            Environment.getExternalStorageDirectory().absolutePath
        }
        textViewSharedStorage.text = "${resources.getString(R.string.shared_storage)} $path"

        editTextFileName = findViewById(R.id.editTextFileName)
        editTextFileContents = findViewById(R.id.editTextFileContents)
    }

    private fun writeToPrivateStorage() {
        val FILE_NAME = editTextFileName.text.toString()
        var fos: FileOutputStream? = null
        try {
            val text = editTextFileContents.text.toString()
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE)
            fos.write(text.toByteArray())
            Toast.makeText(
                this,
                "Файл сохранен по адресу ${getFileStreamPath(FILE_NAME)}",
                Toast.LENGTH_SHORT
            )
                .show()
        } catch (ex: IOException) {
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
        } finally {
            try {
                if (fos != null) fos.close()
            } catch (ex: IOException) {
                Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun readFromPrivateStorage() {
        val FILE_NAME = editTextFileName.text.toString()
        var fin: FileInputStream? = null
        try {
            fin = openFileInput(FILE_NAME)
            val bytes = ByteArray(fin.available())
            fin.read(bytes)
            val text = String(bytes)
            Toast.makeText(this, "Данные из файла: $text", Toast.LENGTH_SHORT).show()
        } catch (ex: IOException) {
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
        } finally {
            try {
                if (fin != null) fin.close()
            } catch (ex: IOException) {
                Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun writeToSharedStorage(createdFile: File? = null) {
        val FILE_NAME = editTextFileName.text.toString()
        var path: File? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            path = Environment.getExternalStoragePublicDirectory(
                // тут можно выбрать, в какой каталог писать
                Environment.DIRECTORY_DOCUMENTS
            )
        } else {
            path = Environment.getExternalStorageDirectory()
        }

        var fos: FileOutputStream? = null
        try {
            val text = editTextFileContents.text.toString()
            val file = createdFile ?: File(path, FILE_NAME)
            file.createNewFile()
            fos = FileOutputStream(file);
            fos.write(text.toByteArray())
            Toast.makeText(
                this,
                "Файл сохранен по адресу ${file.absolutePath}",
                Toast.LENGTH_SHORT
            )
                .show()
        } catch (ex: Exception) {
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
        } finally {
            try {
                if (fos != null) fos.close()
            } catch (ex: IOException) {
                Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun readFromSharedStorage() {
        val FILE_NAME = editTextFileName.text.toString()
        var path: File? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            path = Environment.getExternalStoragePublicDirectory(
                // тут можно выбрать, в какой каталог писать
                Environment.DIRECTORY_DOCUMENTS
            )
        } else {
            path = Environment.getExternalStorageDirectory()
        }

        var fin: FileInputStream? = null
        try {
            val file = File(path, FILE_NAME)
            fin = FileInputStream(file);
            val bytes = ByteArray(fin.available())
            fin.read(bytes)
            val text = String(bytes)
            Toast.makeText(this, "Данные из файла: $text", Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
        } finally {
            try {
                if (fin != null) fin.close()
            } catch (ex: IOException) {
                Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun writeViaIntent() {
        val FILE_NAME = editTextFileName.text.toString()
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/txt"
        intent.putExtra(Intent.EXTRA_TITLE, FILE_NAME)
        startActivityForResult(intent, CREATE_FILE)
    }

    private fun readViaIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, OPEN_FILE)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        val textViewIntentStorage = findViewById<TextView>(R.id.textViewIntentStorage)
        val text = editTextFileContents.text.toString()
        if (requestCode == CREATE_FILE
            && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                Toast.makeText(this, "Файл сохранён: $uri", Toast.LENGTH_SHORT).show()
                textViewIntentStorage.text = "${resources.getString(R.string.selected_storage)} $uri"
                try {
                    val fos = contentResolver.openOutputStream(uri)
                    fos?.write(text.toByteArray())
                } catch (ex: Exception) {
                    Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (requestCode == OPEN_FILE
            && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                textViewIntentStorage.text = "${resources.getString(R.string.selected_storage)} $uri"
                try {
                    val fin = contentResolver.openInputStream(uri)
                    val bytes = ByteArray(fin!!.available())
                    fin.read(bytes)
                    val readText = String(bytes)
                    Toast.makeText(this, "Данные из файла: $readText", Toast.LENGTH_SHORT).show()
                } catch (ex: Exception) {
                    Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}