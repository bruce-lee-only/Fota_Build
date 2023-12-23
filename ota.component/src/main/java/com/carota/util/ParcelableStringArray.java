/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;

public class ParcelableStringArray implements Parcelable {

    public long arg;
    private String[] mPayload;

    public ParcelableStringArray(String[] data) {
        this(data, 0);
    }

    public ParcelableStringArray(String[] data, long arg) {
        mPayload = data;
        this.arg = arg;
    }

    private ParcelableStringArray(Parcel src) {
        arg = src.readLong();
        int length = src.readInt();
        if(length > 0){
            mPayload = new String[length];
            src.readStringArray(mPayload);
        } else {
            mPayload = null;
        }
    }

    public static final Creator<ParcelableStringArray> CREATOR = new Creator<ParcelableStringArray>() {
        @Override
        public ParcelableStringArray createFromParcel(Parcel in) {
            return new ParcelableStringArray(in);
        }

        @Override
        public ParcelableStringArray[] newArray(int size) {
            return new ParcelableStringArray[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(arg);
        if(null == mPayload) {
            dest.writeInt(0);
        } else {
            dest.writeInt(mPayload.length);
            dest.writeStringArray(mPayload);
        }
    }

    public String[] getData() {
        return mPayload;
    }

}
