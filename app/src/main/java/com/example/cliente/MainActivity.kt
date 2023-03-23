package com.example.cliente

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import okhttp3.ResponseBody
import android.widget.Button
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.create
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File


class MainActivity : AppCompatActivity() {
    private val uris = mutableListOf<Uri>()

    companion object{
        const val REQUEST_CODE_IMAGE_PICKER=1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectImageButton = findViewById<Button>(R.id.select_image_button)
        selectImageButton.setOnClickListener {
            selectImage()
        }

        val Enviar = findViewById<Button>(R.id.upload_image_button)
        Enviar.setOnClickListener {
            EnviarDatos()
        }


    }

    // Método para seleccionar una imagen de la galería del dispositivo
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {

            if (data.clipData != null) {
                for (i in 0 until data.clipData!!.itemCount) {
                    uris.add(data.clipData!!.getItemAt(i).uri)
                }
            } else {
                uris.add(data.data!!)
            }


        }


    }

    interface ApiService {
        @Multipart
        @POST("multimedia2.php")
        suspend fun enviarArchivos(@Part files: List<MultipartBody.Part>): ResponseBody
    }

    object RetrofitClient {
        private const val BASE_URL = "http://127.0.0.1:80/multimedia/"
        private val client = OkHttpClient.Builder().build()

        val apiService: ApiService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun EnviarDatos() {
        val files = mutableListOf<MultipartBody.Part>()
        for (uri in uris) {
            val file = File(uri.path!!)
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file[]", file.name, requestFile)
            files.add(part)
        }

        CoroutineScope(Dispatchers.IO).launch {
            var response: ResponseBody? = null
            try {
                response = RetrofitClient.apiService.enviarArchivos(files)
                Log.d("TAG", "Solicitud HTTP realizada correctamente")
                // Procesa la respuesta del servidor si es necesario
            } catch (e: Exception) {
                Log.e("TAG", "Error al realizar la solicitud HTTP", e)
                // Maneja el error si es necesario
            } finally {
                response?.close()
            }
        }
    }























}
