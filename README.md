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

#### `reInit(success, failure)`
Re-initializes the entire plugin, which resets the browsers and services. Use this if the WiFi network has changed while the app is running.

```javascript
zeroconf.reInit()
```

## Licence ##

The MIT License
