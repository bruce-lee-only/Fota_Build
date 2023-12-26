package com.carota.hmi.node;

import com.carota.hmi.EventType;

import java.util.concurrent.Callable;

public interface INode  {
    Boolean call();

    EventType getType();

}
