public class Product {
    private final String code;
    private final String name;
    private final double unitPrice;
    private final int quantity;

    public Product(String code, String name, double unitPrice, int quantity) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Mã không được rỗng");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên không được rỗng");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn 0");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng không được âm");
        }
        this.code = code.trim();
        this.name = name.trim();
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double inventoryValue() {
        return unitPrice * quantity;
    }

    public String toCsvLine() {
        return String.format("%s,%s,%.2f,%d", code, name, unitPrice, quantity);
    }

    @Override
    public String toString() {
        return String.format("%s - %s: %,.0f VND (SL: %d, Đơn giá: %,.0f VND)",
                code, name, inventoryValue(), quantity, unitPrice);
    }
}
