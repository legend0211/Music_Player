package com.example.music_player;

import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    @Multipart
    @POST("/")
    Call<ResponseBody> uploadAudioFile(@Part MultipartBody.Part audioFile);

    @FormUrlEncoded
    @POST("/link")
    Call<ResponseBody> uploadLink(@Field("link") String link);


    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.0.5:5000")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build();
}
