import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BaiTapCoBan {
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // B1. Chuẩn bị môi trường và thiết lập kết nối
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:nhanvien.db";
            conn = DriverManager.getConnection(url);

            // B2. Tạo đối tượng Statement từ Connection
            stmt = conn.createStatement();

            // B3. Thực thi các câu truy vấn từ các Statement đã tạo
            // 3.1. Tạo bảng
            String sql_create_table = "CREATE TABLE IF NOT EXISTS NhanVien ("
                    + "id INTEGER PRIMARY KEY, "
                    + "ten TEXT NOT NULL, "
                    + "chuc_vu TEXT"
                    + ");";
            stmt.executeUpdate(sql_create_table);

            // 3.2. Thêm dữ liệu (ít nhất 3 câu lệnh sql INSERT INTO)
            String sql_insert1 = "INSERT OR IGNORE INTO NhanVien (id, ten, chuc_vu) VALUES (1, 'Nguyen Van A', 'Truong phong');";
            String sql_insert2 = "INSERT OR IGNORE INTO NhanVien (id, ten, chuc_vu) VALUES (2, 'Tran Thi B', 'Nhan vien');";
            String sql_insert3 = "INSERT OR IGNORE INTO NhanVien (id, ten, chuc_vu) VALUES (3, 'Le Van C', 'Ke toan');";
            stmt.executeUpdate(sql_insert1);
            stmt.executeUpdate(sql_insert2);
            stmt.executeUpdate(sql_insert3);

            // B4. Truy vấn và hiển thị dữ liệu
            String sql_select = "SELECT id, ten, chuc_vu FROM NhanVien;";
            rs = stmt.executeQuery(sql_select);
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " - " + rs.getString("ten") + " - " + rs.getString("chuc_vu"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            // B5. Đóng kết nối và giải phóng tài nguyên
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

