package com.depromeet.robo77.robo77.util;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ItemDecorator extends RecyclerView.ItemDecoration {

    private final int mSpace;
    private int type;
    private int lastposition;

    /* type = 1 유저
    *       = 2 딜러
    *       */
    public ItemDecorator(int space, int type, int lastposition) {
        this.mSpace = space;
        this.type = type;
        this.lastposition = lastposition;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        switch(type){
            case 1:
                if(position != state.getItemCount()-1)outRect.set(-70, 0, 0, 0);

                break;
            case 2:
                outRect.set(-753, 0, 0, 0);
                break;
        }

        // 딜러 화면일때 마지막 포지션 예외 처리
        if(type == 2 && position == lastposition-1){
            outRect.set(0, 0, 0, 0);
        }
    }


}
