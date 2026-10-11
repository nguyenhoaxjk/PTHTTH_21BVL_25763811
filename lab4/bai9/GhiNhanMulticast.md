# BÁO CÁO GHI NHẬN THỰC HÀNH BÀI 9: MULTICAST THÔNG BÁO

## 1. Mục tiêu và phạm vi kỹ thuật
- **Địa chỉ Multicast:** `239.255.0.1` thuộc dải `239.255.0.0/16` (IPv4 Administratively Scoped Multicast - RFC 2365).
  - Đây là dải địa chỉ dành riêng cho phạm vi quản trị cục bộ (Local Administrative Scope). Các gói tin trong dải này không được định tuyến ra ngoài Internet công cộng, đảm bảo an toàn cho mạng nội bộ tổ chức/doanh nghiệp.
- **Mô hình hoạt động:** 
  - Một **Sender** phát bản tin tới địa chỉ Multicast IP `239.255.0.1` và cổng `5009`.
  - Nhiều **Receiver** (tối thiểu 2 receiver) cùng lắng nghe trên cổng `5009`, đăng ký gia nhập nhóm qua giao thức IGMP (Internet Group Management Protocol) và nhận thông điệp đồng thời mà không làm tăng tải truyền phát (tiết kiệm băng thông hơn Unicast).

---

## 2. Quản lý Vòng đời Nhóm Multicast trong Java
Trong các phiên bản Java hiện đại (Java 17, 21, 25):
- Phương thức cũ `socket.joinGroup(InetAddress)` đã bị **deprecated**.
- Bắt buộc sử dụng phương thức chuẩn:
  ```java
  SocketAddress groupAddress = new InetSocketAddress(InetAddress.getByName("239.255.0.1"), port);
  NetworkInterface netIf = ...; // Card mạng xác định
  socket.joinGroup(groupAddress, netIf);
  ```
- **Rời nhóm trước khi đóng socket:**
  Trước khi đóng kết nối, tiến trình receiver phải gửi thông báo rời nhóm IGMP Leave Group để Router/Switch hạ tầng ngắt luồng multicast đến máy đó:
  ```java
  socket.leaveGroup(groupAddress, netIf);
  socket.close();
  ```
- Trong bài thực hành, hệ thống đã cài đặt Shutdown Hook tự động để đảm bảo dù bị tắt bằng `Ctrl+C` hay lệnh ngắt bất thường, `socket.leaveGroup(...)` vẫn luôn được gọi.

---

## 3. Ghi nhận các trường hợp Multicast bị chặn và Giải pháp khắc phục

### 3.1. Bị chặn bởi Mạng Wi-Fi (Wi-Fi AP Multicast Filtering / AP Isolation)
- **Nguyên nhân:**
  - Trong mạng Wi-Fi (đặc biệt là Wi-Fi công cộng, trường đại học như IUH), thiết bị Access Point (AP) thường bật tính năng **AP Isolation** (Client Isolation) hoặc **Multicast-to-Unicast translation disable / IGMP Snooping drop**.
  - Việc này nhằm ngăn chặn "Broadcast/Multicast Storm" làm nghẽn băng thông sóng vô tuyến của AP khi có hàng ngàn sinh viên kết nối. Khi đó, AP sẽ chủ động drop các gói tin UDP Multicast giữa các thiết bị không dây.
- **Dấu hiệu:** Sender gửi thành công không báo lỗi, nhưng Receiver không nhận được bất kỳ byte dữ liệu nào (bị timeout).
- **Giải pháp:**
  1. Sử dụng kết nối dây Ethernet (Switch phòng Lab hỗ trợ IGMP Snooping).
  2. Khi kiểm thử trên một máy (localhost), chỉ định card mạng Loopback (`Software Loopback Interface 1` / `127.0.0.1`) hoặc card mạng ảo (VMware VMnet, VirtualBox Host-Only Adapter).

### 3.2. Bị chặn bởi VPN (Virtual Private Network)
- **Nguyên nhân:**
  - Phần lớn các phần mềm VPN (OpenVPN, WireGuard, Cisco AnyConnect, Tailscale) khi kích hoạt sẽ chỉnh sửa bảng định tuyến hệ điều hành (Routing Table), trỏ default gateway `0.0.0.0/0` qua adapter ảo của VPN (TUN/TAP).
  - Nhiều tunnel VPN ở tầng Layer 3 (TUN mode) **chỉ hỗ trợ Unicast** và hoàn toàn không hỗ trợ giao thức Multicast/IGMP. Gói tin Multicast gửi đi sẽ bị tunnel loại bỏ âm thầm.
- **Giải pháp:**
  - Tạm thời ngắt kết nối (Disconnect) VPN trong quá trình thực hành Multicast Socket.
  - Cấu hình chỉ định tường minh `NetworkInterface` bằng card mạng vật lý hoặc card loopback qua tham số dòng lệnh `args[3]`.

### 3.3. Bị chặn bởi Windows Defender Firewall / Tường lửa hệ điều hành
- **Nguyên nhân:**
  - Tường lửa Windows mặc định có chính sách chặn các kết nối Inbound UDP chưa được đăng ký trong danh sách cho phép (Allowed Apps).
  - Khi một tiến trình Java mới lắng nghe cổng UDP 5009, Windows Firewall có thể hiển thị cảnh báo UAC hoặc âm thầm drop gói tin nếu chọn "Cancel".
- **Giải pháp:**
  - Mở cổng UDP 5009 trong Windows Firewall bằng lệnh PowerShell (quyền Administrator):
    ```powershell
    New-NetFirewallRule -DisplayName "Lab4-Multicast-5009" -Direction Inbound -LocalPort 5009 -Protocol UDP -Action Allow
    ```
  - Sau khi kiểm thử xong, xóa bỏ rule để bảo mật hệ thống:
    ```powershell
    Remove-NetFirewallRule -DisplayName "Lab4-Multicast-5009"
    ```

---

## 4. Phương án kiểm thử đề xuất (Localhost và Phòng Lab)

| Môi trường kiểm thử | Card mạng khuyến nghị | Cách chạy |
| :--- | :--- | :--- |
| **Kiểm thử trên 1 máy (Localhost)** | Tự động hoặc chỉ định Loopback / VMware | `java -cp . bai9.MulticastTest` |
| **Chạy thủ công Receiver 1** | Cùng máy hoặc máy khác trong LAN | `java -cp . bai9.MulticastReceiver R1 239.255.0.1 5009` |
| **Chạy thủ công Receiver 2** | Cùng máy hoặc máy khác trong LAN | `java -cp . bai9.MulticastReceiver R2 239.255.0.1 5009` |
| **Chạy thủ công Sender** | Nhập từ bàn phím hoặc gửi qua args | `java -cp . bai9.MulticastSender "Thông báo khẩn!" 239.255.0.1 5009` |
