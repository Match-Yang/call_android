package im.zego.callsdk.model;

import com.google.gson.annotations.SerializedName;

public class ZegoRoomInfo {

    // room ID
    @SerializedName("id")
    private String roomID;
    // room name
    @SerializedName("name")
    private String roomName;

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public String toString() {
        return "ZegoRoomInfo{" +
            "roomID='" + roomID + '\'' +
            ", roomName='" + roomName + '\'' +
            '}';
    }
}