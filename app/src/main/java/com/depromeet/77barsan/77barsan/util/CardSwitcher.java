package com.depromeet.robo77.robo77.util;

import android.content.Context;

import com.depromeet.robo77.robo77.R;

public class CardSwitcher {

    private Context context;


    public CardSwitcher(Context context) {
        this.context = context;
    }

    // 딜러
    public int getDealerCardImage(int type, int num) {

       int[] images = {R.mipmap.d_0, R.mipmap.d_x1, R.mipmap.d_2, R.mipmap.d_3, R.mipmap.d_4,
                R.mipmap.d_5, R.mipmap.d_6, R.mipmap.d_7,  R.mipmap.d_8, R.mipmap.d_9};

        int r = 0;
        if(type == 0){
            if(num < 10 && num >= 0){
                r = images[num];
            }else{
                switch (num) {
                    case 10:
                        r = R.mipmap.d_10;
                        break;
                    case 11:
                        r = R.mipmap.d_11;
                        break;
                    case 22:
                        r = R.mipmap.d_22;
                        break;
                    case 33:
                        r = R.mipmap.d_33;
                        break;
                    case 44:
                        r = R.mipmap.d_44;
                        break;
                    case 55:
                        r = R.mipmap.d_55;
                        break;
                    case 66:
                        r = R.mipmap.d_66;
                        break;
                    case 76:
                        r = R.mipmap.d_76;
                        break;
                    case -1:
                        r = R.mipmap.d__1;
                        break;
                    case -3:
                        r = R.mipmap.d__3;
                        break;
                    case -10:
                        r = R.mipmap.d__10;
                        break;
                }
            }
        }else{
            r = images[1];
        }

        return r;
    }

    // 유저
    public int getUserCardImage(int type, int num){
        int r = 0;
        if(type == 0) {
            switch (num) {
                case 0:
                    r = R.mipmap.u_0;
                    break;

                case 2:
                    r = R.mipmap.u_2;
                    break;
                case 3:
                    r = R.mipmap.u_3;
                    break;
                case 4:
                    r = R.mipmap.u_4;
                    break;
                case 5:
                    r = R.mipmap.u_5;
                    break;
                case 6:
                    r = R.mipmap.u_6;
                    break;
                case 7:
                    r = R.mipmap.u_7;
                    break;
                case 8:
                    r = R.mipmap.u_8;
                    break;
                case 9:
                    r = R.mipmap.u_9;
                    break;
                case 10:
                    r = R.mipmap.u_10;
                    break;
                case 11:
                    r = R.mipmap.u_11;
                    break;
                case 22:
                    r = R.mipmap.u_22;
                    break;
                case 33:
                    r = R.mipmap.u_33;
                    break;
                case 44:
                    r = R.mipmap.u_44;
                    break;
                case 55:
                    r = R.mipmap.u_55;
                    break;
                case 66:
                    r = R.mipmap.u_66;
                    break;
                case 76:
                    r = R.mipmap.u_76;
                    break;
                case -1:
                    r = R.mipmap.u__1;
                    break;
                case -3:
                    r = R.mipmap.u__3;
                    break;
                case -10:
                    r = R.mipmap.u__10;
                    break;
            }
        }else{
            r = R.mipmap.u_x1;
        }

        return r;
    }
}

