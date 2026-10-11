package bai8;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SecureFileServer {
    public static final int DEFAULT_PORT = 5008;
    private static final int MAX_CLIENTS = 10;
    public static final String UPLOAD_DIR = "data" + File.separator + "uploads";

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        startServer(port);
    }

    public static void startServer(int port) {
        File uploadFolder = new File(UPLOAD_DIR);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Secure File Transfer Server dang lang nghe tai port " + port);
            System.out.println("Thu muc upload co dinh: " + uploadFolder.getAbsolutePath());

            while (true) {
                Socket socket = server.accept();
                pool.submit(() -> handleClient(socket));
            }
        } catch (IOException e) {
            System.err.println("Khong the khoi dong File Server: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    private static void handleClient(Socket socket) {
        String clientAddress = String.valueOf(socket.getRemoteSocketAddress());
        System.out.println("Ket noi moi tu client: " + clientAddress);

        try (Socket clientSocket = socket;
             DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            // 1. Đọc metadata từ Client: Tên file, Kích thước (long), và SHA-256 mong đợi
            String clientGivenFileName = in.readUTF();
            long fileSize = in.readLong();
            String expectedSha256 = in.readUTF().trim();

            System.out.printf("[%s] Nhan metadata: File = '%s' | Size = %d bytes | SHA256 = %s%n",
                    clientAddress, clientGivenFileName, fileSize, expectedSha256);

            // 2. Chống Path Traversal: Lấy duy nhất tên file gốc, loại bỏ hoàn toàn đường dẫn do client chỉ định
            File rawFile = new File(clientGivenFileName);
            String safeFileName = rawFile.getName();
            // Lọc bỏ bất kỳ chuỗi "../" hoặc "..\\" nào còn sót lại
            safeFileName = safeFileName.replace("..", "").replace("/", "").replace("\\", "").trim();
            if (safeFileName.isEmpty()) {
                safeFileName = "upload_" + System.currentTimeMillis() + ".dat";
            }

            File targetFile = new File(UPLOAD_DIR, safeFileName);
            System.out.printf("[%s] File se duoc luu tai: %s%n", clientAddress, targetFile.getAbsolutePath());

            // 3. Đọc ĐÚNG số byte đã khai báo (fileSize), không dùng EOF làm ranh giới duy nhất
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            long remaining = fileSize;

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                while (remaining > 0) {
                    int toRead = (int) Math.min(buffer.length, remaining);
                    int readBytes = in.read(buffer, 0, toRead);
                    if (readBytes == -1) {
                        throw new EOFException("Mat ket noi truoc khi doc du so byte da khai bao (" + (fileSize - remaining) + "/" + fileSize + " bytes)");
                    }
                    fos.write(buffer, 0, readBytes);
                    digest.update(buffer, 0, readBytes);
                    remaining -= readBytes;
                }
            }

            // 4. Tính lại SHA-256 của file vừa lưu và so khớp
            String actualSha256 = bytesToHex(digest.digest());
            System.out.printf("[%s] SHA256 thuc te: %s%n", clientAddress, actualSha256);

            if (actualSha256.equalsIgnoreCase(expectedSha256)) {
                System.out.println("[%s] Xac thuc SHA256 THANH CONG!");
                out.writeUTF("OK " + safeFileName);
                out.flush();
            } else {
                System.err.println("[%s] Xac thuc SHA256 THAT BAI! Xoa file hong: " + targetFile.getName());
                targetFile.delete(); // Xóa file sai lệch
                out.writeUTF("ERR HASH_MISMATCH");
                out.flush();
            }

        } catch (EOFException e) {
            System.err.println("Client [" + clientAddress + "] ngat ket noi giua chung: " + e.getMessage());
        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("Loi xu ly file voi client [" + clientAddress + "]: " + e.getMessage());
        } finally {
            System.out.println("Dong ket noi client: " + clientAddress);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
