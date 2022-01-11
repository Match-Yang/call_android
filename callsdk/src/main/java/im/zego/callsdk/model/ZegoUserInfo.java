package im.zego.callsdk.model;

import java.util.Objects;

/**
 * Class user information.
 * <p>Description: This class contains the user related information.</>
 */
public class ZegoUserInfo {

    /**
     * User ID, refers to the user unique ID, can only contains numbers and letters.
     */
    private String userID;

    /**
     * User name, cannot be null.
     */
    private String userName;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ZegoUserInfo that = (ZegoUserInfo) o;

        if (!Objects.equals(userID, that.userID)) {
            return false;
        }
        return Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        int result = userID != null ? userID.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ZegoUserInfo{" +
            "userID='" + userID + '\'' +
            ", userName='" + userName + '\'' +
            '}';
    }
}