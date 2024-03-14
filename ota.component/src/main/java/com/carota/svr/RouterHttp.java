/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.svr;

import android.content.Context;
import android.text.TextUtils;

import com.carota.component.BuildConfig;
import com.carota.util.MimeType;
import com.momock.util.Logger;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.HttpStatusCode;
import fi.iki.elonen.ILinkInterceptor;
import fi.iki.elonen.NanoHTTPD;

public class RouterHttp extends NanoHTTPD implements IRouterHttp, ILinkInterceptor {

    private final IHttpDispatcher mDispatcher;
    private final TransferEncoder mEncoder;
    private Context mContext;

    public RouterHttp(Context context, int port) {
        super(port);
        mContext = context.getApplicationContext();
        mDispatcher = new HttpDispatcher();
        mEncoder = new TransferEncoder(BuildConfig.SALT);
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            HttpResp hr = mDispatcher.process(session, session.getBody());
            return null != hr ? hr.getNanoRsp() : null;
        } catch (UnsupportedOperationException uoe) {
            return newFixedLengthResponse(HttpStatusCode.BAD_REQUEST, "Not Found Handler");
        } catch (Exception e) {
            return newFixedLengthResponse(HttpStatusCode.INTERNAL_ERROR, "IE HD : " + e.getMessage());
        }
    }

    @Override
    public boolean startServer() {
        try {
            start(SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean stopServer() {
        stop();
        return true;
    }

    @Override
    public void setRequestHandler(String host, String path, IHttpHandler handler) {
        mDispatcher.setHandler(host, path, handler);
    }

    @Override
    public int getPort() {
        return super.getListeningPort();
    }

    @Override
    public boolean isRunning() {
        return super.wasStarted();
    }

    @Override
    protected ILinkInterceptor getLinkInterceptor() {
        return this;
    }

    @Override
    public byte[] onRecv(IHTTPSession s, byte[] data, String dataType) throws IOException {
        // Logger.error("onLinkRecv @ " + dataType);
        Map<String, String> header = s.getHeaders();
        try {
            return mEncoder.decodeFromByte(data, header.get("seed"));
        } catch (Exception e) {
            throw new IOException("Recv Body Error");
        }
    }

    @Override
    public RawData onSend(IHTTPSession s, byte[] raw, String dataType) throws IOException {
        // Logger.error("onLinkSend @ " + dataType);
        Map<String, String> header = s.getHeaders();
        try {
            String seed = header.get("seed");
            if(TextUtils.isEmpty(seed)) {
                return new RawData(new byte[0], null, null);
            }
            byte[] payload = mEncoder.encodeToByte(raw, seed);

            String type = null == dataType ? MimeType.CS_PBA : MimeType.CS_TXT;
            return new RawData(payload, type, seed);
        } catch (Exception e) {
            throw new IOException("Send Body Error");
        }
    }
}
