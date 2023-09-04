package com.brainx.limitwatcher


object BaseApiManager {

    //    private var baseRetrofit: Retrofit? = null
//    private var tenantRetrofit: Retrofit? = null
//    private var essentialRetrofit: Retrofit? = null
    private val TAG = "ServiceGenerator"
    private val TIME = 100L
/*
    fun getRetrofit(): Retrofit {


        val baseUrl = "https://roads.googleapis.com/v1/"

        if (baseRetrofit == null) {

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = createHttpClient()
                .addInterceptor(logging)
                .connectTimeout(TIME, TimeUnit.SECONDS)
                .readTimeout(TIME, TimeUnit.SECONDS)
            baseRetrofit = getRetrofitObject(baseUrl, httpClient.build())
        }
        return baseRetrofit!!
    }

    private fun getRetrofitObject(baseUrl: String, client: OkHttpClient): Retrofit? {

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .create()
                )
            )
            .build()
    }

    private fun createHttpClient(): OkHttpClient.Builder {

        var httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .build()

            val response = chain.proceed(request)
            response
        }

        httpClient.build()
        return httpClient
    }*/
}