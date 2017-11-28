# Cordova WifiInfo Plugin

This plugin allows you to read WiFi network status information from applications developed using PhoneGap/Cordova 3.0 or newer.

## Installation

In your application project directory:

```bash
cordova plugin add cordova-plugin-wifiinfo
```

## Usage ##

```javascript
var wifi = cordova.plugins.wifiinfo;
```

#### `getHostname(success, failure)`
Returns this device's hostname.

```javascript
wifi.getHostname(function success(hostname){
    console.log(hostname); // ipad-of-user.local.
});
```

#### `getInfo(success, failure)`
Returns this device's network info. Android only.

```javascript
wifi.getHostname(function success(info){
    console.log(info); //
    /*
    {
        hostname: 'ipad-of-user.local',

        // ConnectionInfo
        connection: {
            bssid (string)
            hidden (boolean): Whether the network is hidden or not
            ip (string)
            speed (int): Uplink speed
            mac (string)
            rssi (int)
            ssid (string)
            frequency (int): WiFi band, Lollipop (API 26+) only
        },

        // DhcpInfo
        dhcp: {
            dns1 (string)
            dns2 (string)
            gateway (string)
            ip (string)
            lease (int): Lifespan of DHCP lease
            netmask (string)
            server (string)
        },

        // list of IPv4 and IPv6 interfaces
        interfaces: {
            wlan0: {
                ipv4Addresses[],
                ipv6Addresses[]
            }
        }
    }
    */
});
```

## ToDo

- [ ] Integrate [swift-netutils](https://github.com/svdo/swift-netutils)

## Licence ##

The MIT License
