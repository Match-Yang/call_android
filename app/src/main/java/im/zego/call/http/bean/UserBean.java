package im.zego.call.http.bean;

import com.google.gson.annotations.SerializedName;

public class UserBean {

    @SerializedName("id")
    public String userID;
    @SerializedName("name")
    public String userName;
    @SerializedName("order")
    public String order;

    @Override
    public String toString() {
        return "UserBean{" +
            "userID='" + userID + '\'' +
            ", name='" + userName + '\'' +
            ", order='" + order + '\'' +
            '}';
    }
}
