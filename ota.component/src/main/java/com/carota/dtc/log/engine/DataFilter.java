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

import java.util.ArrayList;
import java.util.List;

public class DataFilter {

    private List<Rule> mRules;

    public DataFilter() {
        mRules = new ArrayList<>();
    }

    public boolean addRule(List<String> rawRuleList) {
        if(null == rawRuleList || 0 == rawRuleList.size()) {
            Logger.error("[DTC : LOG-DF] : Empty RAW Rule");
            return false;
        }
        Rule rule = null;
        try {
            for (String raw : rawRuleList) {
                Rule r = parseSingleRule(raw);
                if (null != r) {
                    if (null != rule) {
                        rule.setAttached(r);
                    } else {
                        rule = r;
                    }
                }
            }
            if(null != rule) {
                mRules.add(rule);
                return true;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    private Rule parseSingleRule(String raw) {
        int pos = raw.indexOf(':');
        int target = Integer.parseInt(raw.substring(0, pos));
        String rule = raw.substring(pos + 1);
        if(target < 10) {
            Logger.error("[DTC : LOG-DF] : Invalid TARGET = " + target);
        } else if(target < 20) {
            return new KeywordRule(target % 10, rule);
        } else if(target < 30) {
            return new RegularRule(target % 10, rule);
        } else {
            Logger.error("[DTC : LOG-DF] : Unknown TARGET = " + target);
        }
        return null;
    }

    public boolean screen(Piece data) {
        if(0 == mRules.size()) {
            Logger.debug("[DTC : LOG-DF] : No Rule Found");
            return true;
        }
        for(Rule r : mRules) {
            if(r.match(data)) {
                return true;
            }
        }
        return false;
    }
}
