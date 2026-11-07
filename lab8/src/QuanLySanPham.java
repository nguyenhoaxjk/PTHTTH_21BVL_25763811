import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QuanLySanPham {
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // 1. Kết nối cơ sở dữ liệu
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:sanpham.db";
            conn = DriverManager.getConnection(url);

            // 2. Tạo bảng SanPham (maSP, tenSP, gia, soluong)
            stmt = conn.createStatement();
            String sql_create_table = "CREATE TABLE IF NOT EXISTS SanPham ("
                    + "maSP TEXT PRIMARY KEY, "
                    + "tenSP TEXT, "
                    + "gia REAL, "
                    + "soluong INTEGER"
                    + ");";
            stmt.executeUpdate(sql_create_table);

            // 3. Sử dụng Transaction và PreparedStatement để thêm mới (Create) ít nhất 3 sản phẩm
            conn.setAutoCommit(false); // Tắt auto-commit để bắt đầu Transaction

            String sql_insert = "INSERT OR REPLACE INTO SanPham (maSP, tenSP, gia, soluong) VALUES (?, ?, ?, ?);";
            pstmt = conn.prepareStatement(sql_insert);

            // Thêm sản phẩm 1
            pstmt.setString(1, "SP01");
            pstmt.setString(2, "Laptop Dell Inspiron");
            pstmt.setDouble(3, 15000000);
            pstmt.setInt(4, 10);
            pstmt.executeUpdate();

            // Thêm sản phẩm 2
            pstmt.setString(1, "SP02");
            pstmt.setString(2, "Chuot khong day Logitech");
            pstmt.setDouble(3, 350000);
            pstmt.setInt(4, 50);
            pstmt.executeUpdate();

            // Thêm sản phẩm 3
            pstmt.setString(1, "SP03");
            pstmt.setString(2, "Ban phim co DareU");
            pstmt.setDouble(3, 850000);
            pstmt.setInt(4, 20);
            pstmt.executeUpdate();

            // Commit transaction thêm mới
            conn.commit();
            pstmt.close();

            // 4. Truy vấn (Read): Liệt kê tất cả sản phẩm
            System.out.println("--- Danh sach tat ca san pham ---");
            String sql_select_all = "SELECT * FROM SanPham;";
            rs = stmt.executeQuery(sql_select_all);
            while (rs.next()) {
                System.out.println(rs.getString("maSP") + " - " + rs.getString("tenSP") + " - " + rs.getDouble("gia") + " - " + rs.getInt("soluong"));
            }
            rs.close();

            // 5. Truy vấn (Read): Tìm kiếm sản phẩm theo tenSP sử dụng PreparedStatement
            System.out.println("\n--- Tim kiem san pham theo ten (Dell) ---");
            String sql_search = "SELECT * FROM SanPham WHERE tenSP LIKE ?;";
            pstmt = conn.prepareStatement(sql_search);
            pstmt.setString(1, "%Dell%");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString("maSP") + " - " + rs.getString("tenSP") + " - " + rs.getDouble("gia") + " - " + rs.getInt("soluong"));
            }
            rs.close();
            pstmt.close();

            // 6. Cập nhật (Update) với Transaction: Thay đổi giá và số lượng theo maSP
            String sql_update = "UPDATE SanPham SET gia = ?, soluong = ? WHERE maSP = ?;";
            pstmt = conn.prepareStatement(sql_update);
            pstmt.setDouble(1, 14500000);
            pstmt.setInt(2, 15);
            pstmt.setString(3, "SP01");
            pstmt.executeUpdate();
            conn.commit(); // Commit Transaction cập nhật
            pstmt.close();

            // 7. Xóa (Delete) sử dụng PreparedStatement: Xóa theo maSP
            String sql_delete = "DELETE FROM SanPham WHERE maSP = ?;";
            pstmt = conn.prepareStatement(sql_delete);
            pstmt.setString(1, "SP03");
            pstmt.executeUpdate();
            conn.commit(); // Commit Transaction xóa
            pstmt.close();

            // In lại danh sách sau khi Update và Delete
            System.out.println("\n--- Danh sach san pham sau khi Update va Delete ---");
            rs = stmt.executeQuery(sql_select_all);
            while (rs.next()) {
                System.out.println(rs.getString("maSP") + " - " + rs.getString("tenSP") + " - " + rs.getDouble("gia") + " - " + rs.getInt("soluong"));
            }

        } catch (Exception e) {
            // Rollback giao dịch nếu xảy ra lỗi
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Giao dich bi loi, da rollback!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            // Xử lý ngoại lệ triệt để: Đóng toàn bộ tài nguyên trong khối finally
            try {
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

