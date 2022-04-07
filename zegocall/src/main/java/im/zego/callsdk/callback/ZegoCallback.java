package im.zego.callsdk.callback;

/**
 * Description: When the called method is asynchronous processing,If you are making and processing asynchronous
 * calls,the following callbacks will be triggered when a method has finished its execution and returns the execution
 * result.
 */
public interface ZegoCallback {

    /**
     * @param errorCode refers to the operation status code.<br> 0: Operation successful. <br> 100xxxx: The Express SDK
     *                  error code. For details, refer to the error code documentation.https://doc-en.zego.im/article/5548 <br> 600xxxx:
     */
    void onResult(int errorCode);
}