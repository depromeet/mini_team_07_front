package com.depromeet.robo77.robo77.model;

/**
 * Created by dongseok on 2018. 5. 7..
 */

public class Card {
    private int cardType;
    private int cardNum;
    private int cardId;


    public Card() {
    }

    public Card(int cardType, int cardNum, int cardId) {
        this.cardType = cardType;
        this.cardNum = cardNum;
        this.cardId = cardId;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public int getCardNum() {
        return cardNum;
    }

    public void setCardNum(int cardNum) {
        this.cardNum = cardNum;
    }
}
