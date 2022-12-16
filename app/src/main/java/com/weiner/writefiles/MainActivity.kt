package com.weiner.writefiles

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import java.io.*
import java.net.URI


class MainActivity : AppCompatActivity() {
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
    }

    fun writeToPrivateStorage() {
        val FILE_NAME = "file.txt"
        var fos: FileOutputStream? = null
        try {
            val text = "Какие-то данные"
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

    fun readFromPrivateStorage() {
        val FILE_NAME = "file.txt"
        var fin: FileInputStream? = null
        try {
            fin = openFileInput(FILE_NAME)
            val bytes = ByteArray(fin.available())
            fin.read(bytes)
            val text = String(bytes)
            Toast.makeText(this, "Данные из файла: ${text}", Toast.LENGTH_SHORT).show()
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

    fun writeToSharedStorage(createdFile: File? = null) {
        val FILE_NAME = "file.txt"
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
            val text = "Какие-то данные"
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

    fun readFromSharedStorage() {
        val FILE_NAME = "file.txt"
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
            Toast.makeText(this, "Данные из файла: ${text}", Toast.LENGTH_SHORT).show()
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

    val CREATE_FILE = 1
    fun writeViaIntent() {
        val FILE_NAME = "file.txt"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/txt"
        intent.putExtra(Intent.EXTRA_TITLE, FILE_NAME)
        startActivityForResult(intent, CREATE_FILE)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        val text = "Какие-то данные"
        if (requestCode == CREATE_FILE
            && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                Toast.makeText(this, "Файл сохранён: ${uri.toString()}", Toast.LENGTH_SHORT).show()
                val fos = contentResolver.openOutputStream(uri)
                fos?.write(text.toByteArray())
            }
        }
    }

}