package com.carota.html;

import android.os.Build;

import java.lang.reflect.Method;

public final class ReflectionLimitUtil {
    private static Object sVMRuntime;
    private static Method setHiddenApiExemptions;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
                setHiddenApiExemptions.setAccessible(true);
                sVMRuntime = getRuntime.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean clearLimit() {
        if (sVMRuntime == null || setHiddenApiExemptions == null) {
            return false;
        }
        try {
            setHiddenApiExemptions.invoke(sVMRuntime, new Object[]{new String[]{"L"}});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
