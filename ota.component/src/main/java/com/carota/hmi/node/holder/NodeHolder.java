package com.carota.hmi.node.holder;

import com.carota.hmi.node.INode;

import java.util.List;

public class NodeHolder implements IHolder {
    private final List<INode> list;

    public NodeHolder(List<INode> factory) {
        list = factory;
    }

    @Override
    public boolean call() {
        boolean success = true;
        for (INode node : list) {
            if (!node.call()) {
                success = false;
                break;
            }
        }
        return success;
    }

}
