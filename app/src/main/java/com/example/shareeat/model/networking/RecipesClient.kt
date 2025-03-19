import com.example.shareeat.BuildConfig
import com.example.shareeat.model.networking.RecipesApi
import com.idz.colman24class1.model.networking.RecipesInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RecipesClient {
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(RecipesInterceptor())
            .build()
    }

    val recipesApiClient: RecipesApi by lazy {
        val retrofitClient = Retrofit.Builder()
            .baseUrl(BuildConfig.TASTY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofitClient.create(RecipesApi::class.java)
    }
}