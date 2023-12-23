# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepattributes Signature, InnerClasses, EnclosingMethod, Exceptions
# core API
-keep class com.carota.core.ClientState {
   public *;
}

-keep class com.carota.core.ICheckCallback {
   public *;
}

-keep class com.carota.core.IDownloadCallback {
   public *;
}

-keep class com.carota.core.IInstallViewHandler {
   public *;
}

-keep class com.carota.core.ICoreStatus {
   public *;
}

-keep class com.carota.core.VehicleCondition* {
   public *;
}

-keep class com.carota.core.ISession {
   public *;
}

-keep class com.carota.core.ITask {
   public *;
}

-keep interface com.carota.sda.ISlaveMethod {
    public protected *;
}

-keep class com.carota.CarotaClient* {
    public *;
}

-keep class com.carota.CarotaOffline* {
    public *;
}

-keep class com.carota.DaemonService {
    public *;
}

-keep class com.carota.MainService {
    public protected *;
}

-keep class com.carota.InstallToast {
    public protected *;
}

-keep class com.carota.sota.* {
    public protected *;
}

-keep class com.carota.sota.store.AppData {
    public protected *;
}

-keep class com.carota.sota.store.AppInfo {
    public protected *;
}

#-keep class com.carota.*{
#    public protected *;
#}

# CONFIGURE FOR RELEASE APK

# System API
-keep class android.os.* {
   *;
}

# ok http3
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# AIDL API
-keep class com.carota.agent.IRemoteAgent* {*;}

-keep class com.carota.agent.IRemoteAgentCallback* {*;}

-keep class com.carota.agent.IRemoteAgentService* {*;}

-keep class com.carota.core.ILocalService* {*;}

#protocol-buffers files
#-keep class com.carota.protobuf**{*;}
