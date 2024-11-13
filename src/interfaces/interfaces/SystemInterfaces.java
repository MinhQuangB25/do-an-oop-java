package interfaces;

import java.util.List;

public interface SystemInterfaces {
    // Interface cho việc áp dụng giảm giá
    interface Discountable {
        double calculateDiscount();
        void applyDiscount(double discountPercent);
    }

    // Interface cho việc lưu trữ và đọc file
    interface Storable<T> {
        void saveToFile(String filename, List<T> data);
        List<T> loadFromFile(String filename);
    }

    // Interface cho các đối tượng có ID
    interface Identifiable {
        String getId();
        void setId(String id);
    }

    // Interface cho việc in thông tin
    interface Printable {
        String getInfo();
        void display();
    }
} 