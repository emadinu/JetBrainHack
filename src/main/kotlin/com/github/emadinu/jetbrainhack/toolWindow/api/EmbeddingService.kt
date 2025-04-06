package com.github.emadinu.jetbrainhack.toolWindow.api

import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

object EmbeddingService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    fun sendClassesAndResponse(
        classesJson: String,
        responseJson: String,
        callback: (String?) -> Unit
    ) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("classes", classesJson)
            .addFormDataPart("response", responseJson)
            .build()

        val request = Request.Builder()
            .url("http://127.0.0.1:5000/match")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(null)
                    } else {
                        val responseBody = response.body?.string()
                        callback(responseBody)
                    }
                }
            }
        })
    }

    fun integrateCode(
        requestType: String,
        apiUrl: String,
        JSONFormat: String,
        module: Int,
        callback: (String?) -> Unit
    ) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("requestType", requestType)
            .addFormDataPart("apiUrl", apiUrl)
            .addFormDataPart("JSONFormat", JSONFormat)
            .addFormDataPart("module", module.toString())
            .build()

        val request = Request.Builder()
            .url("http://127.0.0.1:5000/integrationCode")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(null)
                    } else {
                        val responseBody = response.body?.string()
                        callback(responseBody)
                    }
                }
            }
        })
    }
}
