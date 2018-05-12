package com.depromeet.robo77.robo77.ui;

import android.graphics.drawable.AnimationDrawable;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.depromeet.robo77.robo77.R;
import com.depromeet.robo77.robo77.model.Card;
import com.depromeet.robo77.robo77.model.User;
import com.depromeet.robo77.robo77.util.CardHorizonAdapter;
import com.depromeet.robo77.robo77.util.CardSendListener;
import com.depromeet.robo77.robo77.util.ItemDecorator;
import com.depromeet.robo77.robo77.util.SimpleItemTouchHelperCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;


/* 방 참가*/
public class UserRoomActivity extends AppCompatActivity {

    private static final String TAG = UserRoomActivity.class.getName();

    /**
     * USER
     * <p>
     * SEND
     * 1. 게임 참가 : joinRoom
     * 2. 카드 제출
     * <p>
     * GET
     * 1. User ID 받기 : joinComplete
     * 2. 게임 시작 ( Restart )
     * 3. 유저 턴 시작
     * 4. 유저 턴 종료 ( 하트 손실여부, 카드 받기, 턴 종료 )
     * 5. 게임 한판 종료
     * 6. 게임 완전 종료
     */
    private enum UserState {
        NONE,       // 방 입장 전
        JOIN,       // 방 입장 완료
        START,      // 게임 참가중
        MY_TURN,
        SET_END,    // 한 게임 종료
        GAME_END    // 게임 종료
    }

    // TODO 게임 진행 중 => 뒤로가기 금지 ( 게임아웃 X )

    private UserState state = UserState.NONE;

    private int roomId;
    private User user;
    private Socket socket;
    private final String addr = "";        // port 포함 주소.

    private ConstraintLayout readyLayout;
    private ConstraintLayout gameLayout;
    private RecyclerView recyclerView;

    private CardHorizonAdapter adapter;

    private ImageView heart1View;
    private ImageView heart2View;
    private ImageView heart3View;

    private int heart = 3;

    private int emitPos = -1;

