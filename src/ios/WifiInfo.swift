import Foundation

@objc(WifiInfo) public class WifiInfo : CDVPlugin  {

    override public func pluginInitialize() {
    }

    override public func onAppTerminate() {
    }

    @objc(getHostname:) getHostname(_ command: CDVInvokedUrlCommand) {

        let hostname = Hostname.get() as String

        #if DEBUG
            print("WifiInfo: hostname \(hostname)")
        #endif

        let pluginResult = CDVPluginResult(status:CDVCommandStatus_OK, messageAs: hostname)
        self.commandDelegate?.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(getInfo:) getInfo(_ command: CDVInvokedUrlCommand) {

        #if DEBUG
            print("WifiInfo: getInfo")
        #endif

        let hostname = Hostname.get() as String
        let message: NSDictionary = NSDictionary(
            objects: [hostname, false, false, false],
            forKeys: ["hostname" as NSCopying, "connection" as NSCopying, "interfaces" as NSCopying, "dhcp" as NSCopying]
        )

        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: message as! [AnyHashable: Any])
        self.commandDelegate?.send(pluginResult, callbackId: command.callbackId)
    }

}
