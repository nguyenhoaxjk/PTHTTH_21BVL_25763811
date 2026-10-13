# HƯỚNG DẪN KIỂM THỬ THỰC HÀNH LAB 4: SOCKET TCP & UDP TRONG JAVA
> Môn học: Phát triển hệ thống tích hợp (IUH)  
> Môi trường: JDK 17+ (Java 25 LTS), Windows / Linux / macOS.

---

## ⚡ HƯỚNG DẪN BIÊN DỊCH TOÀN BỘ DỰ ÁN

Mở Terminal tại thư mục gốc của dự án (`.../PTHTTH_21BVL_25763811/`) và chạy lệnh sau để biên dịch toàn bộ các bài tập vào thư mục `lab4`:

```powershell
# Biên dịch tất cả các bài từ bài 1 đến bài 10
javac -d lab4 lab4/src/network/*.java lab4/src/tcp/*.java lab4/src/udp/*.java lab4/bai3/*.java lab4/bai4.java lab4/bai5/*.java lab4/bai6/*.java lab4/bai7/*.java lab4/bai8/*.java lab4/bai9/*.java lab4/bai10/*.java
```

---

## 🚀 TỔNG HỢP CÁC LỆNH KIỂM THỬ TỰ ĐỘNG (CHẠY 1 LỆNH)

Nếu muốn kiểm tra nhanh độ chính xác của các bài có bộ test tự động:

| Bài | Chức năng | Lệnh chạy test nhanh |
| :--- | :--- | :--- |
| **Bài 4** | Máy tính từ xa (CALC Protocol) | `java -cp lab4 bai4 test` |
| **Bài 6** | Đo hiệu năng & Benchmark TCP vs UDP | `java -cp lab4 bai6.BenchmarkRunner` |
| **Bài 7** | Server lưu nhật ký tin nhắn Client | `java -cp lab4 bai7.MessageLogTest` |
| **Bài 8** | Truyền file TCP an toàn SHA-256 | `java -cp lab4 bai8.FileTransferTest` |
| **Bài 9** | Multicast thông báo 239.255.0.1 (2 Receivers) | `java -cp lab4 bai9.MulticastTest` |
| **Bài 10**| UDP Discovery kết hợp TCP Service | `java -cp lab4 bai10.DiscoveryTest` |

---

## 📖 HƯỚNG DẪN CHI TIẾT TỪNG BÀI TẬP (BÀI 1 ĐẾN BÀI 10)

---

### BÀI 1: Host và URI Inspector
* **Mục tiêu:** Phân tích thông tin hostname, địa chỉ IP (IPv4/IPv6, Loopback, Site Local) và các thành phần của URI.
* **Lệnh chạy:**
```powershell
# Ca kiểm thử 1: Phân tích localhost và URI đầy đủ
java -cp lab4 network.HostUriInspector localhost "http://localhost:8080/api/users?role=admin#section1"

# Ca kiểm thử 2: Domain Internet
java -cp lab4 network.HostUriInspector example.com "https://example.com/docs/intro"

# Ca kiểm thử 3: Bắt lỗi hostname không tồn tại
java -cp lab4 network.HostUriInspector host-khong-ton-tai.invalid "http://test.local"
```

---

### BÀI 2: TCP Đổi Chữ Số Thành Chữ
* **Mục tiêu:** Gửi 1 chữ số (`0-9`), server trả về cách đọc tiếng Việt (`không` - `chín`). Bắt lỗi `ERR INVALID_DIGIT`.
* **Cách test:**
  * **Terminal 1 (Server):**
    ```powershell
    java -cp lab4 tcp.TcpDigitServer 5000
    ```
  * **Terminal 2 (Client):**
    ```powershell
    java -cp lab4 tcp.TcpDigitClient localhost 5000
    ```
  * **Thử nghiệm trên Client:** Nhập `0`, `5`, `9`, Enter (rỗng), `10`, `a`, `QUIT`.

---

