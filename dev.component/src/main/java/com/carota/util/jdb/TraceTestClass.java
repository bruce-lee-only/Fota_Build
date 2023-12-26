/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util.jdb;

import android.util.Log;

public class TraceTestClass {

    private TraceTestInterface ti = new TraceInterface();

    public static class TraceInterStatic {
        public void print () {
            log(getClass().getSimpleName(), getClass().getName());
        }
    }

    public class TraceInter {
        public void print () {
            log(getClass().getSimpleName(), getClass().getName());
        }
    }

    public class TraceInterface implements TraceTestInterface {

        @Override
        public void print() {
            log(getClass().getSimpleName(), getClass().getName());
        }
    }

    public void print() {
        log(getClass().getSimpleName(), getClass().getName());
        new TraceInterStatic().print();
        new TraceInter().print();
        ti.print();
    }

    private static void log(String tag, String msg) {
        new Throwable().printStackTrace();
        String realMsg = new Throwable().getStackTrace()[2].getClassName() + "; " + msg;
        Log.i(tag, realMsg);
    }
}
