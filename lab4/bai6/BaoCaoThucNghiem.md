# BÁO CÁO THỰC NGHIỆM SO SÁNH HIỆU NĂNG TCP VÀ UDP
*(Yêu cầu Bài 6 - Lập trình Socket TCP và UDP trong Java)*

## 1. Môi trường và Thiết lập thực nghiệm
- **Hệ điều hành**: Microsoft Windows (Windows 11)
- **Môi trường chạy**: Java Virtual Machine (JDK 25 / Java 17+)
- **Địa chỉ mạng thử nghiệm**: `localhost` (127.0.0.1)
- **Kích thước thông điệp (Payload size)**: Cố định **512 bytes** mỗi gói tin.
- **Số lượng thông điệp mỗi lần đo**: **1.000 thông điệp**.
- **Số lần lặp lại thực nghiệm**: **5 lần** (để loại bỏ sai số ngẫu nhiên).
- **Cấu hình Timeout (UDP)**: `1.000 ms` (`socket.setSoTimeout(1000)`).
- **Phương pháp đo lường**: Sử dụng `System.nanoTime()` trước và sau vòng lặp 1.000 yêu cầu, quy đổi sang mili-giây (ms).

---

## 2. Bảng số liệu thực nghiệm đo đạc thực tế

| Lần đo | TCP Thời gian (ms) | TCP Phản hồi nhận được | UDP Thời gian (ms) | UDP Phản hồi nhận được |
| :---: | :---: | :---: | :---: | :---: |
| **Lần 1** | 229 ms | 1000 / 1000 (100%) | 138 ms | 1000 / 1000 (100%) |
| **Lần 2** | 131 ms | 1000 / 1000 (100%) | 106 ms | 1000 / 1000 (100%) |
| **Lần 3** | 163 ms | 1000 / 1000 (100%) | 84 ms | 1000 / 1000 (100%) |
| **Lần 4** | 132 ms | 1000 / 1000 (100%) | 65 ms | 1000 / 1000 (100%) |
| **Lần 5** | 111 ms | 1000 / 1000 (100%) | 91 ms | 1000 / 1000 (100%) |
| **TRUNG BÌNH** | **153.2 ms** | **1000 / 1000 (100%)** | **96.8 ms** | **1000 / 1000 (100%)** |

---

## 3. Phân tích hiện tượng và Đánh giá học thuật

### 3.1. Tại sao UDP có tổng thời gian nhanh hơn trên localhost?
- **Không có chi phí ACK và kiểm soát luồng**: UDP gửi trực tiếp gói tin `DatagramPacket` mà không cần chờ ACK, không cần cơ chế kiểm soát tắc nghẽn (Congestion Control) hay kiểm soát luồng (Flow Control / Sliding Window).
- **Header nhẹ**: Header UDP chỉ 8 bytes so với header TCP tối thiểu 20 bytes.
- Trên môi trường `localhost`, tỉ lệ mất gói gần như bằng 0% do gói tin chỉ luân chuyển trong bộ đệm Loopback của OS mà không qua đường truyền vật lý.

### 3.2. Vì sao KHÔNG THỂ kết luận UDP luôn nhanh hơn hoặc tốt hơn TCP chỉ từ một lần chạy?
1. **Mất gói trên mạng thực tế (Packet Loss)**:
   - Trên mạng Internet/LAN thực tế có độ trễ (latency), jitter và nghẽn mạng, nếu gói tin UDP bị mất thì tầng ứng dụng phải tự cài đặt cơ chế retransmit (truyền lại). Chi phí truyền lại ở tầng ứng dụng thường chậm và kém tối ưu hơn nhiều so với cơ chế Fast Retransmit tích hợp sẵn ở nhân hệ điều hành của TCP.
2. **Hiện tượng tràn bộ đệm (Buffer Overflow)**:
   - Nếu UDP Client bắn 1.000 gói tin quá dồn dập với tốc độ cao hơn khả năng xử lý của Server, bộ đệm nhận (Socket Receive Buffer) của UDP sẽ bị tràn và hệ điều hành sẽ âm thầm hủy (drop) các gói mà không hề báo lỗi cho Client.
   - Ngược lại, TCP có cơ chế **Backpressure (Flow Control)** thông qua cửa sổ trượt (Receive Window) giúp tự động làm chậm tốc độ gửi của Client khi Server chưa kịp xử lý.
3. **Biến thiên thời gian (Jitter) giữa các lần chạy**:
   - Ở Lần 1, TCP mất 229 ms do chi phí JIT compiler (Warm-up) và khởi tạo kết nối ban đầu, nhưng đến Lần 5 thời gian giảm xuống còn 111 ms (gần tiệm cận UDP 91 ms). Điều này cho thấy hiệu năng chịu ảnh hưởng lớn từ bộ nhớ đệm và trạng thái luồng của JVM.

---

## 4. Kết luận kiến trúc
- **Dùng TCP khi**: Ứng dụng yêu cầu độ chính xác tuyệt đối, dữ liệu không được phép mất mát hoặc sai thứ tự (truyền file, giao dịch ngân hàng, chat, web APIs, RPC).
- **Dùng UDP khi**: Ứng dụng cần độ trễ cực thấp, chấp nhận mất một lượng nhỏ dữ liệu mà không cần gửi lại (phát trực tiếp Video/Audio streaming, game online real-time, DNS query, telemetry đo cảm biến IoT).
