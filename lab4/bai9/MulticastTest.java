package bai9;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Bài 9: Bộ kiểm thử tự động Multicast thông báo.
 * Yêu cầu kiểm thử:
 * 1. Khởi động ít nhất 2 Receiver cùng tham gia group 239.255.0.1.
 * 2. Chỉ rõ NetworkInterface khi joinGroup.
 * 3. Sender gửi nhiều thông điệp (tiếng Anh, tiếng Việt UTF-8 có dấu, ký tự đặc biệt).
 * 4. Xác nhận cả 2 Receiver cùng nhận đủ tất cả thông điệp với nội dung toàn vẹn.
 * 5. Gọi leaveGroup() trước khi đóng socket.
 */
public class MulticastTest {
    private static final String GROUP_IP = "239.255.0.1";
    private static final int PORT = 5009;

    public static void main(String[] args) throws Exception {
        System.out.println("======================================================================");
        System.out.println("BỘ KIỂM THỬ TỰ ĐỘNG BÀI 9 - MULTICAST THÔNG BÁO (239.255.0.1)");
        System.out.println("======================================================================\n");

        // Chọn interface phù hợp
        NetworkInterface netIf = NetworkInterfaceHelper.getSuitableInterface(args.length > 0 ? args[0] : null);
        System.out.printf("Sử dụng NetworkInterface: [%s] %s%n", netIf.getName(), netIf.getDisplayName());

        SocketAddress groupAddress = new InetSocketAddress(InetAddress.getByName(GROUP_IP), PORT);

        // Danh sách thông điệp nhận được của từng receiver
        List<String> receiver1Messages = Collections.synchronizedList(new ArrayList<>());
        List<String> receiver2Messages = Collections.synchronizedList(new ArrayList<>());

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch finishLatch = new CountDownLatch(2);

        // Thread cho Receiver 1
        Thread t1 = new Thread(() -> {
            try (MulticastSocket socket = new MulticastSocket(null)) {
                socket.setReuseAddress(true);
                socket.bind(new InetSocketAddress(PORT));
                socket.joinGroup(groupAddress, netIf);
                socket.setSoTimeout(3000);
                readyLatch.countDown();
                System.out.println("✔ Receiver 1 đã joinGroup thành công trên " + netIf.getName());

                byte[] buf = new byte[4096];
                while (receiver1Messages.size() < 3) {
                    try {
                        DatagramPacket p = new DatagramPacket(buf, buf.length);
                        socket.receive(p);
                        String msg = new String(p.getData(), p.getOffset(), p.getLength(), StandardCharsets.UTF_8);
                        receiver1Messages.add(msg);
                        System.out.println("  [Receiver 1 nhận được]: " + msg);
                    } catch (SocketTimeoutException te) {
                        System.err.println("  [Receiver 1]: Timeout chờ gói tin.");
                        break;
                    }
                }

                // Rời group trước khi đóng socket
                socket.leaveGroup(groupAddress, netIf);
                System.out.println("✔ Receiver 1 đã leaveGroup an toàn.");
            } catch (IOException e) {
                System.err.println("Receiver 1 lỗi: " + e.getMessage());
            } finally {
                finishLatch.countDown();
            }
        });

        // Thread cho Receiver 2
        Thread t2 = new Thread(() -> {
            try (MulticastSocket socket = new MulticastSocket(null)) {
                socket.setReuseAddress(true);
                socket.bind(new InetSocketAddress(PORT));
                socket.joinGroup(groupAddress, netIf);
                socket.setSoTimeout(3000);
                readyLatch.countDown();
                System.out.println("✔ Receiver 2 đã joinGroup thành công trên " + netIf.getName());

                byte[] buf = new byte[4096];
                while (receiver2Messages.size() < 3) {
                    try {
                        DatagramPacket p = new DatagramPacket(buf, buf.length);
                        socket.receive(p);
                        String msg = new String(p.getData(), p.getOffset(), p.getLength(), StandardCharsets.UTF_8);
                        receiver2Messages.add(msg);
                        System.out.println("  [Receiver 2 nhận được]: " + msg);
                    } catch (SocketTimeoutException te) {
                        System.err.println("  [Receiver 2]: Timeout chờ gói tin.");
                        break;
                    }
                }

                // Rời group trước khi đóng socket
                socket.leaveGroup(groupAddress, netIf);
                System.out.println("✔ Receiver 2 đã leaveGroup an toàn.");
            } catch (IOException e) {
                System.err.println("Receiver 2 lỗi: " + e.getMessage());
            } finally {
                finishLatch.countDown();
            }
        });

        t1.start();
        t2.start();

        // Chờ cả 2 receiver sẵn sàng
        if (!readyLatch.await(5, TimeUnit.SECONDS)) {
            System.err.println("✘ Lỗi: Các Receiver không khởi động kịp thời!");
            return;
        }

        // Tạm nghỉ ngắn để đảm bảo IGMP report đã được socket đăng ký
        Thread.sleep(300);

        System.out.println("\n-------------------------------------------------------------");
        System.out.println("BẮT ĐẦU SENDER PHÁT THÔNG BÁO TỚI NHÓM MULTICAST");
        System.out.println("-------------------------------------------------------------");

        String[] testMessages = {
            "THONG_BAO_1: Chao mung cac ban sinh vien den voi buoi thuc hanh Socket Java!",
            "THONG_BAO_2: Tiếng Việt UTF-8: Hệ thống máy chủ bảo trì vào lúc 22:00 hôm nay. ★ ✪ ☘",
            "THONG_BAO_3: [KHẨN CẤP] Điểm thi giữa kỳ môn PTHTTH đã được cập nhật trên cổng sinh viên!"
        };

        for (int i = 0; i < testMessages.length; i++) {
            System.out.printf("%n[Gửi gói tin %d/3]: \"%s\"%n", (i + 1), testMessages[i]);
            MulticastSender.sendMessage(testMessages[i], GROUP_IP, PORT, netIf.getName());
            Thread.sleep(200); // Giãn cách ngắn giữa các thông điệp
        }

        // Chờ 2 receiver xử lý xong
        finishLatch.await(6, TimeUnit.SECONDS);

        System.out.println("\n=============================================================");
        System.out.println("KẾT QUẢ ĐÁNH GIÁ KIỂM THỬ BÀI 9");
        System.out.println("=============================================================");

        boolean passedR1 = receiver1Messages.size() == testMessages.length;
        boolean passedR2 = receiver2Messages.size() == testMessages.length;

        System.out.printf("Receiver 1: Nhận được %d/%d thông điệp -> %s%n",
                receiver1Messages.size(), testMessages.length, (passedR1 ? "✔ PASS" : "✘ FAIL"));
        System.out.printf("Receiver 2: Nhận được %d/%d thông điệp -> %s%n",
                receiver2Messages.size(), testMessages.length, (passedR2 ? "✔ PASS" : "✘ FAIL"));

        boolean contentMatch = true;
        for (int i = 0; i < testMessages.length; i++) {
            if (i < receiver1Messages.size() && !testMessages[i].equals(receiver1Messages.get(i))) {
                contentMatch = false;
            }
            if (i < receiver2Messages.size() && !testMessages[i].equals(receiver2Messages.get(i))) {
                contentMatch = false;
            }
        }

        System.out.printf("Kiểm tra tính toàn vẹn nội dung UTF-8 của cả 2 Receiver: %s%n",
                (contentMatch ? "✔ CHÍNH XÁC 100%" : "✘ NỘI DUNG BỊ SAI LỆCH"));

        if (passedR1 && passedR2 && contentMatch) {
            System.out.println("\n🎉 BÀI 9: TẤT CẢ CÁC TIÊU CHÍ KIỂM THỬ ĐỀU ĐẠT (PASS)!");
        } else {
            System.err.println("\n⚠ BÀI 9: CÓ LỖI XẢY RA TRONG QUÁ TRÌNH KIỂM THỬ!");
        }
    }
}
