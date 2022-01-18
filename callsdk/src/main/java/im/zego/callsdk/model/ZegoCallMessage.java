package im.zego.callsdk.model;


import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * { "actionType":2, "target": ["123"], "content": { "user_info": {id:"123", name:"haha"}, "response_type": 1, } } .
 */
public class ZegoCallMessage {

    public static final int CALL = 1;
    public static final int CANCEL_CALL = 2;
    public static final int RESPONSE_CALL = 3;
    public static final int END_CALL = 4;

    @SerializedName("actionType")
    public int actionType;
    @SerializedName("target")
    public List<String> target;
    @SerializedName("content")
    public ContentBean content;

    public static class ContentBean {

        @SerializedName("user_info")
        public UserInfoBean userInfo;
        @SerializedName("response_type")
        public ZegoResponseType responseType;
        @SerializedName("call_type")
        public ZegoCallType callType;
    }

    public static class UserInfoBean {

        @SerializedName("id")
        public String userID;
        @SerializedName("name")
        public String name;

        public UserInfoBean(String userID, String name) {
            this.userID = userID;
            this.name = name;
        }
    }
}
