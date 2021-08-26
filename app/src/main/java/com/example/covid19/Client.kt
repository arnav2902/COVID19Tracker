package com.example.covid19

import okhttp3.OkHttpClient
import okhttp3.Request

object Client {

    private val OkHttpClient = okhttp3.OkHttpClient()
    private val request = Request.Builder().url("https://api.covid19india.org/data.json").build()

    val api = OkHttpClient.newCall(request)
}