    // 내 차례일 경우 카드 내기
   private CardSendListener listener = new CardSendListener() {
        @Override
        public void cardSend(int pos, Card card) {
            if (socket != null && state == UserState.MY_TURN) {
                emitPos = pos;
                try {

                    JSONObject obj = new JSONObject();
                    obj.put("socketId", user.getSocketId());
                    obj.put("roomId", roomId);
                    obj.put("cardType", card.getCardType());
                    obj.put("cardId", card.getCardId());
                    obj.put("cardNum", card.getCardNum());

                    socket.emit("emitCard", obj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private NfcAdapter nfcAdapter;

    private AnimationDrawable animationDrawable;
    private ImageView waitingImage;

    private Handler handler;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_room);

        getRoomNumber();

        initView();
        initSocket();
        // TODO Recycler Event.
    }

    private void getRoomNumber() {
        if (getIntent() != null) {
            roomId = getIntent().getIntExtra("roomId", -1);
            if (roomId == -1) finish();
        }
    }

    private void initView() {

        heart1View = findViewById(R.id.userGameHeart_one);
        heart2View = findViewById(R.id.userGameHeart_two);
        heart3View = findViewById(R.id.userGameHeart_three);

        waitingImage = findViewById(R.id.user_waiting);
        startAnimation();

        readyLayout = findViewById(R.id.userReadyLayout);
        gameLayout = findViewById(R.id.userGameLayout);
        recyclerView = findViewById(R.id.userCardRecyclerView);

        recyclerView.setHasFixedSize(true);


        // TODO RecyclerView item OverLap
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new ItemDecorator(-160, 1, 5));

        adapter = new CardHorizonAdapter(this, listener);
        recyclerView.setAdapter(adapter);


        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    private void startAnimation() {
        waitingImage.setImageDrawable(getResources().getDrawable(R.drawable.user_waiting_animation));
        animationDrawable = (AnimationDrawable) waitingImage.getDrawable();

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

                }, 2000);
            }
        };
    }

    private void gameReadyVisible() {
        runOnUiThread(() -> {
            // 애니메이션 종료
            waitingImage.setImageDrawable(null);
            animationDrawable.stop();


            readyLayout.setVisibility(View.GONE);
            gameLayout.setVisibility(View.VISIBLE);
        });

        // 타이머 해제
        timer.cancel();
        handler = null;
    }

    private void initSocket() {
        socket = IO.socket(URI.create(addr));
        // Event
        connect();
        setStart();
        turnStart();
        turnEndAndUpdate();
        setOver();
        gameOver();
        emitComplete();

        socket.connect();
    }

    private void emitComplete(){
        socket.on("emitComplete", args->{
             Log.d(TAG, "emitComplete");
             adapter.deleteItem(emitPos);
        });
    }

    private void connect() {
        socket.on(Socket.EVENT_CONNECT, args -> {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("roomId", roomId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("joinRoom", jsonObject);

        });
    }


    /* gameStart 가 실행 될 경우 setStart로 카드들을 받아옴  */
    private void setStart() {

        socket.on("setStart", args -> {
            state = UserState.START;

            user = new User();
            gameReadyVisible();

            JSONObject obj = (JSONObject) args[0];

            try {
                user.setHeart(3);
                // user.setHeart(obj.getInt("heart"));
                // user.setOrder(obj.getInt("order"));
                user.setSocketId(obj.getString("socketId"));

                List<Card> cards = new ArrayList<>();

                JSONArray array = obj.getJSONArray("cards");
                Log.d(TAG, array.toString());
                for (int i = 0; i < array.length(); i++) {
                    int type = array.getJSONObject(i).getInt("cardType");
                    int num = array.getJSONObject(i).getInt("cardNum");
                    int cardId = array.getJSONObject(i).getInt("cardId");
                    cards.add(new Card(type, num, cardId));

                    Log.e("setStart", String.valueOf(num));
                    if (i+1 == array.length()) {
                        runOnUiThread(() -> adapter.setItems(cards));
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void turnStart() {
        socket.on("turnStart", args -> {
            state = UserState.MY_TURN;

            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "당신 차례입니다. 카드를 제출해주세요.", Toast.LENGTH_SHORT).show());

        });
    }

    private void turnEndAndUpdate() {

        socket.on("turnEndAndUpdate", args -> {

            state = UserState.START;
            JSONObject obj = (JSONObject) args[0];
            // card
            try {
                int calValue = obj.getInt("heart");
                user.caluHeart(calValue);
                if(calValue == -1) {
                    runOnUiThread(() -> {
                        switch (user.getHeart()) {
                            case 2:
                                heart3View.setVisibility(View.INVISIBLE);
                                break;
                            case 1:
                                heart2View.setVisibility(View.INVISIBLE);
                                break;
                            case 0:
                                heart1View.setVisibility(View.INVISIBLE);
                                break;
                        }
                    });
                }


                int type = obj.getInt("cardType");
                int num = obj.getInt("cardNum");
                int id = obj.getInt("cardId");
                String userid = obj.getString("socketId");

                runOnUiThread(() -> adapter.addItem(new Card(type, num, id)));

                Log.e("turnend", userid + " " + String.valueOf(num));
                // TODO 턴 종료 알림.

            } catch (JSONException e) {
                e.printStackTrace();
            }

        });
    }

    private void setOver() {
        socket.on("setOver", args -> {
            state = UserState.SET_END;
            //runOnUiThread(() ->  adapter.clearItem());
        });
    }

    private void gameOver() {
        socket.on("gameOver", args -> {
            state = UserState.GAME_END;
            // TODO GAME OVER Dialog
            // 1. 누가 꼴지인지 알려주기 & Socket . disconnect
            // 2. 확인 누르면 MainActivity 로 이동
            socket.disconnect();
            finish();
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.close();
    }
}
