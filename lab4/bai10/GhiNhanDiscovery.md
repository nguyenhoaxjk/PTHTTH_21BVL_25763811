# BÁO CÁO GHI NHẬN THỰC HÀNH BÀI 10: UDP DISCOVERY KẾT HỢP TCP SERVICE

## 1. Giới thiệu và Đặt vấn đề
Trong các hệ thống phân tán và kiến trúc microservices (hoặc thiết bị IoT/Smart Home), địa chỉ IP và Port của các máy chủ dịch vụ thường thay đổi linh hoạt (qua DHCP hoặc dynamic port allocation).
- **Vấn đề:** Nếu cấu hình cứng (hardcode) IP/Port trong client, khi server đổi IP thì hệ thống sẽ mất kết nối.
- **Giải pháp:** Cơ chế **Service Discovery** (Khám phá dịch vụ tự động - tương tự như SSDP trong UPnP hoặc DNS-SD/mDNS trong Apple Bonjour):
  1. Client phát bản tin thăm dò `DISCOVER_SERVICE` qua UDP Broadcast.
  2. Các Server dịch vụ đang hoạt động trong mạng phản hồi lại thông tin dịch vụ của mình.
  3. Client thu thập danh sách, chọn dịch vụ và chuyển sang thiết lập kênh truyền tin cậy **TCP** để thực hiện các giao dịch nghiệp vụ.

---

## 2. Thiết kế Giao thức và Luồng hoạt động

### 2.1. Định dạng thông điệp
| Chiều truyền | Giao thức | Cú pháp | Ý nghĩa |
| :--- | :--- | :--- | :--- |
| **Client -> Server** | UDP Broadcast | `DISCOVER_SERVICE` | Yêu cầu các server trong mạng tự giới thiệu |
| **Server -> Client** | UDP Unicast | `SERVICE tên_dịch_vụ tcp_port phiên_bản` | Trả về thông tin định danh và cổng TCP |
| **Client <-> Server** | TCP Connection | `INFO`, `PING`, `CALC`, `QUIT` | Kênh truyền nghiệp vụ tin cậy, toàn vẹn dữ liệu |

### 2.2. Trích xuất Địa chỉ nguồn từ Datagram
- Khi Server phản hồi gói UDP, Client không cần yêu cầu Server phải khai báo IP của nó trong nội dung chuỗi (vì server có thể không biết IP public của mình nếu đứng sau NAT hoặc multi-homed).
- Thay vào đó, Client sử dụng trực tiếp địa chỉ nguồn từ lớp mạng (Network Layer) thông qua API:
  ```java
  InetAddress serverIp = responsePacket.getAddress();
  int serverTcpPort = parseTcpPort(responseContent);
  ```
- Sau đó, Client mở kết nối TCP trực tiếp:
  ```java
  Socket tcpSocket = new Socket(serverIp, serverTcpPort);
  ```

---

## 3. Các cơ chế Kỹ thuật nâng cao

### 3.1. Cơ chế Khử trùng lặp (Deduplication)
- **Hiện tượng:** Trong môi trường mạng LAN, khi Client gửi broadcast tới cả `255.255.255.255`, `127.0.0.1` và subnet broadcast, một Server có thể nhận được nhiều gói tin discovery và gửi lại nhiều phản hồi. Hoặc mạng có cơ chế truyền lại làm gói tin bị duplicate.
- **Giải pháp:**
  - Client duy trì cấu trúc `Map<String, DiscoveredService>` với khóa định danh duy nhất là:
    ```
    Key = IP_Nguồn + ":" + TCP_Port
    ```
  - Nếu đã tồn tại Key trong Map, Client tăng biến đếm `responseCount++` và bỏ qua việc tạo mới đối tượng dịch vụ.
  - Điều này giúp danh sách dịch vụ hiển thị cho người dùng luôn sạch sẽ, không có mục thừa.

### 3.2. Quản lý Timeout (Tránh treo vô hạn)
- Do UDP là giao thức phi kết nối (Connectionless), nếu không có Server nào hoạt động trong mạng hoặc gói tin bị mất trên đường truyền, Client có nguy cơ bị chặn vĩnh viễn ở lệnh `socket.receive()`.
- Để khắc phục, Client thiết lập thời gian timeout trước khi nhận:
  ```java
  socket.setSoTimeout(2500); // Chờ tối đa 2.5 giây
  ```
- Khi hết thời gian mà không có thêm gói tin nào, JVM sẽ ném ra ngoại lệ `SocketTimeoutException`. Client bắt ngoại lệ này để kết thúc giai đoạn tìm kiếm một cách duyên dáng và chuyển sang hiển thị kết quả.

---

## 4. Hướng dẫn Biên dịch và Chạy thử nghiệm

### 4.1. Biên dịch toàn bộ
```bash
javac -d lab4 lab4/bai10/*.java
```

### 4.2. Chạy kiểm thử tự động
```bash
java -cp lab4 bai10.DiscoveryTest
```

### 4.3. Chạy thủ công trên nhiều Terminal
- **Terminal 1 (Server 1 - Calculator):**
  ```bash
  java -cp lab4 bai10.DiscoveryServer CalculatorService 6001 5010 1.0.0
  ```
- **Terminal 2 (Server 2 - DataStorage):**
  ```bash
  java -cp lab4 bai10.DiscoveryServer DataStorageService 6002 5010 2.1.0
  ```
- **Terminal 3 (Discovery Client):**
  ```bash
  java -cp lab4 bai10.DiscoveryClient 5010 2500
  ```
