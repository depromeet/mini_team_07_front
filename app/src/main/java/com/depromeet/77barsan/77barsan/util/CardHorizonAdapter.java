package com.depromeet.robo77.robo77.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.depromeet.robo77.robo77.R;
import com.depromeet.robo77.robo77.model.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardHorizonAdapter extends RecyclerView.Adapter<CardHorizonAdapter.CardHolder>
        implements ItemTouchHelperAdapter{

    private Context context;
    private List<Card> items;
    private CardSendListener listener;

    private CardSwitcher switcher;

    public CardHorizonAdapter(Context context, CardSendListener listener) {
        this.context = context;
        this.listener = listener;

        items = new ArrayList<>();
        switcher = new CardSwitcher(context);
    }

    public void setItems(List<Card> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void addItem(Card card){
        items.add(card);
        notifyDataSetChanged();
    }

    public void clearItem(){
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public CardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.card_item_layout, parent, false);
        return new CardHolder(v);
    }

    @Override
    public void onBindViewHolder(CardHolder holder, int position) {
        // 번호에 맞게 이미지 설정
        holder.imageView.setImageResource(switcher.getUserCardImage(items.get(position).getCardType() ,items.get(position).getCardNum()));

    }

    @Override
    public int getItemCount() {
        return (items != null) ? items.size() : 0;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition){
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        // send card
        listener.cardSend(position, items.get(position));
        //items.remove(position);
        //notifyItemRemoved(position);
    }

    public void deleteItem(int pos){
        items.remove(pos);
        notifyItemRemoved(pos);
    }

    class CardHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public CardHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.card_text_view);

            itemView.setOnFocusChangeListener((v, hasFocus) -> {
                if(hasFocus){
                    v.setScaleX(1.5f);
                    v.setScaleY(1.5f);
                    itemView.invalidate();
                   // itemView.bringToFront();
                   // ViewCompat.setElevation(itemView, 1);
                }else{
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                    //ViewCompat.setElevation(itemView, 0);
                }
            });


        }

    }



}
