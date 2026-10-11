package bai8;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;

public class SecureFileClient {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Cu phap su dung: java -cp . bai8.SecureFileClient <duong_dan_file> [host] [port]");
            System.out.println("Vi du: java -cp . bai8.SecureFileClient test.txt localhost 5008");
            return;
        }

        String filePath = args[0];
        String host = args.length > 1 ? args[1] : "localhost";
        int port = args.length > 2 ? Integer.parseInt(args[2]) : SecureFileServer.DEFAULT_PORT;

        File target = new File(filePath);
        if (!target.exists()) {
            System.err.println("Loi: Duong dan khong ton tai: " + filePath);
            return;
        }

        try {
            if (target.isDirectory()) {
                File[] files = target.listFiles(File::isFile);
                if (files == null || files.length == 0) {
                    System.out.println("Thu muc '" + target.getAbsolutePath() + "' hien dang rong, hay copy file vao thu muc nay truoc khi gui.");
                    return;
                }
                System.out.println("Tim thay " + files.length + " file trong thu muc. Bat dau gui lan luot...");
                for (File f : files) {
                    uploadFile(f, f.getName(), host, port);
                    System.out.println("--------------------------------------------------");
                }
            } else {
                uploadFile(target, target.getName(), host, port);
            }
        } catch (Exception e) {
            System.err.println("Loi gui file: " + e.getMessage());
        }
    }

    public static String uploadFile(File file, String remoteFileName, String host, int port) throws Exception {
        long fileSize = file.length();
        System.out.printf("Chuan bi gui file: '%s' (%d bytes) toi %s:%d...%n", file.getName(), fileSize, host, port);

        // 1. Tinh truoc ma SHA-256 cua file
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        try (FileInputStream fis = new FileInputStream(file)) {
            int read;
            while ((read = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        String sha256Hex = SecureFileServer.bytesToHex(digest.digest());
        System.out.println("SHA-256 cua file: " + sha256Hex);

        // 2. Ket noi va truyen du lieu qua Socket
        try (Socket socket = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            // Gui metadata
            out.writeUTF(remoteFileName);
            out.writeLong(fileSize);
            out.writeUTF(sha256Hex);
            out.flush();

            // Gui toan bo du lieu file
            try (FileInputStream fis = new FileInputStream(file)) {
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            out.flush();

            // Nhan ket qua xac nhan tu server
            String response = in.readUTF();
            System.out.println("Phan hoi tu Server: " + response);
            return response;
        }
    }
}
