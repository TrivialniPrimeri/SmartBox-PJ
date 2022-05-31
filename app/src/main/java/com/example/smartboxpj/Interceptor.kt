package com.example.smartboxpj

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import okhttp3.*
import java.lang.Exception

data class response(val accessToken: String)

class myInterceptor (private val ctx: Context) : Interceptor {

    private fun getNewAccessToken(): String{
        var tokenCookie = "";
        val cookieManager = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(ctx))

        val client = OkHttpClient.Builder().cookieJar(cookieManager).build()
        val request: Request =  Request.Builder().url("https://trivialciapi.maticsulc.com/auth/refresh").post(FormBody.Builder().build()).build()

        try {
            client.newCall(request).execute().use {resp ->
                val jsonResponse = Gson().fromJson(resp.body!!.string(), response::class.java)
                val cookie = Cookie.Builder()
                    .domain("trivialciapi.maticsulc.com")
                    .path("/")
                    .name("accessToken")
                    .value(jsonResponse.accessToken)
                    .httpOnly()
                    .expiresAt(System.currentTimeMillis() + 25000) //25sec
                    .build()

                tokenCookie = cookie.toString()

                }
                return tokenCookie

        } catch (e: Exception){
            println(e.localizedMessage)
        }
        return tokenCookie
        //call new token and retry
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        var req = chain.request()
        val resp = chain.proceed(req)

        if(resp.code == 401){
            resp.close()
            val newCookieValue = getNewAccessToken()
            val newReq = req.newBuilder()
                .addHeader("Cookie", newCookieValue)
                .build()
            return chain.proceed(newReq)
        } //end if

        return resp

    }


}