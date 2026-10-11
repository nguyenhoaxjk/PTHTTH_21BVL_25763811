package bai10;

import java.util.List;

/**
 * Bài 10: Bộ kiểm thử tự động Khám phá dịch vụ (UDP discovery kết hợp TCP service).
 * Tiêu chí kiểm thử:
 * 1. Khám phá đồng thời nhiều Server đang hoạt động trên mạng.
 * 2. Kiểm tra tính năng loại bỏ phản hồi trùng lặp (Deduplication).
 * 3. Dùng IP nguồn và TCP port từ gói tin UDP để kết nối TCP thành công đến từng Server.
 * 4. Kiểm thử Timeout khi không có Server nào phản hồi (tránh treo chương trình).
 */
public class DiscoveryTest {
    private static final int DISCOVERY_PORT = 5010;
    private static final int EMPTY_PORT = 5999;
    private static final int TCP_PORT_1 = 6001;
    private static final int TCP_PORT_2 = 6002;

    public static void main(String[] args) throws Exception {
        System.out.println("======================================================================");
        System.out.println("BỘ KIỂM THỬ TỰ ĐỘNG BÀI 10 - UDP DISCOVERY KẾT HỢP TCP SERVICE");
        System.out.println("======================================================================\n");

        int passed = 0;
        int total = 0;

        // -----------------------------------------------------------------
        // GIAI ĐOẠN 1: KHỞI TẠO 2 SERVER DỊCH VỤ KHÁC NHAU
        // -----------------------------------------------------------------
        System.out.println(">>> KHỞI ĐỘNG 2 MÁY CHỦ DỊCH VỤ ĐỘC LẬP <<<");
        DiscoveryServer server1 = new DiscoveryServer("CalculatorService", TCP_PORT_1, DISCOVERY_PORT, "1.0.0");
        DiscoveryServer server2 = new DiscoveryServer("DataStorageService", TCP_PORT_2, DISCOVERY_PORT, "2.1.0");

        server1.start();
        server2.start();
        Thread.sleep(300); // Chờ 2 server khởi động sẵn sàng

        try {
            // -------------------------------------------------------------
            // CA KIỂM THỬ 1: PHÁT HIỆN ĐỦ 2 DỊCH VỤ VÀ LOẠI BỎ PHẢN HỒI TRÙNG
            // -------------------------------------------------------------
            total++;
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("[TEST 1] Kiểm thử phát hiện nhiều dịch vụ & Deduplication...");
            System.out.println("-------------------------------------------------------------");

            List<DiscoveredService> services = DiscoveryClient.discoverServices(DISCOVERY_PORT, 2000);

            System.out.printf("Số lượng dịch vụ phát hiện được: %d%n", services.size());
            for (DiscoveredService s : services) {
                System.out.printf("  ✔ Đã tìm thấy: %s%n", s);
            }

            boolean hasServer1 = services.stream().anyMatch(s -> "CalculatorService".equals(s.getServiceName()) && s.getTcpPort() == TCP_PORT_1);
            boolean hasServer2 = services.stream().anyMatch(s -> "DataStorageService".equals(s.getServiceName()) && s.getTcpPort() == TCP_PORT_2);

            if (services.size() == 2 && hasServer1 && hasServer2) {
                System.out.println("✔ PASS CA 1: Phát hiện chính xác cả 2 Server và lọc trùng lặp hoàn hảo!\n");
                passed++;
            } else {
                System.err.println("✘ FAIL CA 1: Danh sách dịch vụ không khớp kỳ vọng!\n");
            }

            // -------------------------------------------------------------
            // CA KIỂM THỬ 2: KẾT NỐI TCP DỰA VÀO IP NGUỒN VÀ TCP PORT CỦA SERVER 1
            // -------------------------------------------------------------
            total++;
            System.out.println("-------------------------------------------------------------");
            System.out.println("[TEST 2] Kiểm thử kết nối TCP tới CalculatorService (Port " + TCP_PORT_1 + ")...");
            System.out.println("-------------------------------------------------------------");

            DiscoveredService s1 = services.stream()
                    .filter(s -> "CalculatorService".equals(s.getServiceName()))
                    .findFirst().orElse(null);

            if (s1 != null && DiscoveryClient.testTcpConnection(s1.getIp(), s1.getTcpPort())) {
                System.out.println("✔ PASS CA 2: Kết nối và giao tiếp TCP thành công với Server 1!\n");
                passed++;
            } else {
                System.err.println("✘ FAIL CA 2: Lỗi kết nối TCP tới Server 1!\n");
            }

            // -------------------------------------------------------------
            // CA KIỂM THỬ 3: KẾT NỐI TCP DỰA VÀO IP NGUỒN VÀ TCP PORT CỦA SERVER 2
            // -------------------------------------------------------------
            total++;
            System.out.println("-------------------------------------------------------------");
            System.out.println("[TEST 3] Kiểm thử kết nối TCP tới DataStorageService (Port " + TCP_PORT_2 + ")...");
            System.out.println("-------------------------------------------------------------");

            DiscoveredService s2 = services.stream()
                    .filter(s -> "DataStorageService".equals(s.getServiceName()))
                    .findFirst().orElse(null);

            if (s2 != null && DiscoveryClient.testTcpConnection(s2.getIp(), s2.getTcpPort())) {
                System.out.println("✔ PASS CA 3: Kết nối và giao tiếp TCP thành công với Server 2!\n");
                passed++;
            } else {
                System.err.println("✘ FAIL CA 3: Lỗi kết nối TCP tới Server 2!\n");
            }

        } finally {
            // Dừng 2 server ban đầu
            server1.stop();
            server2.stop();
            Thread.sleep(300);
        }

        // -------------------------------------------------------------
        // CA KIỂM THỬ 4: KIỂM THỬ TIMEOUT KHI KHÔNG CÓ SERVER NÀO PHẢN HỒI
        // -------------------------------------------------------------
        total++;
        System.out.println("-------------------------------------------------------------");
        System.out.println("[TEST 4] Kiểm thử Timeout (Cổng UDP " + EMPTY_PORT + " không có Server)...");
        System.out.println("-------------------------------------------------------------");

        long start = System.currentTimeMillis();
        List<DiscoveredService> emptyServices = DiscoveryClient.discoverServices(EMPTY_PORT, 1200);
        long elapsed = System.currentTimeMillis() - start;

        System.out.printf("Thời gian chờ thực tế: %d ms | Số dịch vụ: %d%n", elapsed, emptyServices.size());
        if (emptyServices.isEmpty() && elapsed >= 1000 && elapsed <= 2500) {
            System.out.println("✔ PASS CA 4: Bắt timeout chuẩn xác sau khoảng 1.2s, chương trình không treo!\n");
            passed++;
        } else {
            System.err.println("✘ FAIL CA 4: Lỗi xử lý timeout!\n");
        }

        // -------------------------------------------------------------
        // TỔNG KẾT BÁO CÁO
        // -------------------------------------------------------------
        System.out.println("=============================================================");
        System.out.printf("TỔNG KẾT KIỂM THỬ BÀI 10: %d/%d CA ĐẠT (PASS)%n", passed, total);
        System.out.println("=============================================================");

        if (passed == total) {
            System.out.println("🎉 XUẤT SẮC: BÀI 10 ĐÃ VƯỢT QUA TOÀN BỘ CÁC CA KIỂM THỬ!");
        } else {
            System.err.println("⚠ CÓ CA KIỂM THỬ CHƯA ĐẠT!");
        }
    }
}
