package bai10;

import java.util.Objects;

/**
 * Bài 10: Đại diện cho một dịch vụ mạng được phát hiện thông qua UDP Discovery.
 * Chứa thông tin: Tên dịch vụ, IP nguồn, cổng TCP, phiên bản và số lần nhận phản hồi.
 */
public class DiscoveredService {
    private final String serviceName;
    private final String ip;
    private final int tcpPort;
    private final String version;
    private int responseCount;

    public DiscoveredService(String serviceName, String ip, int tcpPort, String version) {
        this.serviceName = serviceName;
        this.ip = ip;
        this.tcpPort = tcpPort;
        this.version = version;
        this.responseCount = 1;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getIp() {
        return ip;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public String getVersion() {
        return version;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void incrementCount() {
        this.responseCount++;
    }

    /**
     * Khóa định danh duy nhất của dịch vụ trên mạng: IP + TCP Port
     */
    public String getServiceKey() {
        return ip + ":" + tcpPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoveredService that = (DiscoveredService) o;
        return tcpPort == that.tcpPort && Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, tcpPort);
    }

    @Override
    public String toString() {
        return String.format("[%s] tại %s:%d (Phiên bản: %s | Số gói nhận: %d)",
                serviceName, ip, tcpPort, version, responseCount);
    }
}
