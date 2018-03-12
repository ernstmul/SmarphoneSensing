package nl.tudelft.smartphonesensing.utils;

/**
 * Created by ernstmulders on 17/02/2018.
 *
 * Class which contains the RSSI, SSID and timestamp of each wifi signal
 */

public class WifiData {

    public String SSID;
    public Integer RSSi;
    public Long timestamp;

    // never used?
    public void setValues(String SSID, Integer RSSi, Long timestamp){
        this.SSID = SSID;
        this.RSSi = RSSi;
        this.timestamp = timestamp;
    }

    public String toString(){
        return "SSID: " + SSID + ", RSSi:" + RSSi + ", timestamp:" + timestamp;
    }
}