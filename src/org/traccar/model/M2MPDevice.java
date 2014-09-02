package org.traccar.model;

import org.m2mp.db.registry.RegistryNode;

import java.util.UUID;

public class M2MPDevice extends Device {
    private final org.m2mp.db.entity.Device device;
    public M2MPDevice(org.m2mp.db.entity.Device dev) {
        this.device = dev;
    }

    private static final String NODE_BY_SIMPLE_ID = "/device/by-simple-id";
    private static final String PROP_SIMPLE_ID = "simple-id";

    private static synchronized long getNextCounter() {
        long id;
        do {
            RegistryNode node = new RegistryNode(NODE_BY_SIMPLE_ID).check();
            id = node.getProperty("counter", 1);
            node.setProperty("counter", id + 1);
        } while ( bySimpleId(id) != null );
        return id;
    }

    public static M2MPDevice bySimpleId( long simpleId ) {
        RegistryNode child = new RegistryNode(NODE_BY_SIMPLE_ID).getChild(simpleId+"");
        if ( child.exists() ) {
            return new M2MPDevice(new org.m2mp.db.entity.Device(child.getPropertyUUID("id")));
        } else {
            return null;
        }
    }

    @Override
    public Long getId() {
        long simpleId = device.getNode().getProperty(PROP_SIMPLE_ID, 0);
        if ( simpleId == 0 ) {
            simpleId = getNextCounter();
            device.getNode().setProperty(PROP_SIMPLE_ID, simpleId);
            new RegistryNode(NODE_BY_SIMPLE_ID).getChild(simpleId+"").check().setProperty("id", device.getId());
        }
        return simpleId;
    }

    public UUID getUUID() {
        return device.getId();
    }
}
