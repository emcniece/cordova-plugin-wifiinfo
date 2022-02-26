import Foundation

@objc(WifiInfo) public class WifiInfo : CDVPlugin  {

    override public func pluginInitialize() {
    }

    override public func onAppTerminate() {
    }

    @objc(getHostname:) func getHostname(_ command: CDVInvokedUrlCommand) {

        let hostname = Hostname.get() as String

        #if DEBUG
            print("WifiInfo: hostname \(hostname)")
        #endif

        let pluginResult = CDVPluginResult(status:CDVCommandStatus_OK, messageAs: hostname)
        self.commandDelegate?.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(getInfo:) func getInfo(_ command: CDVInvokedUrlCommand) {

        #if DEBUG
            print("WifiInfo: getInfo")
        #endif
        
        let mac = "02:00:00:00:00:00" // good enough as new iOS restriction wont return anything else
        let ipv4 = getIPAddress(addrType: UInt8(AF_INET))
        let ipv6 = getIPAddress(addrType: UInt8(AF_INET6))
        
        let connection: NSDictionary = NSDictionary(
            objects: [false, false, ipv4, false, mac, false, false, ipv6],
            forKeys: ["bssid" as NSCopying, "hidden" as NSCopying, "ip" as NSCopying, "speed" as NSCopying, "mac" as NSCopying, "rssi" as NSCopying, "ssid" as NSCopying, "ipv6" as NSCopying]
        )

        let hostname = Hostname.get() as String
        let message: NSDictionary = NSDictionary(
            objects: [hostname, connection, false, false],
            forKeys: ["hostname" as NSCopying, "connection" as NSCopying, "interfaces" as NSCopying, "dhcp" as NSCopying]
        )

        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: message as! [AnyHashable: Any])
        self.commandDelegate?.send(pluginResult, callbackId: command.callbackId)
    }
    
    func getIPAddress(addrType: UInt8) -> String {
        var address: String?
        var ifaddr: UnsafeMutablePointer<ifaddrs>? = nil
        if getifaddrs(&ifaddr) == 0 {
            var ptr = ifaddr
            while ptr != nil {
                defer { ptr = ptr?.pointee.ifa_next }

                guard let interface = ptr?.pointee else { return "" }
                let addrFamily = interface.ifa_addr.pointee.sa_family
                if addrFamily == addrType {

                    let name: String = String(cString: (interface.ifa_name))
                    if  name == "en0" || name == "en2" || name == "en3" || name == "en4" || name == "pdp_ip0" || name == "pdp_ip1" || name == "pdp_ip2" || name == "pdp_ip3" {
                        var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
                        getnameinfo(interface.ifa_addr, socklen_t((interface.ifa_addr.pointee.sa_len)), &hostname, socklen_t(hostname.count), nil, socklen_t(0), NI_NUMERICHOST)
                        address = String(cString: hostname)
                    }
                }
            }
            freeifaddrs(ifaddr)
        }
        return address ?? ""
    }

}
