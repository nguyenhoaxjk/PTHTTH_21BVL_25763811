# SO SÁNH HÀNH VI KHI SERVER DỪNG GIỮA LÚC CLIENT ĐANG HOẠT ĐỘNG (BÀI 3)

## 1. Bối cảnh kịch bản thử nghiệm
- **Bước 1:** Bật Server (TCP hoặc UDP) và bật Client tương ứng. Client gửi vài lệnh (`DATE`, `TIME`) thành công.
- **Bước 2:** Đột ngột tắt Server bằng tổ hợp phím `Ctrl + C` (hoặc kill process) trong khi Client vẫn đang mở và chờ lệnh của người dùng.
- **Bước 3:** Người dùng tiếp tục gõ một lệnh từ Client (ví dụ `DATETIME`) và bấm Enter để quan sát hành vi của từng giao thức.

---

## 2. Bảng so sánh chi tiết giữa TCP và UDP

| Tiêu chí | TCP (TcpDateTimeClient) | UDP (UdpDateTimeClient) |
| :--- | :--- | :--- |
| **Bản chất giao thức** | Hướng kết nối (Connection-oriented), duy trì trạng thái kết nối socket 2 chiều (Stateful). | Không kết nối (Connectionless), không duy trì trạng thái phiên (Stateless). |
| **Hành vi khi Server tắt** | Khi tiến trình Server bị tắt, hệ điều hành của Server sẽ gửi gói tin TCP đóng kết nối (`FIN`) hoặc hủy kết nối (`RST`) tới Client. | Không có thông báo đóng phiên nào được gửi qua mạng. Phía Client hoàn toàn không hề biết Server đã tắt. |
| **Phản ứng của Client khi đọc dữ liệu** | - Khi Client gọi `in.readLine()`, luồng đọc trả về giá trị `null` ngay lập tức (báo hiệu End Of Stream) hoặc ném `SocketException: Connection reset` / `Broken pipe`.<br>- Client nhận biết ngay lập tức Server đã dừng và ngắt phiên an toàn. | - Client gửi `DatagramPacket` đi thành công ra card mạng (vì không cần bắt tay kết nối trước).<br>- Sau đó, Client bị chặn (block) tại lệnh `socket.receive()` để chờ gói tin phản hồi. |
| **Thời gian phát hiện lỗi** | **Tức thì (Real-time):** Gần như mili-giây, ngay khi Server đóng kết nối hoặc khi Client vừa gửi lệnh tiếp theo. | **Phụ thuộc cấu hình Timeout:**<br>- Nếu **có** `setSoTimeout(3000)`: Client phát hiện sau đúng 3 giây qua ngoại lệ `SocketTimeoutException`.<br>- Nếu **không có** Timeout: Client sẽ bị **treo vĩnh viễn (hung thread)** chờ mãi mãi. |
| **Thông báo lỗi hiển thị** | `Server closed the connection` hoặc `Lỗi kết nối: Connection reset` | `Loi: Het 3 giay khong nhan duoc phan hoi tu server (Timeout)` |

---

## 3. Kết luận rút ra
1. **TCP** an toàn và minh bạch hơn về trạng thái kết nối. Ứng dụng client có thể phát hiện sự cố mất kết nối gần như ngay lập tức để chủ động ngắt phiên hoặc thực hiện cơ chế tự kết nối lại (Auto-reconnect).
2. **UDP** nhẹ hơn và không tốn chi phí duy trì kết nối, nhưng phía ứng dụng **bắt buộc phải chủ động thiết lập `socket.setSoTimeout(...)`**. Nếu thiếu cơ chế timeout, ứng dụng UDP sẽ rơi vào trạng thái treo vô hạn khi mạng chập chờn hoặc server gặp sự cố.
