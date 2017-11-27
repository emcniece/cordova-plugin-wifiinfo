'use strict';
var exec = require('cordova/exec');

var WifiInfo = {

    getHostname : function(success, failure) {
        return exec(success, failure, "WifiInfo", "getHostname", []);
    },

    getInfo : function(success, failure) {
        return exec(success, failure, "WifiInfo", "getInfo", []);
    },

    reInit : function(success, failure) {
        return exec(success, failure, "WifiInfo", "reInit", []);
    }

};

module.exports = WifiInfo;