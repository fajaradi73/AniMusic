package id.fajarproject.animusic.data.network

import id.fajarproject.animusic.data.network.model.MusicModel
import id.fajarproject.animusic.utils.Constant
import id.fajarproject.animusic.utils.Util
import io.reactivex.Observable
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


/**
 * Created by Fajar Adi Prasetyo on 05/08/2020.
 */

interface ApiServiceInterface {

    @GET("listmusic")
    fun getMusic(): Observable<MusicModel>

    companion object Factory {
        fun create(): ApiServiceInterface {
            val retrofit = retrofit2.Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(Util.getOkHttp())
                .baseUrl(Constant.BaseUrlApi)
                .build()
            return retrofit.create(ApiServiceInterface::class.java)
        }

    }
}