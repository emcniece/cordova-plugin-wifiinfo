package net.emcniece.cordova;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class WifiInfo extends CordovaPlugin {

    private static final String TAG = "WifiInfo";

    WifiManager.MulticastLock lock;

    private List<InetAddress> addresses;
    private List<InetAddress> ipv6Addresses;
    private List<InetAddress> ipv4Addresses;
    private String hostname;

    public static final String ACTION_GET_HOSTNAME = "getHostname";
    public static final String ACTION_GET_INFO = "getInfo";

    // Re-initialize
    public static final String ACTION_REINIT = "reInit";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Context context = this.cordova.getActivity().getApplicationContext();
        WifiManager wifi = (WifiManager) context.getSystemService(WIFI_SERVICE);
        lock = wifi.createMulticastLock("WifiInfoPluginLock");
        lock.setReferenceCounted(true);
        lock.acquire();

        try {
            addresses = new ArrayList<InetAddress>();
            ipv6Addresses = new ArrayList<InetAddress>();
            ipv4Addresses = new ArrayList<InetAddress>();
            List<NetworkInterface> intfs = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : intfs) {
                if (intf.supportsMulticast()) {
                    List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                    for (InetAddress addr : addrs) {
                        if (!addr.isLoopbackAddress()) {
                            if (addr instanceof Inet6Address) {
                                addresses.add(addr);
                                ipv6Addresses.add(addr);
                            } else if (addr instanceof Inet4Address) {
                                addresses.add(addr);
                                ipv4Addresses.add(addr);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        Log.d(TAG, "Addresses " + addresses);

        try {
            hostname = getHostName(cordova);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        Log.d(TAG, "Hostname " + hostname);

        Log.v(TAG, "Initialized");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (lock != null) {
            lock.release();
            lock = null;
        }

        Log.v(TAG, "Destroyed");
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {

        if (ACTION_GET_HOSTNAME.equals(action)) {

            if (hostname != null) {
                Log.d(TAG, "Hostname: " + hostname);
                callbackContext.success(hostname);
            } else {
                callbackContext.error("Error: undefined hostname");
                return false;
            }

        } else if (ACTION_GET_INFO.equals(action)) {
            Log.d(TAG, "getInfo");

            final CordovaInterface cd = this.cordova;
            cordova.getThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    Context context = cd.getActivity().getApplicationContext();
                    WifiManager wifi = (WifiManager) context.getSystemService(WIFI_SERVICE);

                    DhcpInfo dhcpInfo = wifi.getDhcpInfo();
                    android.net.wifi.WifiInfo wifiInfo = wifi.getConnectionInfo();

                    JSONObject status = new JSONObject();
                    try {
                        status.put("hostname", hostname);
                        status.put("interfaces", getInterfaces());
                        status.put("dhcp", jsonifyDhcpInfo(dhcpInfo));
                        status.put("connection", jsonifyConnection(wifiInfo));

                        Log.d(TAG, "Sending result: " + status.toString());

                        PluginResult result = new PluginResult(PluginResult.Status.OK, status);
                        result.setKeepCallback(true);
                        callbackContext.sendPluginResult(result);

                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        callbackContext.error("Error: " + e.getMessage());
                    } catch (SocketException e) {
                        Log.e(TAG, e.getMessage(), e);
                        callbackContext.error("Error: " + e.getMessage());
                    }
                }
            });

        } else {
            Log.e(TAG, "Invalid action: " + action);
            callbackContext.error("Invalid action: " + action);
            return false;
        }

        return true;
    }

    // return IP4 & IP6 addresses
    public static JSONObject getInterfaces() throws JSONException, SocketException {
        JSONObject obj = new JSONObject();
        JSONObject intfobj;
        JSONArray ipv4Addresses;
        JSONArray ipv6Addresses;
        List<NetworkInterface> intfs = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface intf : intfs) {
            if (!intf.isLoopback()) {
                intfobj = new JSONObject();
                ipv4Addresses = new JSONArray();
                ipv6Addresses = new JSONArray();

                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        if (addr instanceof Inet6Address) {
                            ipv6Addresses.put(addr.getHostAddress());
                        } else if (addr instanceof Inet4Address) {
                            ipv4Addresses.put(addr.getHostAddress());
                        }
                    }
                }

                if ((ipv4Addresses.length() > 0) || (ipv6Addresses.length() > 0)) {
                    intfobj.put("ipv4Addresses", ipv4Addresses);
                    intfobj.put("ipv6Addresses", ipv6Addresses);
                    intfobj.put("mac", macAddressFromNetworkInterface(intf));

                    obj.put(intf.getName(), intfobj);
                }
            }
        }

        return obj;
    }

    private static JSONObject jsonifyDhcpInfo(DhcpInfo info) throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("dns1", ipToString(info.dns1));
        obj.put("dns2", ipToString(info.dns2));
        obj.put("gateway", ipToString(info.gateway));
        obj.put("ip", ipToString(info.ipAddress));
        obj.put("lease", info.leaseDuration);
        obj.put("netmask", ipToString(info.netmask));
        obj.put("server", ipToString(info.serverAddress));

        return obj;
    }

    private static JSONObject jsonifyConnection(android.net.wifi.WifiInfo info) throws JSONException {
        JSONObject obj = new JSONObject();
        String macAddress = "02:00:00:00:00:00";

        try {
            macAddress = macAddressFromNetworkInterface(NetworkInterface.getByName("wlan0"));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        obj.put("bssid", info.getBSSID());
        obj.put("hidden", info.getHiddenSSID());
        obj.put("ip", ipToString(info.getIpAddress()));
        obj.put("speed", info.getLinkSpeed());
        obj.put("mac", macAddress);
        obj.put("rssi", info.getRssi());
        obj.put("ssid", info.getSSID());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            obj.put("frequency", info.getFrequency());
        } else {
            obj.put("frequency", null);
        }

        return obj;
    }

    private static String ipToString(int ip) {
        return String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
    }

    private static String macAddressFromNetworkInterface(NetworkInterface intf) {
        String macAddress = "02:00:00:00:00:00";

        try {
            byte[] macBytes = intf.getHardwareAddress();
            if (macBytes != null) {
                StringBuilder mac = new StringBuilder();
                for (byte b : macBytes) {
                    mac.append(String.format("%02X:", b));
                }

                if (mac.length() > 0) {
                    mac.deleteCharAt(mac.length() - 1);
                }

                macAddress = mac.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return macAddress;
    }

    // http://stackoverflow.com/questions/21898456/get-android-wifi-net-hostname-from-code
    public static String getHostName(CordovaInterface cordova) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method getString = Build.class.getDeclaredMethod("getString", String.class);
        getString.setAccessible(true);
        String hostName = getString.invoke(null, "net.hostname").toString();

        if (TextUtils.isEmpty(hostName) || hostName.equals("unknown")) {
            // API 26+ :
            // Querying the net.hostname system property produces a null result
            String id = Settings.Secure.getString(cordova.getActivity().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            hostName = "android-" + id;
        }
        return hostName;
    }

}
