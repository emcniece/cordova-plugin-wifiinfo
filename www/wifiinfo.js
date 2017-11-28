'use strict';
var exec = require('cordova/exec');

var WifiInfo = {

    getHostname : function(success, failure) {
        return exec(success, failure, "WifiInfo", "getHostname", []);
    },

    getInfo : function(success, failure) {
        return exec(success, failure, "WifiInfo", "getInfo", []);
    }

};

module.exports = WifiInfo;