# SO SÁNH HÀNH VI KHI SERVER DỪNG GIỮA LÚC CLIENT ĐANG HOẠT ĐỘNG
*(Yêu cầu Bài 3 - Lập trình Socket TCP và UDP trong Java)*

## 1. Bối cảnh thực nghiệm
Giả sử cả TCP Client và UDP Client đều đang chạy và đã từng gửi/nhận lệnh thành công với Server tương ứng. Sau đó, tiến trình **Server bị tắt đột ngột** (bị tắt cửa sổ, bị `Ctrl+C` hoặc `kill process`).

---

## 2. Hành vi đối với TCP (Connection-Oriented)
- **Bản chất giao thức**: TCP là giao thức **hướng kết nối** (Connection-Oriented), duy trì trạng thái phiên làm việc (Session state) giữa Client và Server thông qua quá trình bắt tay và trao đổi gói tin ACK liên tục.
- **Hiện tượng thực tế khi Server dừng**:
  1. Khi tiến trình Server dừng, hệ điều hành của máy chủ sẽ gửi gói tin `FIN` hoặc `RST` (Reset) tới Client để đóng kết nối.
  2. Phía TCP Client: 
     - Lệnh `in.readLine()` lập tức nhận được giá trị `null` (báo hiệu luồng dữ liệu đã đóng từ phía đối tác) hoặc ném ngoại lệ `SocketException: Connection reset` khi cố gắng gửi dữ liệu tiếp theo.
     - Client phát hiện ngay lập tức tình trạng ngắt kết nối và có thể đóng tài nguyên, đưa ra thông báo rõ ràng cho người dùng (ví dụ: *"Server đã đóng kết nối hoặc bị dừng đột ngột"*).
- **Kết luận TCP**: Khả năng nhận biết trạng thái kết nối tức thời, có độ tin cậy cao.

---

## 3. Hành vi đối với UDP (Connectionless)
- **Bản chất giao thức**: UDP là giao thức **không kết nối** (Connectionless). Mỗi gói `DatagramPacket` được gửi đi độc lập mà không cần bắt tay hay duy trì phiên kết nối.
- **Hiện tượng thực tế khi Server dừng**:
  1. Khi Server dừng, Client hoàn toàn **không hề biết** Server đã chết.
  2. Khi Client tiếp tục gửi một gói `DatagramPacket`, lệnh `socket.send(...)` vẫn thực hiện thành công bình thường (vì UDP chỉ đẩy gói ra card mạng/buffer mà không chờ xác nhận ACK).
  3. Khi Client gọi `socket.receive(receivePacket)` để chờ phản hồi:
     - Do không có phản hồi nào trả về, nếu **không cấu hình timeout**, Client sẽ bị **treo vĩnh viễn (hang/block)** ở hàm `receive()`.
     - Nhờ có cấu hình `socket.setSoTimeout(3000)`, sau 3 giây chờ đợi không nhận được gói tin, socket ném ngoại lệ `SocketTimeoutException`.
     - Phía Client chỉ có thể suy đoán gián tiếp là Server đã tắt hoặc mạng bị nghẽn thông qua việc hết thời gian chờ (Timeout).
- **Kết luận UDP**: Không có cơ chế tự động báo trạng thái sống/chết của Server; bắt buộc ứng dụng phải tự quản lý bằng `setSoTimeout` và cơ chế retry ở tầng ứng dụng (Application Layer).

---

## 4. Bảng tổng hợp so sánh

| Tiêu chí | TCP DateTime Service | UDP DateTime Service |
| :--- | :--- | :--- |
| **Loại giao thức** | Hướng kết nối (`Socket`) | Không kết nối (`DatagramSocket`) |
| **Phát hiện Server tắt** | **Tức thì** (nhận `FIN`/`RST`, `readLine() == null`) | **Bị động / Trễ** (thông qua hết hạn Timeout) |
| **Lệnh `send` / gửi** | Báo lỗi ngay nếu kết nối đã đứt | Vẫn gửi được ra buffer mạng |
| **Rủi ro nếu không timeout** | Ít rủi ro treo vì OS socket báo lỗi | **Treo vĩnh viễn** ở `receive()` nếu không có `setSoTimeout` |
| **Xử lý ngắt phiên `QUIT`** | Bắt buộc có để giải phóng socket | Không cần lệnh `QUIT` cho phiên kết nối |
