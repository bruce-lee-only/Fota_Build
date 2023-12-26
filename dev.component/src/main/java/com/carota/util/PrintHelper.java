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

import java.util.Iterator;
import java.util.Map;

public class PrintHelper {
    public static <K,V>String print(Map<K, V> map) {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            if (iter.hasNext()) {
                sb.append(',').append(' ');
            }
        }
        return sb.toString();
    }
}
