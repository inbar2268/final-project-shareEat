package com.idz.colman24class1.model.networking
import com.example.shareeat.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class RecipesInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-rapidapi-key", BuildConfig.TASTY_ACCESS_TOKEN)
            .addHeader("x-rapidapi-host", BuildConfig.TASTY_HOST)
            .addHeader("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
