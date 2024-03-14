package com.carota.hmi.dispacther;

import android.content.Context;

public abstract class BaseDataDispacher {

    protected final Context mContext;
    protected BaseDataDispacher(Context context) {
        mContext = context;
    }

}
