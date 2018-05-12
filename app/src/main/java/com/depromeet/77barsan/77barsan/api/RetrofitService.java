package com.depromeet.robo77.robo77.api;


import com.depromeet.robo77.robo77.model.Room;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.POST;

public interface RetrofitService {

    // 방 만들기
    @POST("/room")
    Call<Room> getRoomId();

    /*@GET("/{Roomid}")
    Call<Room> getRoomId(
            @Path("Roomid") int Roomid
    );*/


    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("")
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create())).build();

}
