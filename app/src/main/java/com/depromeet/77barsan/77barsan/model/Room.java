package com.depromeet.robo77.robo77.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ihoyong on 2018. 5. 5..
 */

public class Room {

    private int total;
    private int roomId;

    public Room() {
    }

    public Room(int total, int roomId) {
        this.total = total;
        this.roomId = roomId;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
