/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.build;

import com.momock.util.Logger;
import com.momock.util.XmlHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Configuration implements IConfiguration{
    /**
     <carota>
         <id>hu</id>

         <node id="hub" type="skt">
             <name>ota_proxy</name>
             <addr>192.168.100.1</addr>
             <port>20001</port>
         </node>

         <node id="task">
             <time name="hu">300</time> <!-- Sec -->
             <size name="IPC">12000</size><!-- MB/s-->
         </node>
     </carota>
    */

    private Map<String, ConfigParser> mConfig;
    private ConfigParser mDefaultParser;


    private Configuration(ConfigParser def, Map<String, ConfigParser> config) {
        mConfig = new HashMap<>(config);
        mDefaultParser = def;
        if(null != def) {
            mConfig.put(def.getClass().getName(), def);
        }
    }

    public <T> T get(Class<T> klass) {
        return klass.cast(mConfig.get(klass.getName()));
    }

    private Configuration update(XmlPullParser parser) {
        if(null == parser){
            Logger.debug("CFG : XML is null");
            return this;
        }
        try{
            int eventType;
            do {
                eventType = parser.next();
                if(eventType == XmlPullParser.START_TAG && 2 == parser.getDepth()) {
                    String tag = parser.getName();
                    String attrId = parser.getAttributeValue(null,"id");
                    String attrType = parser.getAttributeValue(null, "type");
                    String attrEnabled = parser.getAttributeValue(null, "enabled");
                    //Logger.debug("Parser TAG : " + tag + "[" + attrName + "]" + attrType);
                    if(tag.equals("node")) {
                        ConfigParser cp = mConfig.get(attrId);
                        if(null != cp) {
                            if(null == attrEnabled || "true".equals(attrEnabled)) {
                                cp.setEnabled(true);
                            } else {
                                Logger.debug("CFG : skip NODE[OFF] " + parser.getName());
                            }
                            cp.setType(attrType);
                            parseNode(parser, cp);
                            continue;
                        } else {
                            Logger.debug("CFG : skip NODE[UNKNOWN] " + parser.getName());
                        }
                        XmlHelper.skipCurrentTag(parser);
                    } else if(null != mDefaultParser) {
                        String text = parser.nextText();
                        Logger.debug("CFG : Parse RT [" + tag + "]" + attrType + ":" + text);
                        mDefaultParser.setType(attrType);
                        mDefaultParser.set(tag, null, text, false);
                    } else {
                        Logger.debug("CFG : Pass RT");
                    }
                }
            } while (eventType != XmlPullParser.END_DOCUMENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    private void parseNode(XmlPullParser parser, ConfigParser param)
            throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        int eventType = parser.getEventType();
        if (eventType != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT
                && (eventType != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if(eventType == XmlPullParser.START_TAG && null != param) {
                String name = parser.getName();
                String key = parser.getAttributeValue(null, "name");
                String enabled = parser.getAttributeValue(null, "enabled");
                String val = parser.nextText();
                Logger.debug("CFG : Parse NODE [" + name + "](" + enabled + ")" + key + ":" + val);
                param.set(name, key, val, null == enabled || "true".equals(enabled));
            }
        }
    }

    public static class Builder {
        private Map<String, ConfigParser> mCfgParsers;
        private ConfigParser mDefCfgParser;
        private XmlPullParser mCfgSrc;
        private ConfigHook mCfgHook;

        public Builder(ConfigHook cfgHook) {
            mCfgParsers = new HashMap<>();
            mCfgSrc = null;
            mCfgHook = cfgHook;
        }

        public Builder addNodeParser(ConfigParser parser) {
            if(null != parser && null != parser.ID) {
                parser.setConfigHook(mCfgHook);
                mCfgParsers.put(parser.ID, parser);
                mCfgParsers.put(parser.getClass().getName(), parser);
            } else {
                Logger.error("CFG : fail to add Parser");
            }
            return this;
        }

        public Builder setDefaultParser(ConfigParser parser) {
            mDefCfgParser = parser;
            return this;
        }

        public Builder setSrc(XmlPullParser configuration) {
            mCfgSrc = configuration;
            return this;
        }

        public Configuration build() {
            return new Configuration(mDefCfgParser, mCfgParsers).update(mCfgSrc);
        }
    }
}
