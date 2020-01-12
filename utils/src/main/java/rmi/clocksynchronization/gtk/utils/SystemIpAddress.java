package rmi.clocksynchronization.gtk.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class SystemIpAddress {
	public String getInet4AddressString() throws SocketException {
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		String inet4AddressString = null;
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddressess = networkInterface.getInetAddresses();
            while (inetAddressess.hasMoreElements()) {
                InetAddress inetAddress = inetAddressess.nextElement();
                if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                    inet4AddressString = inetAddress.getHostAddress();
                }
            }
        }
        return inet4AddressString;
	}
}