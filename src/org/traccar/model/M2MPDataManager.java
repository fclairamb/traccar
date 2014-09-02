package org.traccar.model;

import ly.bit.nsq.NSQProducer;
import org.apache.cassandra.utils.UUIDGen;
import org.json.simple.JSONObject;
import org.m2mp.db.DB;
import org.mortbay.log.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;


public class M2MPDataManager implements DataManager {

    private final Properties properties;
    private final NSQProducer producer;

    public M2MPDataManager(Properties properties) {
        this.properties = properties;
        this.producer = new NSQProducer("storage");
        DB.keyspace(properties.getProperty("database.keyspace", "ks_test"));
    }

    @Override
    public List<Device> getDevices() throws Exception {
        return new ArrayList<Device>();
    }

    @Override
    public Device getDeviceByImei(String imei) throws Exception {
        org.m2mp.db.entity.Device coreDevice = org.m2mp.db.entity.Device.byIdent("imei:"+imei, true);
        M2MPDevice device = new M2MPDevice( coreDevice );

        return device;
    }

    @Override
    public Long addPosition(Position position) throws Exception {
        JSONObject obj = new JSONObject();
        // Standard fields
        obj.put("_from", "receiver-traccar");
        obj.put("_to", "m2mp-storage");
        obj.put("_call", "store_ts");
        obj.put("_time", new Date().getTime()/1000);

        M2MPDevice device = M2MPDevice.bySimpleId(position.getDeviceId());

        if ( device == null ) {
            Log.warn("Device "+position.getDeviceId()+" could not be found !");
        }

        obj.put("key", "dev-" + device.getUUID());
        obj.put("type", "sen:loc");
        obj.put("date_uuid", UUIDGen.minTimeUUID(position.getTime().getTime()).toString());
        {
            JSONObject data = new JSONObject();
            obj.put("data", data);
            data.put("lat", position.getLatitude());
            data.put("lon", position.getLongitude());
            data.put("spd", position.getSpeed());
            data.put("alt", position.getAltitude());
        }

        try {
            producer.put(obj.toJSONString());
        }
        catch( Exception ex ) {
            Log.warn(ex);
            ex.printStackTrace();
        }

        // This makes no sense here
        return null;
    }

    @Override
    public void updateLatestPosition(Long deviceId, Long positionId) throws Exception {
        // Let's just not do it
    }
}
