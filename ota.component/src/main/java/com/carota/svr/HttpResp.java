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

import com.carota.util.MimeType;
import com.momock.util.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import fi.iki.elonen.HttpStatusCode;
import fi.iki.elonen.NanoHTTPD;

public abstract class HttpResp {

    public static class ByteResp extends HttpResp {

        private ByteResp(PrivStatusCode status, String mimeType, byte[] data) {
            super(NanoHTTPD.newFixedLengthResponse(status, mimeType, data));
        }
    }

    public static class SteamResp extends HttpResp {

        /**
         * @param mimeType
         * @param data
         * @param totalSize input stream total, it must be > 0
         * @param startFrom -1 if not used
         * @param endAt     -1 if not used
         * @return HTTP 200 if startFrom < 0, otherwise it will return HTTP 206
         */
        private SteamResp(NanoHTTPD.Response.IStatus status, String mimeType,
                          InputStream data, long totalSize, long startFrom, long endAt) {
            super(build(status, mimeType, data, totalSize, startFrom, endAt));
        }

        private static NanoHTTPD.Response build(NanoHTTPD.Response.IStatus status,
                                                String mimeType, InputStream data,
                                                long totalSize, long startFrom, long endAt) {
            if (null == data || totalSize < 0) {
                return null;
            }
            String type = mimeType;
            if (null == type) {
                type = MimeType.STREAM;
            }

            if (startFrom < 0) {
                return NanoHTTPD.newFixedLengthResponse(status, type, data, totalSize);
            }

            NanoHTTPD.Response rsp = null;
            try {
                if (totalSize > startFrom && startFrom == data.skip(startFrom)) {
                    long contentLength;
                    if (endAt < 0 || endAt >= totalSize) {
                        contentLength = totalSize - startFrom;
                        rsp = NanoHTTPD.newFixedLengthResponse(status, type, data, contentLength);
                        rsp.addHeader("Content-Range", "bytes " + startFrom + "-");
                    } else {
                        contentLength = endAt - startFrom + 1;
                        if (contentLength < 0) {
                            contentLength = 0;
                        }
                        rsp = NanoHTTPD.newFixedLengthResponse(status, type, data, contentLength);
                        rsp.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + totalSize);
                    }
                } else {
                    rsp = NanoHTTPD.newFixedLengthResponse(HttpStatusCode.RANGE_NOT_SATISFIABLE, null, null, 0);
                    rsp.addHeader("Content-Range", "bytes 0-0/" + totalSize);
                }
                rsp.addHeader("Accept-Ranges", "bytes");
            } catch (Exception e) {
                Logger.error(e);
            }
            return rsp;
        }
    }

    public static class ProxyResp extends HttpResp {

        public ProxyResp(int status, String mimeType,
                         Map<String, String> header, InputStream data, long totalSize) {
            super(build(status, header, mimeType, data, totalSize));
        }

        private static NanoHTTPD.Response build(final int status, Map<String, String> header,
                                                String mimeType, InputStream data, long totalSize) {
            String type = mimeType;
            if (null == type) {
                type = MimeType.TEXT;
            }
            NanoHTTPD.Response.IStatus statusWrap = new NanoHTTPD.Response.IStatus() {
                @Override
                public int getStatusCode() {
                    return status;
                }

                @Override
                public String getDescription() {
                    return status + " CSP";
                }
            };
            NanoHTTPD.Response rsp = NanoHTTPD.newFixedLengthResponse(statusWrap, type, data, totalSize);
            for(Map.Entry<String, String> entry : header.entrySet()) {
                rsp.addHeader(entry.getKey(), entry.getValue());
            }
            return rsp;
        }
    }

    public static HttpResp newInstance(PrivStatusCode status, byte[] data) {
        return new ByteResp(status, null, data);
    }

    public static HttpResp newInstance(NanoHTTPD.Response.IStatus status) {
        return newRawInstance(status, status.getDescription());
    }

    public static HttpResp newInstance(PrivStatusCode status, String json) {
        return new ByteResp(status, MimeType.JSON, null != json ? json.getBytes() : null);
    }

    public static HttpResp newRawInstance(InputStream data, long totalSize, Object extra) {
        long from = -1;
        long to = -1;
        if (extra instanceof NanoHTTPD.IHTTPSession) {
            NanoHTTPD.IHTTPSession session = (NanoHTTPD.IHTTPSession) extra;
            String range = session.getHeaders().get("range");
            // Logger.error("range = " + range);
            if (null != range && range.startsWith("bytes=")) {
                range = range.substring("bytes=".length());
                int minus = range.indexOf('-');
                if (minus > 0) {
                    try {
                        from = Long.parseLong(range.substring(0, minus));
                        to = Long.parseLong(range.substring(minus + 1));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return new SteamResp(from < 0 ? HttpStatusCode.OK : HttpStatusCode.PARTIAL_CONTENT,
                null, data, totalSize, from, to);
    }

    public static HttpResp newRawInstance(NanoHTTPD.Response.IStatus status, String txt) {
        String target = txt;
        if(null == target) {
            target = "";
        }

        ByteArrayInputStream data = new ByteArrayInputStream(target.getBytes());
        return new SteamResp(status, MimeType.TEXT, data, data.available(), -1, -1);
    }

    private NanoHTTPD.Response mNanoRsp;

    private HttpResp(NanoHTTPD.Response rsp) {
        mNanoRsp = rsp;
    }

    NanoHTTPD.Response getNanoRsp(){
        return mNanoRsp;
    }
}
