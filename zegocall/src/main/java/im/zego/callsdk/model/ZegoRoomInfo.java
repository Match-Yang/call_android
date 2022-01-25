package im.zego.callsdk.model;

import com.google.gson.annotations.SerializedName;

public class ZegoRoomInfo {

    // room ID
    @SerializedName("id")
    public String roomID;
    // room name
    @SerializedName("name")
    public String roomName;


    @Override
    public String toString() {
        return "ZegoRoomInfo{" +
            "roomID='" + roomID + '\'' +
            ", roomName='" + roomName + '\'' +
            '}';
    }
}