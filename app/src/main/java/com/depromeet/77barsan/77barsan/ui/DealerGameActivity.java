package com.depromeet.robo77.robo77.ui;

import android.graphics.drawable.AnimationDrawable;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.depromeet.robo77.robo77.R;
import com.depromeet.robo77.robo77.model.Card;
import com.depromeet.robo77.robo77.util.CardSendListener;
import com.depromeet.robo77.robo77.util.CardSwitcher;
import com.depromeet.robo77.robo77.util.DealerCardHorizonAdapter;
import com.depromeet.robo77.robo77.util.ItemDecorator;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;

public class DealerGameActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {

    private static final String TAG = DealerGameActivity.class.getName();

    /**
     * Dealer
     * <p>
     * SEND
     * 1. 방 생성 ( Room id )
     * 1. 게임 시작 신호( Room id )
     * 2. 11의 배수 ( Room id, User id )
     * 3. 77 Over ( Room id, User id )
     * 4. Pass ( Room id )
     * <p>
     * GET
     * 1. 게임 시작 성공 ( 총합 )
     * 2. Card 받기 ( total, card, userId )
     * 3. 게임 한 판 종료
     * 4. 게임 완전 종료
     */

    private enum RoomState {
        NONE,
        OPEN,
        START,
        SET_START,
        SET_END,
        GAME_CLOSE
    }

    private RoomState state = RoomState.NONE;

    private int roomId;
    private Socket socket;
    private final String addr = "";

    private ConstraintLayout createRoomLayout;
    private ConstraintLayout gameRoomLayout;

    private ImageView startButton;
    private AnimationDrawable ani;

    private ImageView dealerDropCardImageView;
    private ImageView dealerDropCardImageView1;

    private Animation animation;

    private RecyclerView cardListRecyclerView;
    private DealerCardHorizonAdapter adapter;

    private CardSendListener listener = new CardSendListener() {
        @Override
        public void cardSend(int pos, Card card) {

        }
    };

    private int total;

    private NfcAdapter nfcAdapter;

    private Handler handler;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dealer_game);

        if (getIntent() != null) {
            roomId = getIntent().getIntExtra("RoomId", -1);
            if (roomId == -1) finish();
        }

        initView();
