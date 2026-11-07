import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TruyVanNangCao {
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:nhanvien.db";
            conn = DriverManager.getConnection(url);
            stmt = conn.createStatement();

            // 1. Thêm nhiều bản ghi vào bảng NhanVien với cột lương (luong)
            stmt.executeUpdate("DROP TABLE IF EXISTS NhanVien;");
            String sql_create_table = "CREATE TABLE NhanVien ("
                    + "id INTEGER PRIMARY KEY, "
                    + "ten TEXT NOT NULL, "
                    + "chuc_vu TEXT, "
                    + "luong REAL"
                    + ");";
            stmt.executeUpdate(sql_create_table);

            stmt.executeUpdate("INSERT INTO NhanVien VALUES (1, 'Nguyen Van A', 'Truong phong', 25000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (2, 'Tran Thi B', 'Nhan vien', 12000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (3, 'Le Van C', 'Nhan vien', 11000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (4, 'Pham Thi D', 'Ke toan', 15000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (5, 'Hoang Van E', 'Ke toan', 14000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (6, 'Vu Van F', 'Truong phong', 28000000);");

            // 2. Tính tổng lương SUM(luong)
            rs = stmt.executeQuery("SELECT SUM(luong) AS tong_luong FROM NhanVien;");
            if (rs.next()) {
                System.out.println("Tong luong: " + rs.getDouble("tong_luong"));
            }
            rs.close();

            // 3. Tìm mức lương trung bình AVG(luong)
            rs = stmt.executeQuery("SELECT AVG(luong) AS luong_tb FROM NhanVien;");
            if (rs.next()) {
                System.out.println("Luong trung binh: " + rs.getDouble("luong_tb"));
            }
            rs.close();

            // 4. Tìm nhân viên có mức lương cao nhất / thấp nhất MIN/MAX(luong)
            rs = stmt.executeQuery("SELECT ten, luong FROM NhanVien WHERE luong = (SELECT MAX(luong) FROM NhanVien);");
            if (rs.next()) {
                System.out.println("Luong cao nhat: " + rs.getString("ten") + " - " + rs.getDouble("luong"));
            }
            rs.close();

            rs = stmt.executeQuery("SELECT ten, luong FROM NhanVien WHERE luong = (SELECT MIN(luong) FROM NhanVien);");
            if (rs.next()) {
                System.out.println("Luong thap nhat: " + rs.getString("ten") + " - " + rs.getDouble("luong"));
            }
            rs.close();

            // 5. Đếm số lượng nhân viên theo chức vụ (COUNT và GROUP BY)
            System.out.println("\nSo luong nhan vien theo chuc vu:");
            rs = stmt.executeQuery("SELECT chuc_vu, COUNT(*) AS so_luong FROM NhanVien GROUP BY chuc_vu;");
            while (rs.next()) {
                System.out.println(rs.getString("chuc_vu") + ": " + rs.getInt("so_luong"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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

