package bai9;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Lớp tiện ích hỗ trợ liệt kê và lựa chọn NetworkInterface phù hợp cho Multicast.
 * Hỗ trợ tự động nhận diện card mạng LAN, Wi-Fi, VMware/VirtualBox, hoặc Loopback.
 */
public class NetworkInterfaceHelper {

    /**
     * In danh sách tất cả các NetworkInterface khả dụng trên máy.
     */
    public static void printAvailableInterfaces() {
        System.out.println("========== DANH SÁCH NETWORK INTERFACES TRÊN HỆ THỐNG ==========");
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                System.out.println("Không tìm thấy card mạng nào.");
                return;
            }
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                System.out.printf("[%s] %s | Up: %b | Multicast: %b | Loopback: %b%n",
                        ni.getName(), ni.getDisplayName(), ni.isUp(), ni.supportsMulticast(), ni.isLoopback());
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    System.out.printf("    -> %s (%s)%n", addr.getHostAddress(),
                            (addr instanceof Inet4Address ? "IPv4" : "IPv6"));
                }
            }
        } catch (SocketException e) {
            System.err.println("Lỗi khi quét card mạng: " + e.getMessage());
        }
        System.out.println("================================================================");
    }

    /**
     * Tìm NetworkInterface phù hợp nhất để tham gia nhóm Multicast.
     * 
     * @param preferredName Tên card mạng người dùng chỉ định (nếu có, ví dụ "eth0", "Wi-Fi", "loopback_0", "127.0.0.1")
     * @return NetworkInterface được chọn
     * @throws SocketException Nếu không tìm thấy card mạng nào hỗ trợ multicast
     */
    public static NetworkInterface getSuitableInterface(String preferredName) throws SocketException {
        // 1. Nếu người dùng chỉ định tên cụ thể
        if (preferredName != null && !preferredName.trim().isEmpty()) {
            preferredName = preferredName.trim();
            NetworkInterface niByName = NetworkInterface.getByName(preferredName);
            if (niByName != null && niByName.isUp() && niByName.supportsMulticast()) {
                return niByName;
            }
            // Thử tìm theo IP
            try {
                InetAddress addr = InetAddress.getByName(preferredName);
                NetworkInterface niByAddr = NetworkInterface.getByInetAddress(addr);
                if (niByAddr != null && niByAddr.isUp() && niByAddr.supportsMulticast()) {
                    return niByAddr;
                }
            } catch (Exception ignored) {
            }
            // Thử tìm gần đúng theo displayName
            Enumeration<NetworkInterface> all = NetworkInterface.getNetworkInterfaces();
            while (all.hasMoreElements()) {
                NetworkInterface ni = all.nextElement();
                if (ni.getDisplayName().toLowerCase().contains(preferredName.toLowerCase())
                        && ni.isUp() && ni.supportsMulticast()) {
                    return ni;
                }
            }
            System.err.println("Cảnh báo: Không tìm thấy card mạng '" + preferredName + "' hoặc card không hỗ trợ Multicast. Tự động chọn card phù hợp.");
        }

        // 2. Tự động chọn card mạng phù hợp:
        // Ưu tiên:
        // - Đang UP
        // - Hỗ trợ Multicast (supportsMulticast == true)
        // - Có địa chỉ IPv4
        // - Ưu tiên card mạng LAN / Virtual Adapter / Wi-Fi trước, sau đó fallback sang loopback
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        if (interfaces == null) {
            throw new SocketException("Hệ thống không có NetworkInterface nào.");
        }

        List<NetworkInterface> candidates = new ArrayList<>();
        NetworkInterface loopbackCandidate = null;

        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (!ni.isUp() || !ni.supportsMulticast()) {
                continue;
            }

            boolean hasIpv4 = false;
            Enumeration<InetAddress> addrs = ni.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = addrs.nextElement();
                if (addr instanceof Inet4Address) {
                    hasIpv4 = true;
                    break;
                }
            }

            if (!hasIpv4) continue;

            if (ni.isLoopback()) {
                loopbackCandidate = ni;
            } else {
                candidates.add(ni);
            }
        }

        // Ưu tiên card mạng không phải loopback (LAN, Wi-Fi, Virtual)
        if (!candidates.isEmpty()) {
            return candidates.get(0);
        }

        // Nếu không có card mạng ngoài nào, dùng loopback
        if (loopbackCandidate != null) {
            return loopbackCandidate;
        }

        // Fallback cuối cùng
        return NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
    }
}