//        initNFC();
        initSocket();
    }

    private void initView() {
        createRoomLayout = findViewById(R.id.dealerCreateRoomLayout);
        gameRoomLayout = findViewById(R.id.dealerGameRoomLayout);

        startButton = findViewById(R.id.create_start);
        dealerDropCardImageView = findViewById(R.id.dealerDropCardImageView);
        dealerDropCardImageView1 = findViewById(R.id.dealerDropCardImageView1);
        startAnimation();

        cardListRecyclerView = findViewById(R.id.dealerGameRoomRecyclerView);


        startButton.setOnClickListener(v -> {

            if (state == RoomState.OPEN) {

                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("socketId", "dealer");
                    jsonObject.put("roomId", roomId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                socket.emit("gameStart", jsonObject);

                state = RoomState.START;
                showGameView();
            }
            // showGameView();
        });
    }


    private void initSocket() {
        socket = IO.socket(URI.create(addr));
        connect();
        setStart();
        cardInfoToDealer();
        setOver();
        gameOver();
        socket.connect();
    }

    private void connect() {
        socket.on(Socket.EVENT_CONNECT, args -> {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("roomId", roomId);
            } catch (Exception e) {

            }
            socket.emit("createRoom", jsonObject);
            state = RoomState.OPEN;
        });
    }

    private void setStart() {
        socket.on("setStart", args -> {
            total = 0;
            state = RoomState.SET_START;
        });
    }

    private void cardInfoToDealer() {
        socket.on("cardInfoToDealer", args -> {

            //TODO 받은 카드 보여주기
            JSONObject obj = (JSONObject) args[0];
            try {
                int cardType = obj.getInt("cardType");
                int cardNum = obj.getInt("cardNum");
                int cardId = obj.getInt("cardId");
                String userId = obj.getString("socketId");

                // 카드 UI 변경
                // changeCardImage(cardType, cardNum);

                switch (cardType) {
                    case 0:
                        total += cardNum;
                        break;
                    case 1:
                        break;
                }

                JSONObject object = new JSONObject();

                if (total >= 77) {
                    gameOver77ChangeCard();
                    object.put("roomId", roomId);
                    object.put("socketId", userId);
                    socket.emit("gameOver77", object);
                } else if (cardType == 1) {
                    object.put("roomId", roomId);
                    object.put("socketId", userId);
                    socket.emit("noCondition", object);
                } else if (total % 11 == 0 && total != 0) {
                    test(cardType, cardNum);
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "11의 배수입니다", Toast.LENGTH_SHORT).show();
                    });
                    object.put("roomId", roomId);
                    object.put("socketId", userId);
                    socket.emit("multiplesOf11", object);
                } else {
                    test(cardType, cardNum);
                    object.put("roomId", roomId);
                    object.put("socketId", userId);
                    socket.emit("noCondition", object);
                }

                Log.d(TAG, object.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

        });
    }

    // 이미지 변경
    private void changeCardImage(int type, int num) {
        runOnUiThread(() -> {
            CardSwitcher switcher = new CardSwitcher(this);
            dealerDropCardImageView.setImageResource(switcher.getDealerCardImage(type, num));
        });
    }

    private void gameOver77ChangeCard() {
        runOnUiThread(() -> {
            dealerDropCardImageView1.setImageResource(0);
            dealerDropCardImageView.setImageResource(0);
        });
    }

    private void test(int type, int num) {
        CardSwitcher switcher = new CardSwitcher(this);
        runOnUiThread(() -> {
            dealerDropCardImageView1.setImageResource(switcher.getDealerCardImage(type, num));
            animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    dealerDropCardImageView.setImageResource(switcher.getDealerCardImage(type, num));
                    dealerDropCardImageView1.setImageResource(0);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            dealerDropCardImageView1.setAnimation(animation);

        });
    }

    private void setOver() {
        socket.on("setOver", args -> {
            state = RoomState.SET_END;
            total = 0;
            runOnUiThread(() -> {
                dealerDropCardImageView.setImageResource(0);
            });
            //TODO 카드 리셋 (카드 댁 맨 처음 화면)
//            JSONObject obj = (JSONObject)args[0];
//            try {
//                String userId = obj.getString("socketId");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
        });
    }

    private void gameOver() {
        socket.on("gameOver", args -> {
            state = RoomState.GAME_CLOSE;

            runOnUiThread(() -> {
                Toast.makeText(this, "게임이 종료되었습니다.", Toast.LENGTH_SHORT).show();
            });
            finish();
        });
    }

    // 게임 시작 했을 때
    private void showGameView() {
        createRoomLayout.setVisibility(View.INVISIBLE);
        gameRoomLayout.setVisibility(View.VISIBLE);
        ani.stop();

        cardListRecyclerView.setHasFixedSize(true);
        cardListRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);


        adapter = new DealerCardHorizonAdapter(this, listener);
        cardListRecyclerView.setAdapter(adapter);

        // dummy
        for (int i = 0; i < 10; i++) {
            adapter.addItem(new Card(i, i, i));
        }
        cardListRecyclerView.setLayoutManager(layoutManager);
        cardListRecyclerView.addItemDecoration(new ItemDecorator(-160, 2, 10));

        // 타이머 해제
        timer.cancel();
        handler = null;
    }

    private void startAnimation() {

        //TODO 버튼 애니메이션 시작
        startButton.setImageDrawable(getResources().getDrawable(R.drawable.game_start));
        ani = (AnimationDrawable) startButton.getDrawable();

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
                    ani.start();

                });
                handler.postDelayed(() -> {
                    ani.stop();

                }, 1800);
            }
        };
    }

    private void initNFC() {
        // TODO NFC활성화 유저들이 NFC로 접속되게함

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // nfc 지원하지 않을 경우 해
        if (nfcAdapter == null) {
            Toast.makeText(this, "이 기기는 NFC를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            nfcAdapter = null;
        }
        nfcAdapter.setNdefPushMessageCallback(this, this);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        return null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        startButton.setImageDrawable(null);
        ani.stop();
        socket.disconnect();
        socket.close();
    }
}
