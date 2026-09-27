package network;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

public class HostUriInspector {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java network.HostUriInspector <hostname> <URI>");
            System.out.println("Example: java network.HostUriInspector localhost http://localhost:8080/api/users?id=1#profile");
            return;
        }

        String hostname = args[0];
        String uriString = args[1];

        // 1. Host Inspector
        System.out.println("Host: " + hostname);
        try {
            InetAddress[] addresses = InetAddress.getAllByName(hostname);
            for (InetAddress address : addresses) {
                System.out.println("- IP: " + address.getHostAddress());
                String type = (address instanceof Inet4Address) ? "IPv4" 
                            : (address instanceof Inet6Address) ? "IPv6" : "Unknown";
                System.out.println("  Type: " + type);
                System.out.println("  Canonical: " + address.getCanonicalHostName());
                System.out.println("  Loopback: " + address.isLoopbackAddress());
                System.out.println("  Site local: " + address.isSiteLocalAddress());
            }
        } catch (UnknownHostException e) {
            System.err.println("Khong phan giai duoc host: " + hostname);
        }

        // 2. URI Inspector
        System.out.println("URI: " + uriString);
        try {
            URI uri = new URI(uriString);
            System.out.println("- Scheme: " + uri.getScheme());
            System.out.println("  Host: " + uri.getHost());
            System.out.println("  Port: " + uri.getPort());
            System.out.println("  Path: " + uri.getPath());
            System.out.println("  Query: " + uri.getQuery());
            System.out.println("  Fragment: " + uri.getFragment());
        } catch (URISyntaxException e) {
            System.err.println("Cu phap URI khong hop le: " + e.getReason());
        }
    }
}