### BÀI 3: Dịch Vụ Ngày Giờ (TCP & UDP)
* **Mục tiêu:** Hỗ trợ các lệnh `DATE`, `TIME`, `DATETIME`. Định dạng `dd MM yyyy` và `HH mm ss`.
* **Cách test TCP:**
  * Terminal 1 (Server): `java -cp lab4 bai3.TcpDateTimeServer 5000`
  * Terminal 2 (Client): `java -cp lab4 bai3.TcpDateTimeClient localhost 5000`
  * Nhập: `DATE`, `TIME`, `DATETIME`, `QUIT`.
* **Cách test UDP:**
  * Terminal 1 (Server): `java -cp lab4 bai3.UdpDateTimeServer 5001`
  * Terminal 2 (Client): `java -cp lab4 bai3.UdpDateTimeClient localhost 5001`
  * Nhập: `DATE`, `TIME`, `DATETIME`, `QUIT`.

---

### BÀI 4: Máy Tính Từ Xa (Remote Calculator)
* **Giao thức:** `CALC toán_tử toán_hạng_1 toán_hạng_2`
* **Test nhanh tự động:**
  ```powershell
  java -cp lab4 bai4 test
  ```
* **Test thủ công qua mạng:**
  * Terminal 1 (Server): `java -cp lab4 bai4 5000`
  * Terminal 2 (Client): `java -cp lab4 bai4 client localhost 5000`
  * Nhập: `CALC + 100 200`, `CALC / 10 0`, `CALC * 7 8`, `QUIT`.

---

### BÀI 5: Chat TCP Nhiều Client
* **Mục tiêu:** Mỗi client chọn nickname duy nhất. Broadcast tin nhắn tới tất cả client khác bằng Thread pool.
* **Cách test:**
  * **Terminal 1 (Server):**
    ```powershell
    java -cp lab4 bai5.ChatServer 5000
    ```
  * **Terminal 2 (Client An):**
    ```powershell
    java -cp lab4 bai5.ChatClient An localhost 5000
    ```
  * **Terminal 3 (Client Binh):**
    ```powershell
    java -cp lab4 bai5.ChatClient Binh localhost 5000
    ```
  * **Thao tác:**
    * Tại Terminal An gõ: `USERS` -> xem danh sách user online.
    * Tại Terminal An gõ: `MSG Xin chao ca phong chat!` -> Terminal Binh nhận được tin nhắn tức thì.
    * Gõ `QUIT` để rời phòng chat.

---

### BÀI 6: Thực Nghiệm So Sánh TCP và UDP
* **Mục tiêu:** Gửi 1000 gói tin kích thước cố định, lặp lại 5 lần đo tổng thời gian, phản hồi và tỷ lệ mất gói.
* **Lệnh chạy:**
  ```powershell
  java -cp lab4 bai6.BenchmarkRunner
  ```
  *(Xem báo cáo phân tích chi tiết tại: `lab4/bai6/BaoCaoThucNghiem.md`)*

---

### BÀI 7: Server Lưu Nhật Ký Tin Nhắn
* **Mục tiêu:** Client gửi `HELLO clientId` trước khi gửi tin nhắn. Server lưu log vào `data/logs/{clientId}.txt`.
* **Test tự động (Khuyên dùng):**
  ```powershell
  java -cp lab4 bai7.MessageLogTest
  ```
* **Test thủ công:**
  * Terminal 1 (Server): `java -cp lab4 bai7.MessageLogServer 5007`
  * Terminal 2 (Client): `java -cp lab4 bai7.MessageLogClient sensor_01 localhost 5007`
  * Kiểm tra file log tạo ra tại: `lab4/data/logs/sensor_01.txt`.

---

### BÀI 8: Truyền File TCP An Toàn (SHA-256)
* **Mục tiêu:** Truyền metadata (tên file, kích thước `long`, mã SHA-256). Đọc đúng số byte, chống ghi đè bằng `../`, băm lại kiểm tra toàn vẹn.
* **Test tự động 4 ca (file rỗng, UTF-8, ảnh binary, chống hack Path Traversal `../`):**
  ```powershell
  java -cp lab4 bai8.FileTransferTest
  ```
