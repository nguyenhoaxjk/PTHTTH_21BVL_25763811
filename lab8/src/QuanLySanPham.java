import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Scanner;

public class QuanLySanPham {
    private static final String URL_SANPHAM = "jdbc:sqlite:sanpham.db";
    private static final String URL_NHANVIEN = "jdbc:sqlite:nhanvien.db";
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,### VND");

    public static Connection getConnection(String url) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(url);
    }

    public static void main(String[] args) {
        initDatabase();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n================================================================================");
            System.out.println("              HE THONG QUAN LY BAI TAP JDBC - LAB 8 (MENU TUONG TAC)            ");
            System.out.println("================================================================================");
            System.out.println("1. [Muc 1 & 4] Them moi 3 san pham (Create bang PreparedStatement + Transaction)");
            System.out.println("2. [Muc 1 & 4] Truy van san pham (Read: Xem tat ca & Tim kiem theo ten)");
            System.out.println("3. [Muc 1 & 2] Cap nhat san pham SP01 (Update gia & so luong + Commit Transaction)");
            System.out.println("4. [Muc 1 & 2] Xoa san pham SP03 (Delete theo ma SP + Commit Transaction)");
            System.out.println("5. [Muc 2 & 3] Demo Transaction Rollback khi co loi & Exception Handling");
            System.out.println("6. [Muc 5]     Truy van nang cao bang NhanVien (SUM, AVG, MIN, MAX, GROUP BY)");
            System.out.println("7. [Chay tat ca] Tu dong thuc hien tuan tu tu muc 1 den muc 6");
            System.out.println("0. Thoat chuong trinh");
            System.out.println("================================================================================");
            System.out.print("Vui long nhap lua chon cua ban (0 - 7): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    chucNangThemSanPham();
                    pause(scanner);
                    break;
                case "2":
                    chucNangTruyVanSanPham();
                    pause(scanner);
                    break;
                case "3":
                    chucNangCapNhatSanPham();
                    pause(scanner);
                    break;
                case "4":
                    chucNangXoaSanPham();
                    pause(scanner);
                    break;
                case "5":
                    chucNangDemoRollbackVaException();
                    pause(scanner);
                    break;
                case "6":
                    chucNangTruyVanNangCao();
                    pause(scanner);
                    break;
                case "7":
                    System.out.println("\n>>> BAT DAU CHAY TOAN BO CAC MUC TU 1 DEN 6 <<<");
                    chucNangThemSanPham();
                    chucNangTruyVanSanPham();
                    chucNangCapNhatSanPham();
                    chucNangXoaSanPham();
                    chucNangDemoRollbackVaException();
                    chucNangTruyVanNangCao();
                    System.out.println("\n>>> DA HOAN TAT CHAY TAT CA CAC MUC! <<<");
                    pause(scanner);
                    break;
                case "0":
                    System.out.println("\nDa thoat chuong trinh. Tam biet!");
                    scanner.close();
                    return;
                default:
                    System.out.println("\nLua chon khong hop le, vui long chon tu 0 den 7!");
                    break;
            }
        }
    }

    private static void pause(Scanner scanner) {
        System.out.print("\n[>>] Nhan phim Enter de quay lai Menu...");
        scanner.nextLine();
    }

    // Khoi tao bang SanPham
    public static void initDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection(URL_SANPHAM);
            stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS SanPham ("
                    + "maSP TEXT PRIMARY KEY, "
                    + "tenSP TEXT NOT NULL, "
                    + "gia REAL NOT NULL, "
                    + "soluong INTEGER NOT NULL"
                    + ");";
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dongTaiNguyen(null, stmt, null, conn);
        }
    }

    // 1. Them moi it nhat 3 san pham voi PreparedStatement & Transaction
    public static void chucNangThemSanPham() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  1. [CREATE] THEM MOI 3 SAN PHAM (SU DUNG PREPAREDSTATEMENT & TRANSACTION)     ");
        System.out.println("--------------------------------------------------------------------------------");
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection(URL_SANPHAM);
            // Bat dau Transaction
            conn.setAutoCommit(false);
            System.out.println("[Transaction] Da tat Auto-Commit (conn.setAutoCommit(false)) de bat dau giao dich.");

            String sql = "INSERT OR REPLACE INTO SanPham (maSP, tenSP, gia, soluong) VALUES (?, ?, ?, ?);";
            pstmt = conn.prepareStatement(sql);

            // Du lieu mau 3 san pham
            String[][] dsSP = {
                {"SP01", "Laptop Dell Inspiron 15", "15000000", "10"},
                {"SP02", "Chuot khong day Logitech M331", "350000", "50"},
                {"SP03", "Ban phim co DareU EK87", "850000", "20"}
            };

            for (String[] sp : dsSP) {
                pstmt.setString(1, sp[0]);
                pstmt.setString(2, sp[1]);
                pstmt.setDouble(3, Double.parseDouble(sp[2]));
                pstmt.setInt(4, Integer.parseInt(sp[3]));
                pstmt.executeUpdate();
                System.out.println(" + [PreparedStatement] Da them san pham: " + sp[0] + " - " + sp[1] 
                        + " | Gia: " + CURRENCY_FORMAT.format(Double.parseDouble(sp[2])) + " | SL: " + sp[3]);
            }

            // Commit Transaction
            conn.commit();
            System.out.println("[Transaction] Da thuc hien commit() thanh cong! 3 san pham da duoc luu vao CSDL.");

        } catch (Exception e) {
            System.err.println("[Loi] Them san pham that bai!");
            rollback(conn);
            e.printStackTrace();
        } finally {
            dongTaiNguyen(null, null, pstmt, conn);
        }
    }

    // 2. Truy van du lieu: Xem toan bo & Tim kiem theo ten
    public static void chucNangTruyVanSanPham() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  2. [READ] TRUY VAN DU LIEU: XEM TAT CA & TIM KIEM THEO TEN (PREPAREDSTATEMENT)");
        System.out.println("--------------------------------------------------------------------------------");
        Connection conn = null;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection(URL_SANPHAM);

            // 2.1. Liet ke tat ca san pham
            System.out.println("--- [2.1] DANH SACH TAT CA SAN PHAM TRONG BANG SanPham ---");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT maSP, tenSP, gia, soluong FROM SanPham;");
            inBangSanPham(rs);
            rs.close();

            // 2.2. Tim kiem theo ten voi PreparedStatement
            String tuKhoa = "Dell";
            System.out.println("\n--- [2.2] TIM KIEM SAN PHAM THEO TEN (Tu khoa: '" + tuKhoa + "') VOI PREPAREDSTATEMENT ---");
            String sqlSearch = "SELECT maSP, tenSP, gia, soluong FROM SanPham WHERE tenSP LIKE ?;";
            pstmt = conn.prepareStatement(sqlSearch);
            pstmt.setString(1, "%" + tuKhoa + "%");
            rs = pstmt.executeQuery();
            inBangSanPham(rs);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dongTaiNguyen(rs, stmt, pstmt, conn);
        }
    }

    // 3. Cap nhat san pham voi Transaction
    public static void chucNangCapNhatSanPham() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  3. [UPDATE] CAP NHAT GIA VA SO LUONG SAN PHAM (TRANSACTION & PREPAREDSTATEMENT)");
        System.out.println("--------------------------------------------------------------------------------");
        Connection conn = null;
        PreparedStatement pstmt = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection(URL_SANPHAM);
            conn.setAutoCommit(false);
            System.out.println("[Transaction] Bat dau giao dich cap nhat (Auto-Commit = false).");

            String maSP = "SP01";
            double giaMoi = 14500000;
            int soLuongMoi = 15;

            String sqlUpdate = "UPDATE SanPham SET gia = ?, soluong = ? WHERE maSP = ?;";
            pstmt = conn.prepareStatement(sqlUpdate);
            pstmt.setDouble(1, giaMoi);
            pstmt.setInt(2, soLuongMoi);
            pstmt.setString(3, maSP);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" + [PreparedStatement] Da cap nhat " + maSP + " -> Gia moi: " 
                        + CURRENCY_FORMAT.format(giaMoi) + ", So luong moi: " + soLuongMoi);
            }

            conn.commit();
            System.out.println("[Transaction] Da commit() giao dich cap nhat thanh cong!");

            // Hien thi lai bang sau khi cap nhat
            System.out.println("\n--- BANG SAN PHAM SAU KHI CAP NHAT ---");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT maSP, tenSP, gia, soluong FROM SanPham;");
            inBangSanPham(rs);

        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
        } finally {
            dongTaiNguyen(rs, stmt, pstmt, conn);
        }
    }

    // 4. Xoa san pham voi Transaction
    public static void chucNangXoaSanPham() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  4. [DELETE] XOA SAN PHAM THEO MA (TRANSACTION & PREPAREDSTATEMENT)            ");
        System.out.println("--------------------------------------------------------------------------------");
        Connection conn = null;
        PreparedStatement pstmt = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection(URL_SANPHAM);
            conn.setAutoCommit(false);
            System.out.println("[Transaction] Bat dau giao dich xoa (Auto-Commit = false).");

            String maSP = "SP03";
            String sqlDelete = "DELETE FROM SanPham WHERE maSP = ?;";
            pstmt = conn.prepareStatement(sqlDelete);
            pstmt.setString(1, maSP);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println(" + [PreparedStatement] Da thuc hien xoa san pham co ma: " + maSP);
            }

            conn.commit();
            System.out.println("[Transaction] Da commit() giao dich xoa thanh cong!");

            // Hien thi lai bang sau khi xoa
            System.out.println("\n--- BANG SAN PHAM SAU KHI XOA (SP03 DA BI LOAI BO) ---");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT maSP, tenSP, gia, soluong FROM SanPham;");
            inBangSanPham(rs);

        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
        } finally {
            dongTaiNguyen(rs, stmt, pstmt, conn);
        }
    }

    // 5. Demo Transaction Rollback khi loi & Exception Handling trong finally
    public static void chucNangDemoRollbackVaException() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  5. DEMO TRANSACTION ROLLBACK VA XU LY NGOAI LE TRIET DE (FINALLY)             ");
        System.out.println("--------------------------------------------------------------------------------");
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection(URL_SANPHAM);
            conn.setAutoCommit(false);
            System.out.println("[Transaction] 1. Bat Transaction: conn.setAutoCommit(false)");

            // Thao tac 1: Them SP99 hop le
            String sqlInsert1 = "INSERT OR REPLACE INTO SanPham (maSP, tenSP, gia, soluong) VALUES (?, ?, ?, ?);";
            pstmt = conn.prepareStatement(sqlInsert1);
            pstmt.setString(1, "SP99");
            pstmt.setString(2, "SanPham Thu Nghiem Rollback");
            pstmt.setDouble(3, 500000);
            pstmt.setInt(4, 5);
            pstmt.executeUpdate();
            System.out.println(" + Thao tac 1: Da chen tam SP99 vao giao dich (chua commit).");
            pstmt.close();

            // Thao tac 2: Co tinh thuc thi cau lenh SQL bi loi de kich hoat Exception
            System.out.println(" + Thao tac 2: Co tinh thuc thi cau truy van loi cu phap de kiem tra rollback...");
            String sqlLoi = "INSERT INTO BangKhongTonTai VALUES (1, 2, 3);";
            pstmt = conn.prepareStatement(sqlLoi);
            pstmt.executeUpdate(); // Se nem ra SQLException!

            // Neu khong co loi thi moi commit (dong nay se khong chay toi)
            conn.commit();

        } catch (SQLException e) {
            System.err.println("[Ngoai le bat duoc] SQLException: " + e.getMessage());
            System.out.println("[Rollback] Da kich hoat conn.rollback()!");
            rollback(conn);
            System.out.println("[Ket qua] Du lieu SP99 o Thao tac 1 da bi huy bo hoan toan, CSDL giu nguyen tinh toan ven!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("\n--- [XU LY NGOAI LE TRIET DE TRONG KHOI FINALLY] ---");
            dongTaiNguyen(rs, null, pstmt, conn);
        }
    }

    // 6. Truy van nang cao: Aggregation Functions (SUM, AVG, MIN, MAX, GROUP BY)
    public static void chucNangTruyVanNangCao() {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println("  6. [TRUY VAN NANG CAO] CAC HAM TONG HOP (AGGREGATION FUNCTIONS) TREN NHAN VIEN");
        System.out.println("--------------------------------------------------------------------------------");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection(URL_NHANVIEN);
            stmt = conn.createStatement();

            // Tao bang NhanVien va them ban ghi mau co cot luong
            stmt.executeUpdate("DROP TABLE IF EXISTS NhanVien;");
            stmt.executeUpdate("CREATE TABLE NhanVien ("
                    + "id INTEGER PRIMARY KEY, "
                    + "ten TEXT NOT NULL, "
                    + "chuc_vu TEXT, "
                    + "luong REAL"
                    + ");");

            stmt.executeUpdate("INSERT INTO NhanVien VALUES (1, 'Nguyen Van A', 'Truong phong', 25000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (2, 'Tran Thi B', 'Nhan vien', 12000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (3, 'Le Van C', 'Nhan vien', 11000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (4, 'Pham Thi D', 'Ke toan', 15000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (5, 'Hoang Van E', 'Ke toan', 14000000);");
            stmt.executeUpdate("INSERT INTO NhanVien VALUES (6, 'Vu Van F', 'Truong phong', 28000000);");

            System.out.println("--- DANH SACH 6 NHAN VIEN VA MUC LUONG MAU ---");
            rs = stmt.executeQuery("SELECT id, ten, chuc_vu, luong FROM NhanVien;");
            System.out.printf("%-5s | %-20s | %-15s | %-15s\n", "ID", "Ho va Ten", "Chuc vu", "Luong");
            System.out.println("------------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-5d | %-20s | %-15s | %-15s\n",
                        rs.getInt("id"),
                        rs.getString("ten"),
                        rs.getString("chuc_vu"),
                        CURRENCY_FORMAT.format(rs.getDouble("luong")));
            }
            rs.close();

            System.out.println("\n--- KET QUA THUC HIEN CAC HAM TONG HOP (AGGREGATION FUNCTIONS) ---");

            // 6.1. Tong luong SUM(luong)
            rs = stmt.executeQuery("SELECT SUM(luong) AS tong_luong FROM NhanVien;");
            if (rs.next()) {
                System.out.println("1. [SUM] Tong luong toan bo nhan vien:  " + CURRENCY_FORMAT.format(rs.getDouble("tong_luong")));
            }
            rs.close();

            // 6.2. Muc luong trung binh AVG(luong)
            rs = stmt.executeQuery("SELECT AVG(luong) AS luong_tb FROM NhanVien;");
            if (rs.next()) {
                System.out.println("2. [AVG] Muc luong trung binh:         " + CURRENCY_FORMAT.format(rs.getDouble("luong_tb")));
            }
            rs.close();

            // 6.3. Luong cao nhat MAX(luong)
            rs = stmt.executeQuery("SELECT ten, luong FROM NhanVien WHERE luong = (SELECT MAX(luong) FROM NhanVien);");
            if (rs.next()) {
                System.out.println("3. [MAX] Nhan vien co luong cao nhat:   " + rs.getString("ten") + " (" + CURRENCY_FORMAT.format(rs.getDouble("luong")) + ")");
            }
            rs.close();

            // 6.4. Luong thap nhat MIN(luong)
            rs = stmt.executeQuery("SELECT ten, luong FROM NhanVien WHERE luong = (SELECT MIN(luong) FROM NhanVien);");
            if (rs.next()) {
                System.out.println("4. [MIN] Nhan vien co luong thap nhat:  " + rs.getString("ten") + " (" + CURRENCY_FORMAT.format(rs.getDouble("luong")) + ")");
            }
            rs.close();

            // 6.5. Dem so luong nhan vien theo chuc vu (COUNT va GROUP BY)
            System.out.println("\n5. [COUNT & GROUP BY] Thong ke so luong nhan vien theo tung chuc vu:");
            rs = stmt.executeQuery("SELECT chuc_vu, COUNT(*) AS so_luong FROM NhanVien GROUP BY chuc_vu;");
            System.out.printf("   %-15s | %-10s\n", "Chuc vu", "So luong");
            System.out.println("   ----------------------------");
            while (rs.next()) {
                System.out.printf("   %-15s | %-10d\n", rs.getString("chuc_vu"), rs.getInt("so_luong"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dongTaiNguyen(rs, stmt, null, conn);
        }
    }

    // In bang san pham dep mat
    private static void inBangSanPham(ResultSet rs) throws SQLException {
        System.out.printf("%-10s | %-32s | %-18s | %-10s\n", "Ma SP", "Ten San Pham", "Don Gia", "So Luong");
        System.out.println("--------------------------------------------------------------------------------");
        boolean coDuLieu = false;
        while (rs.next()) {
            coDuLieu = true;
            System.out.printf("%-10s | %-32s | %-18s | %-10d\n",
                    rs.getString("maSP"),
                    rs.getString("tenSP"),
                    CURRENCY_FORMAT.format(rs.getDouble("gia")),
                    rs.getInt("soluong"));
        }
        if (!coDuLieu) {
            System.out.println("              (Khong tim thay san pham nao)");
        }
    }

    // Rollback giao dich an toan
    private static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Dong tai nguyen trong finally an toan tuyet doi
    private static void dongTaiNguyen(ResultSet rs, Statement stmt, PreparedStatement pstmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
                System.out.println("[Finally] Da dong ResultSet an toan.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
                System.out.println("[Finally] Da dong PreparedStatement an toan.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
                System.out.println("[Finally] Da dong Statement an toan.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
                System.out.println("[Finally] Da dong Connection an toan.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
