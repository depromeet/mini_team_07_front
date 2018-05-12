package com.depromeet.robo77.robo77.ui;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.depromeet.robo77.robo77.R;
import com.depromeet.robo77.robo77.api.RetrofitService;
import com.depromeet.robo77.robo77.model.Room;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*방 참가, 방 만들기 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private AnimationDrawable animationDrawable;
    private ImageView logo;

    private Handler handler;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {

        logo = findViewById(R.id.main_logo);
        startAnimation();
        // 방 만들기

        ImageButton create = findViewById(R.id.main_create);
        create.setOnClickListener(v -> {
            // 1. Get Room Id
            // 2. Send Room id
            RetrofitService retrofitService = RetrofitService.retrofit.create(RetrofitService.class);
            Call<Room> request = retrofitService.getRoomId();
            request.enqueue(new Callback<Room>() {
                @Override
                public void onResponse(Call<Room> call, Response<Room> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Success Room = " + response.body().getRoomId());
                        Intent intent = new Intent(MainActivity.this, DealerGameActivity.class);
                        intent.putExtra("RoomId", response.body().getRoomId());
                        startActivity(intent);

                    } else {
                        Log.d(TAG, "fail");
                    }
                }

                @Override
                public void onFailure(Call<Room> call, Throwable t) {
                    Log.d(TAG, "client error");
                }

            });


        });

        // 방 참가
        ImageButton join = findViewById(R.id.main_join);
        join.setOnClickListener(v -> {
            joinGame(new Room(0, 1));
        });

        // TODO NFC EVENT.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
        logo.setImageDrawable(null);
        animationDrawable.stop();
    }

    private void startAnimation() {
        logo.setImageDrawable(getResources().getDrawable(R.drawable.loading_animation));
        animationDrawable = (AnimationDrawable) logo.getDrawable();
        handler = new Handler();

        timer = new Timer();

        repeatTimer();


    }

    private void repeatTimer() {
        timer = new Timer();
        timer.schedule(repeatTimerTask(), 0, 5000);
    }

    private TimerTask repeatTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    animationDrawable.start();

                });
                handler.postDelayed(() -> {
                    animationDrawable.stop();

                }, 2200);
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();

    }

    @Override
    protected void onResume() {
        super.onResume();
        repeatTimer();
    }

    private void joinGame(Room room) {
        Intent intent = new Intent(MainActivity.this, UserRoomActivity.class);
        intent.putExtra("roomId", room.getRoomId());
        startActivity(intent);
    }
}
