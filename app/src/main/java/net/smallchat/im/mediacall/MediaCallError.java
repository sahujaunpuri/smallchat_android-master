package net.smallchat.im.mediacall;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by it on 2017/8/19.
 */

public class MediaCallError implements Parcelable {
    public int errorCode;
    public String errorMsg;
    public static final Creator<MediaCallError> CREATOR = new Creator() {
        public final MediaCallError createFromParcel(Parcel var1) {
            return new MediaCallError(var1, 0);
        }

        public final MediaCallError[] newArray(int var1) {
            return new MediaCallError[var1];
        }
    };

    public MediaCallError() {
        this(-1, "未知错误");
    }

    public MediaCallError(int var1, String var2) {
        this.errorCode = var1;
        this.errorMsg = var2;
    }

    private MediaCallError(Parcel var1, int i) {
        this.errorCode = var1.readInt();
        this.errorMsg = var1.readString();
    }

    public String toString() {
        return "[errorCode : " + this.errorCode + " , errorMsg : " + this.errorMsg + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
    }
}
