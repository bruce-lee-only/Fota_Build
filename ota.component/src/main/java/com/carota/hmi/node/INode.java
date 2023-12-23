package com.carota.hmi.node;

import com.carota.hmi.EventType;

public interface INode {
    boolean runNode();

    EventType getType();

    boolean isAutoRunNextNode();

    boolean isSuccess();

}
