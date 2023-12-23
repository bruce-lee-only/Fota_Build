package fi.iki.elonen;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface ILinkInterceptor {

    class RawData extends ByteArrayInputStream {
        public final String type;
        public final String seed;
        public RawData(byte[] buf, String dataType, String s) {
            super(buf);
            type = dataType;
            seed = s;
        }
    }

    byte[] onRecv(NanoHTTPD.IHTTPSession s, byte[] data, String dataType) throws IOException;

    RawData onSend(NanoHTTPD.IHTTPSession s, byte[] raw, String dataType) throws IOException;
}