* **Test thủ công:**
  * Terminal 1 (Server): `java -cp lab4 bai8.SecureFileServer 5008`
  * Terminal 2 (Client): `java -cp lab4 bai8.SecureFileClient lab4/data/test_source/tieng_viet.txt localhost 5008`
  * Thư mục lưu file nhận: `lab4/data/uploads/`.

---

### BÀI 9: Multicast Thông Báo (`239.255.0.1`)
* **Mục tiêu:** Nhóm IP `239.255.0.1`, cổng `5009`. Tối thiểu 2 Receiver cùng tham gia và nhận tin. Chỉ rõ `NetworkInterface` khi `joinGroup` và `leaveGroup`.
* **Test tự động (Khuyên dùng):**
  ```powershell
  java -cp lab4 bai9.MulticastTest
  ```
* **Test thủ công:**
  * **Terminal 1 (Receiver 1):**
    ```powershell
    java -cp lab4 bai9.MulticastReceiver R1 239.255.0.1 5009
    ```
  * **Terminal 2 (Receiver 2):**
    ```powershell
    java -cp lab4 bai9.MulticastReceiver R2 239.255.0.1 5009
    ```
  * **Terminal 3 (Sender phát tin):**
    ```powershell
    java -cp lab4 bai9.MulticastSender "Thông báo khẩn cấp từ ban giám hiệu!" 239.255.0.1 5009
    ```
  *(Xem tài liệu ghi nhận Wi-Fi/VPN/Firewall tại: `lab4/bai9/GhiNhanMulticast.md`)*

---

### BÀI 10: UDP Discovery Kết Hợp TCP Service
* **Mục tiêu:** Client broadcast UDP `DISCOVER_SERVICE`. Server trả `SERVICE tên_dịch_vụ tcp_port phiên_bản`. Client dùng IP nguồn datagram và `tcp_port` để kết nối TCP Service. Có xử lý Timeout và Deduplication.
* **Test tự động (Khuyên dùng):**
  ```powershell
  java -cp lab4 bai10.DiscoveryTest
  ```
* **Test thủ công:**
  * **Terminal 1 (Khởi động Server 1 - Máy tính):**
    ```powershell
    java -cp lab4 bai10.DiscoveryServer CalculatorService 6001 5010 1.0.0
    ```
  * **Terminal 2 (Khởi động Server 2 - Lưu trữ):**
    ```powershell
    java -cp lab4 bai10.DiscoveryServer DataStorageService 6002 5010 2.1.0
    ```
  * **Terminal 3 (Client quét tìm dịch vụ):**
    ```powershell
    java -cp lab4 bai10.DiscoveryClient 255.255.255.255 5010 2500
    ```
    *Client tự động tìm thấy cả 2 Server, khử trùng lặp và mở TCP Socket giao tiếp trực tiếp với Server!*
  *(Xem tài liệu phân tích kỹ thuật tại: `lab4/bai10/GhiNhanDiscovery.md`)*

---

## 📁 CẤU TRÚC THƯ MỤC MINH CHỨNG & LOG KIỂM THỬ
* `lab4/evidence/test-results-bai01.txt` : Minh chứng Bài 1 (3 ca hợp lệ + 3 ca lỗi)
* `lab4/evidence/test-results-bai02.txt` : Minh chứng Bài 2 (Các ca biên số 0, 9, chuỗi rỗng, chữ)
* `lab4/evidence/so-sanh-server-dung-bai03.md`: Báo cáo so sánh hành vi khi Server dừng giữa chừng (Bài 3)
* `lab4/bai6/BaoCaoThucNghiem.md`        : Báo cáo thực nghiệm 1000 gói tin TCP vs UDP (Bài 6)
* `lab4/bai9/GhiNhanMulticast.md`         : Báo cáo ghi nhận Multicast & sự cố mạng Wi-Fi/VPN (Bài 9)
* `lab4/evidence/test-results-bai09.txt` : Minh chứng log kiểm thử Bài 9
* `lab4/bai10/GhiNhanDiscovery.md`        : Báo cáo kỹ thuật UDP Discovery & TCP Service (Bài 10)
* `lab4/evidence/test-results-bai10.txt`: Minh chứng log kiểm thử Bài 10
