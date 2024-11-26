//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.List;
//import java.util.Optional;
import java.util.Scanner;
import models.*;
import services.*;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static ProductService productService = new ProductService();
    private static CustomerService customerService = new CustomerService();
    private static EmployeeService employeeService = new EmployeeService();
    private static InvoiceService invoiceService = new InvoiceService(customerService, employeeService, productService);

    static {
        employeeService.setInvoiceService(invoiceService);
    }

    public static void main(String[] args) {
        while (true) {
            showMainMenu();
            int choice = getIntInput("Nhap lua chon cua ban: ");
            
            switch (choice) {
                case 1 -> manageProducts();
                case 2 -> manageCustomers();
                case 3 -> manageEmployees();
                case 4 -> manageInvoices();
                case 0 -> {
                    System.out.println("Cam on ban da su dung chuong trinh!");
                    return;
                }
                default -> System.out.println("Lua chon khong hop le!");
            }
        }
    }

    private static void showMainMenu() {
        System.out.println("\n===== QUAN LY CUA HANG MAY TINH =====");
        System.out.println("1. Quan ly san pham");
        System.out.println("2. Quan ly khach hang");
        System.out.println("3. Quan ly nhan vien");
        System.out.println("4. Quan ly hoa don");
        System.out.println("0. Thoat");
    }

    private static void manageProducts() {
        while (true) {
            System.out.println("\n===== QUAN LY SAN PHAM =====");
            System.out.println("1. Them may tinh");
            System.out.println("2. Them phu kien");
            System.out.println("3. Xem danh sach san pham");
            System.out.println("4. Tim kiem san pham");
            System.out.println("5. Cap nhat san pham");
            System.out.println("6. Xoa san pham");
            System.out.println("7. Thong ke kho hang");
            System.out.println("0. Quay lai");

            try {
                int choice = getIntInput("Nhap lua chon: ");
                switch (choice) {
                    case 1 -> {
                        String id = getStringInput("Nhap ma may tinh: ");
                        String name = getStringInput("Nhap ten may tinh: ");
                        double price = getDoubleInput("Nhap gia (VND): ");
                        int quantity = getIntInput("Nhap so luong: ");
                        String cpu = getStringInput("Nhap CPU (vi du: Intel i7): ");
                        String ram = getStringInput("Nhap RAM (vi du: 16): ");
                        String hardDrive = getStringInput("Nhap dung luong o cung (vi du: 512 hoac 1TB): ");
                        
                        productService.addComputer(id, name, price, quantity, cpu, ram, hardDrive);
                    }
                    case 2 -> {
                        String id = getStringInput("Nhap ma phu kien: ");
                        String name = getStringInput("Nhap ten phu kien: ");
                        double price = getDoubleInput("Nhap gia: ");
                        int quantity = getIntInput("Nhap so luong: ");
                        String type = getStringInput("Nhap loai phu kien: ");
                        
                        productService.addAccessory(id, name, price, quantity, type);
                    }
                    case 3 -> {
                        List<Product> products = productService.getAllProducts();
                        if (products.isEmpty()) {
                            System.out.println("Danh sach san pham trong!");
                        } else {
                            System.out.println("\nDanh sach san pham:");
                            products.forEach(p -> {
                                System.out.println("----------------------------------------");
                                p.display();
                            });
                        }
                    }
                    case 4 -> {
                        String keyword = getStringInput("Nhap tu khoa tim kiem: ");
                        List<Product> results = productService.searchProducts(keyword);
                        if (results.isEmpty()) {
                            System.out.println("Khong tim thay san pham nao!");
                        } else {
                            System.out.println("\nKet qua tim kiem:");
                            results.forEach(p -> {
                                System.out.println("----------------------------------------");
                                p.display();
                            });
                        }
                    }
                    case 5 -> {
                        // Hiển thị danh sách sản phẩm trước
                        System.out.println("\nDanh sach san pham hien tai:");
                        productService.displayProductsFromFile();
                        
                        // Tiếp tục với quy trình cập nhật
                        String id = getStringInput("\nNhap ma san pham can cap nhat: ");
                        String name = getStringInput("Nhap ten moi (Enter de giu nguyen): ");
                        String priceStr = getStringInput("Nhap gia moi (Enter de giu nguyen): ");
                        String quantityStr = getStringInput("Nhap so luong moi (Enter de giu nguyen): ");
                        
                        Double price = priceStr.isEmpty() ? null : Double.parseDouble(priceStr);
                        Integer quantity = quantityStr.isEmpty() ? null : Integer.parseInt(quantityStr);
                        
                        productService.updateProductDetails(id, name, price, quantity);
                    }
                    case 6 -> {
                        // Hiển thị danh sách sản phẩm trước
                        System.out.println("\nDanh sach san pham hien tai:");
                        productService.displayProductsFromFile();
                        
                        // Tiếp tục với quy trình xóa
                        String id = getStringInput("\nNhap ma san pham can xoa: ");
                        String confirm = getStringInput("Ban co chac chan muon xoa? (Y/N): ");
                        if (confirm.equalsIgnoreCase("Y")) {
                            productService.deleteProductById(id);
                        }
                    }
                    case 7 -> productService.displayInventoryStatistics();
                    case 0 -> { return; }
                    default -> System.out.println("Lua chon khong hop le!");
                }
            } catch (Exception e) {
                System.out.println("Loi: " + e.getMessage());
            }
        }
    }

    private static void manageCustomers() {
        while (true) {
            System.out.println("\n===== QUAN LY KHACH HANG =====");
            System.out.println("1. Xem danh sach khach hang");
            System.out.println("2. Tim kiem khach hang");
            System.out.println("3. Cap nhat khach hang");
            System.out.println("4. Xoa khach hang");
            System.out.println("5. Mua hang");
            System.out.println("6. Xem lich su mua hang");
            System.out.println("0. Quay lai");

            try {
                int choice = getIntInput("Nhap lua chon: ");
                switch (choice) {
                    case 1 -> customerService.displayFromFile();
                    case 2 -> customerService.searchItems();
                    case 3 -> {
                        // Hiển thị danh sách khách hàng trước
                        System.out.println("\nDanh sach khach hang hien tai:");
                        customerService.displayFromFile();
                        
                        // Tiếp tục với quy trình cập nhật
                        String id = getStringInput("\nNhap ma khach hang can cap nhat: ");
                        String name = getStringInput("Nhap ten moi (Enter de giu nguyen): ");
                        String address = getStringInput("Nhap dia chi moi (Enter de giu nguyen): ");
                        String phone = getStringInput("Nhap so dien thoai moi (Enter de giu nguyen): ");
                        
                        customerService.updateCustomerDetails(id, name, address, phone);
                    }
                    case 4 -> {
                        // Hiển thị danh sách khách hàng trước
                        System.out.println("\nDanh sach khach hang hien tai:");
                        customerService.displayFromFile();
                        
                        // Tiếp tục với quy trình xóa
                        String id = getStringInput("\nNhap ma khach hang can xoa: ");
                        String confirm = getStringInput("Ban co chac chan muon xoa? (Y/N): ");
                        if (confirm.equalsIgnoreCase("Y")) {
                            customerService.deleteCustomer(id);
                        }
                    }
                    case 5 -> invoiceService.processNewInvoice();
                    case 6 -> {
                        String customerId = getStringInput("Nhap ma khach hang: ");
                        invoiceService.displayCustomerPurchaseHistory(customerId);
                    }
                    case 0 -> { return; }
                    default -> System.out.println("Lua chon khong hop le!");
                }
            } catch (Exception e) {
                System.out.println("Loi: " + e.getMessage());
            }
        }
    }

    private static void manageEmployees() {
        while (true) {
            System.out.println("\n===== QUAN LY NHAN VIEN =====");
            System.out.println("1. Them nhan vien");
            System.out.println("2. Xem danh sach nhan vien");
            System.out.println("3. Tim kiem nhan vien");
            System.out.println("4. Cap nhat thong tin nhan vien");
            System.out.println("5. Xoa nhan vien");
            System.out.println("0. Quay lai");

            try {
                int choice = getIntInput("Nhap lua chon: ");
                switch (choice) {
                    case 1 -> {
                        String id = getStringInput("Nhap ma nhan vien: ");
                        String name = getStringInput("Nhap ten nhan vien: ");
                        String address = getStringInput("Nhap dia chi: ");
                        String phone = getStringInput("Nhap so dien thoai: ");
                        String position = getStringInput("Nhap chuc vu: ");
                        
                        employeeService.addEmployee(id, name, address, phone, position);
                    }
                    case 2 -> employeeService.displayEmployeesFromFile();
                    case 3 -> employeeService.searchEmployees();
                    case 4 -> {
                        // Hiển thị danh sách nhân viên trước
                        System.out.println("\nDanh sach nhan vien hien tai:");
                        employeeService.displayEmployeesFromFile();
                        
                        // Tiếp tục với quy trình cập nhật
                        String id = getStringInput("\nNhap ma nhan vien can cap nhat: ");
                        String name = getStringInput("Nhap ten moi (Enter de giu nguyen): ");
                        String address = getStringInput("Nhap dia chi moi (Enter de giu nguyen): ");
                        String phone = getStringInput("Nhap so dien thoai moi (Enter de giu nguyen): ");
                        String position = getStringInput("Nhap chuc vu moi (Enter de giu nguyen): ");
                        
                        employeeService.updateEmployeeDetails(id, name, address, phone, position);
                    }
                    case 5 -> {
                        // Hiển thị danh sách nhân viên trước
                        System.out.println("\nDanh sach nhan vien hien tai:");
                        employeeService.displayEmployeesFromFile();
                        
                        // Tiếp tục với quy trình xóa
                        String id = getStringInput("\nNhap ma nhan vien can xoa: ");
                        String confirm = getStringInput("Ban co chac chan muon xoa? (Y/N): ");
                        if (confirm.equalsIgnoreCase("Y")) {
                            employeeService.deleteEmployeeById(id);
                        }
                    }
                    case 0 -> { return; }
                    default -> System.out.println("Lua chon khong hop le!");
                }
            } catch (Exception e) {
                System.out.println("Loi: " + e.getMessage());
            }
        }
    }

    private static void manageInvoices() {
        while (true) {
            System.out.println("\n===== QUAN LY HOA DON =====");
            System.out.println("1. Xem danh sach hoa don");
            System.out.println("2. Tim kiem hoa don");
            System.out.println("3. Thong ke hoa don");
            System.out.println("0. Quay lai");

            try {
                int choice = getIntInput("Nhap lua chon: ");
                switch (choice) {
                    case 1 -> invoiceService.displayInvoices();
                    case 2 -> invoiceService.searchInvoices();
                    case 3 -> invoiceService.displayInvoiceStatistics();
                    case 0 -> { return; }
                    default -> System.out.println("Lua chon khong hop le!");
                }
            } catch (Exception e) {
                System.out.println("Loi: " + e.getMessage());
            }
        }
    }

    // Utility methods for input handling
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so nguyen hop le!");
            }
        }
    }

    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so thuc hop le!");
            }
        }
    }
} 