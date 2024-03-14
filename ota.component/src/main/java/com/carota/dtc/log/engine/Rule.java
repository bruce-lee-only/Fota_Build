/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dtc.log.engine;

import com.carota.dtc.log.data.Piece;
import com.momock.util.Logger;

public abstract class Rule {

    public static final int TARGET_WHOLE = 0;
    public static final int TARGET_TAG = 1;
    public static final int TARGET_TIME = 2;
    public static final int TARGET_APP = 3;
    public static final int TARGET_LEVEL = 4;
    public static final int TARGET_MESSAGE = 5;

    public static final int MESSAGE_FMT_SELF = 1;
    public static final int MESSAGE_FMT_OTHER = 2;

    private int mTarget;
    private Rule mAttached;

    public Rule(int target) {
        mTarget = target;
        mAttached = null;
    }

    public Rule setAttached(Rule attached) {
        if(null == mAttached) {
            mAttached = attached;
        } else {
            mAttached.setAttached(attached);
        }
        return this;
    }

    public int getTarget() {
        return mTarget;
    }

    public final boolean match(Piece piece) {
        String data = findTargetData(this, piece);
        if(onMatch(data)) {
            return null == mAttached || mAttached.match(piece);
        }
        return false;
    }
    protected abstract boolean onMatch(String data);

    private String findTargetData(Rule r, Piece data) {
        String ret = null;
        switch (r.getTarget()) {
            case Rule.TARGET_WHOLE:
                ret = data.getOrigin();
                break;
            case Rule.TARGET_TIME:
                ret = data.getTimestamp();
                break;
            case Rule.TARGET_APP:
                ret = data.getApp();
                break;
            case Rule.TARGET_TAG:
                ret = data.getTag();
                break;
            case Rule.TARGET_LEVEL:
                ret = data.getLevel();
                break;
            case Rule.TARGET_MESSAGE:
                ret = data.getMessage();
                break;
        }
        return ret;
    }
}
