package services;

import models.Invoice;
import models.Product;
import models.Customer;
import models.Employee;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

public class InvoiceService extends BaseService<Invoice> {
    private CustomerService customerService;
    private EmployeeService employeeService;
    private ProductService productService;

    public InvoiceService(CustomerService customerService, 
                         EmployeeService employeeService, 
                         ProductService productService) {
        super("invoices.txt");
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.productService = productService;
        this.fileHandler.setServices(customerService, employeeService, productService);
        loadItems();
    }

    @Override
    protected void loadItems() {
        items = new ArrayList<>();
        try {
            List<String> lines = fileHandler.readAllLines(filename);
            parseInvoicesFromLines(lines);
        } catch (IOException e) {
           
        }
    }

    @Override
    public Optional<Invoice> findById(String id) {
        loadItems();
        return items.stream()
            .filter(i -> i.getId().equals(id))
            .findFirst();
    }

    @Override
    protected void validateInput(String id, String name) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma hoa don khong duoc de trong!");
        }
        if (findById(id).isPresent()) {
            throw new IllegalArgumentException("Ma hoa don da ton tai!");
        }
    }

    public void processNewInvoice() {
        try {
            // Hiển thị danh sách khách hàng
            System.out.println("\n=== DANH SACH KHACH HANG ===");
            customerService.displayFromFile();
            
            Customer customer = null;
            while (customer == null) {
                String customerId = getStringInput("\nNhap ma khach hang (Enter de them khach hang moi): ");
                
                if (customerId.isEmpty()) {
                    // Nhập thông tin khách hàng mới
                    String name = getStringInput("Nhap ten khach hang: ");
                    String address = getStringInput("Nhap dia chi: ");
                    String phone = getStringInput("Nhap so dien thoai: ");
                    
                    // Tạo ID mới cho khách hàng
                    List<Customer> customers = customerService.getAllItems();
                    int maxId = 0;
                    for (Customer c : customers) {
                        if (c.getId().startsWith("KH")) {
                            try {
                                int id = Integer.parseInt(c.getId().substring(2));
                                maxId = Math.max(maxId, id);
                            } catch (NumberFormatException e) {
                                // Bỏ qua nếu không parse được
                            }
                        }
                    }
                    String newId = String.format("KH%03d", maxId + 1);
                    
                    customer = new Customer(newId, name, address, phone);
                    customerService.addCustomer(customer);
                    System.out.println("Da them khach hang moi: " + customer.getId());
                } else {
                    Optional<Customer> existingCustomer = customerService.findById(customerId);
                    if (existingCustomer.isPresent()) {
                        customer = existingCustomer.get();
                    } else {
                        System.out.println("Khong tim thay khach hang! Vui long thu lai hoac Enter de them moi.");
                    }
                }
            }

            // Hiển thị danh sách nhân viên
            System.out.println("\n=== DANH SACH NHAN VIEN ===");
            employeeService.displayEmployeesFromFile();

            String employeeId = getStringInput("\nNhap ma nhan vien: ");
            employeeService.loadItems();
            
            Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nhan vien!"));
            
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmm");
            String invoiceId = "HD" + sdf.format(new Date());
            
            Invoice invoice = new Invoice(invoiceId, new Date());
            invoice.setCustomer(customer);
            invoice.setEmployee(employee);
            
            // Hiển thị danh sách sản phẩm
            System.out.println("\n=== DANH SACH SAN PHAM ===");
            productService.displayProductsFromFile();
            
            boolean hasProducts = false;
            while (true) {
                String productId = getStringInput("\nNhap ma san pham (Enter de ket thuc): ");
                if (productId.isEmpty()) {
                    if (!hasProducts) {
                        System.out.println("Hoa don phai co it nhat mot san pham!");
                        continue;
                    }
                    break;
                }
                
                productService.loadItems();
                Product product = productService.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay san pham!"));
                
                int quantity = getIntInput("Nhap so luong: ");
                if (quantity <= 0) {
                    System.out.println("So luong phai lon hon 0!");
                    continue;
                }
                
                if (product.getQuantity() < quantity) {
                    System.out.println("So luong ton kho khong du!");
                    continue;
                }
                
                invoice.addItem(product, quantity);
                productService.updateProductQuantity(productId, quantity);
                hasProducts = true;
                
                // Hiển thị lại danh sách sản phẩm sau mỗi lần thêm
                System.out.println("\n=== DANH SACH SAN PHAM HIEN TAI ===");
                productService.displayProductsFromFile();
            }
            
            items.add(invoice);
            fileHandler.saveInvoiceToText(invoice, filename);
            System.out.println("\nTao hoa don thanh cong!");
            invoice.display();
            
        } catch (IllegalArgumentException e) {
            System.out.println("Loi: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Co loi xay ra: " + e.getMessage());
        }
    }

    public void displayInvoices() {
        displayFromFile();
    }

    public void searchInvoices() {
        System.out.println("\n=== TIM KIEM HOA DON ===");
        String invoiceId = getStringInput("\nNhap ma hoa don can tim: ").toUpperCase();
        
        try {
            List<String> lines = fileHandler.readAllLines(filename);
            boolean found = false;
            boolean isCurrentInvoice = false;
            List<String> currentInvoiceLines = new ArrayList<>();
            
            for (String line : lines) {
                if (line.contains("Invoice [")) {
                    if (isCurrentInvoice) {
                        // In hóa đơn trước đó nếu đang trong quá trình hiển thị
                        printInvoice(currentInvoiceLines);
                        currentInvoiceLines.clear();
                        isCurrentInvoice = false;
                    }
                    
                    // Kiểm tra nếu là hóa đơn cần tìm
                    if (line.toUpperCase().contains(invoiceId)) {
                        isCurrentInvoice = true;
                        found = true;
                    }
                }
                
                if (isCurrentInvoice) {
                    currentInvoiceLines.add(line);
                }
            }
            
            // In hóa đơn cuối cùng nếu có
            if (!currentInvoiceLines.isEmpty()) {
                printInvoice(currentInvoiceLines);
            }
            
            if (!found) {
                System.out.println("Khong tim thay hoa don nao!");
            }
            
        } catch (IOException e) {
            System.err.println("Loi khi doc file: " + e.getMessage());
        }
    }

    private void printInvoice(List<String> invoiceLines) {
        System.out.println("\n========== CHI TIET HOA DON ==========");
        for (String line : invoiceLines) {
            if (!line.trim().isEmpty()) {
                System.out.println(line);
            }
        }
    }

    public void displayCustomerPurchaseHistory(String customerId) {
        System.out.println("\n=== LICH SU MUA HANG ===");
        try {
            List<String> lines = fileHandler.readAllLines(filename);
            boolean found = false;
            boolean isCurrentInvoice = false;
            List<String> currentInvoiceLines = new ArrayList<>();
            boolean isCustomerInvoice = false;
            
            for (String line : lines) {
                if (line.contains("Invoice [")) {
                    if (isCurrentInvoice && isCustomerInvoice) {
                        // In hóa đơn trước đó của khách hàng này
                        printInvoice(currentInvoiceLines);
                    }
                    currentInvoiceLines.clear();
                    isCurrentInvoice = true;
                    isCustomerInvoice = false;
                }
                
                if (isCurrentInvoice) {
                    currentInvoiceLines.add(line);
                    // Kiểm tra nếu là dòng thông tin khách hàng
                    if (line.contains("Khach hang:") && line.contains(customerId)) {
                        isCustomerInvoice = true;
                        found = true;
                    }
                }
            }
            
            // In hóa đơn cuối cùng nếu là của khách hàng này
            if (!currentInvoiceLines.isEmpty() && isCustomerInvoice) {
                printInvoice(currentInvoiceLines);
            }
            
            if (!found) {
                System.out.println("Khach hang chua co lich su mua hang!");
            }
            
        } catch (IOException e) {
        }
    }

    private void parseInvoicesFromLines(List<String> lines) {
        Invoice currentInvoice = null;
        
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            
            try {
                if (line.contains("Invoice [")) {
                    if (currentInvoice != null) {
                        items.add(currentInvoice);
                    }
                    
                    // Parse invoice header
                    String[] parts = line.split("\\[|\\]")[1].split(",");
                    String id = parts[0].split(":")[1].trim();
                    String dateStr = parts[1].split(":")[1].trim();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = sdf.parse(dateStr);
                    
                    currentInvoice = new Invoice(id, date);
                    
                } else if (currentInvoice != null) {
                    final Invoice invoice = currentInvoice;
                    
                    if (line.contains("Khach hang:")) {
                        String customerInfo = line.substring(line.indexOf(":") + 1).trim();
                        String[] parts = customerInfo.split("-", 2);
                        if (parts.length > 1) {
                            String customerId = parts[0].trim();
                            customerService.findById(customerId).ifPresent(invoice::setCustomer);
                        }
                    } else if (line.contains("Nhan vien:")) {
                        String employeeInfo = line.substring(line.indexOf(":") + 1).trim();
                        String[] parts = employeeInfo.split("-", 2);
                        if (parts.length > 1) {
                            String employeeId = parts[0].trim();
                            employeeService.findById(employeeId).ifPresent(invoice::setEmployee);
                        }
                    } else if (line.contains("(Ma:") && line.contains("x")) {
                        // Parse product details
                        String[] parts = line.split("x");
                        String productInfo = parts[0].trim();
                        String productId = productInfo.substring(
                            productInfo.indexOf("(Ma:") + 4,
                            productInfo.indexOf(")")
                        ).trim();
                        
                        // Extract quantity
                        String quantityStr = parts[1].trim();
                        int quantity = Integer.parseInt(
                            quantityStr.substring(0, quantityStr.indexOf(":")).trim()
                        );
                        
                        // Add product to invoice
                        productService.findById(productId).ifPresent(product -> 
                            invoice.addItem(product, quantity)
                        );
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        
        // Add the last invoice if exists
        if (currentInvoice != null) {
            items.add(currentInvoice);
        }
    }

    public void displayInvoiceStatistics() {
        try {
            List<String> lines = fileHandler.readAllLines(filename);
            if (lines.isEmpty()) {
                System.out.println("Chua co hoa don nao!");
                return;
            }

            double totalRevenue = 0;
            int totalProducts = 0;
            Map<String, ProductSalesStats> productSales = new HashMap<>();
            Map<String, CustomerStats> customerStats = new HashMap<>();
            Map<String, EmployeeStats> employeeStats = new HashMap<>();

            String currentCustomerId = null;
            String currentEmployeeId = null;
            double currentInvoiceTotal = 0;

            for (String line : lines) {
                line = line.trim();
                
                if (line.contains("Khach hang:")) {
                    currentCustomerId = line.substring(line.indexOf(":") + 1).trim().split("-")[0].trim();
                } else if (line.contains("Nhan vien:")) {
                    currentEmployeeId = line.substring(line.indexOf(":") + 1).trim().split("-")[0].trim();
                } else if (line.contains("(Ma:") && line.contains("x")) {
                    try {
                        // Xử lý dòng sản phẩm
                        String[] parts = line.split("x");
                        String productInfo = parts[0].trim();
                        String productName = productInfo.substring(2, productInfo.indexOf("(Ma:")).trim();
                        String productId = productInfo.substring(productInfo.indexOf("(Ma:") + 4, productInfo.indexOf(")")).trim();
                        
                        // Lấy số lượng và giá
                        String quantityAndPrice = parts[1].trim();
                        int quantity = Integer.parseInt(quantityAndPrice.substring(0, quantityAndPrice.indexOf(":")).trim());
                        double price = Double.parseDouble(quantityAndPrice.substring(quantityAndPrice.indexOf(":") + 1)
                            .replace("VND", "").replace(",", "").trim());
                        
                        totalProducts += quantity;
                        
                        // Cập nhật thống kê sản phẩm với giá đơn vị chính xác
                        double pricePerUnit = price / quantity;
                        ProductSalesStats stats = productSales.computeIfAbsent(productId,
                            k -> new ProductSalesStats(productId, productName));
                        stats.addSale(quantity, pricePerUnit);
                        
                    } catch (Exception e) {
                        System.err.println("Loi khi xu ly chi tiet san pham: " + e.getMessage());
                    }
                } else if (line.startsWith("Tong tien:")) {
                    String amountStr = line.substring(line.indexOf(":") + 1)
                        .replace("VND", "")
                        .replace(",", "")
                        .trim();
                    currentInvoiceTotal = Double.parseDouble(amountStr);
                    totalRevenue += currentInvoiceTotal;

                    // Cập nhật thống kê khách hàng và nhân viên
                    final String finalCustomerId = currentCustomerId;
                    final String finalEmployeeId = currentEmployeeId;
                    final double finalInvoiceTotal = currentInvoiceTotal;

                    if (finalCustomerId != null) {
                        customerService.findById(finalCustomerId).ifPresent(customer -> 
                            customerStats.computeIfAbsent(finalCustomerId, 
                                k -> new CustomerStats(customer))
                                .addInvoice(finalInvoiceTotal));
                    }

                    if (finalEmployeeId != null) {
                        employeeService.findById(finalEmployeeId).ifPresent(employee -> 
                            employeeStats.computeIfAbsent(finalEmployeeId, 
                                k -> new EmployeeStats(employee))
                                .addInvoice(finalInvoiceTotal));
                    }

                    currentCustomerId = null;
                    currentEmployeeId = null;
                    currentInvoiceTotal = 0;
                }
            }

            // Hiển thị thống kê
            System.out.println("\n=== THONG KE HOA DON ===");
            System.out.printf("Tong doanh thu: %,.0f VND%n", totalRevenue);
            System.out.println("Tong so san pham da ban: " + totalProducts);

            // Hiển thị top sản phẩm bán chạy
            System.out.println("\nTop san pham ban chay:");
            productSales.values().stream()
                .sorted((p1, p2) -> Double.compare(p2.getTotalRevenue(), p1.getTotalRevenue()))
                .limit(5)
                .forEach(stats -> {
                    System.out.printf("- %s - %s: %d san pham - %,.0f VND%n",
                        stats.getProductId(),
                        stats.getProductName(),
                        stats.getTotalQuantity(),
                        stats.getTotalRevenue());
                });

            // Hiển thị thống kê theo khách hàng
            System.out.println("\nTop khach hang:");
            customerStats.values().stream()
                .sorted((c1, c2) -> Double.compare(c2.getTotalAmount(), c1.getTotalAmount()))
                .limit(5)
                .forEach(stats -> {
                    System.out.printf("- %s - %s: %d hoa don - %,.0f VND%n",
                        stats.getCustomer().getId(),
                        stats.getCustomer().getName(),
                        stats.getInvoiceCount(),
                        stats.getTotalAmount());
                });

            // Hiển thị thống kê theo nhân viên
            System.out.println("\nHieu suat nhan vien:");
            employeeStats.values().stream()
                .sorted((e1, e2) -> Double.compare(e2.getTotalAmount(), e1.getTotalAmount()))
                .forEach(stats -> {
                    System.out.printf("- %s - %s: %d hoa don - %,.0f VND%n",
                        stats.getEmployee().getId(),
                        stats.getEmployee().getName(),
                        stats.getInvoiceCount(),
                        stats.getTotalAmount());
                });

        } catch (Exception e) {
            System.err.println("Loi khi xu ly thong ke: " + e.getMessage());
        }
    }

    // Lớp hỗ trợ thống kê khách hàng
    private static class CustomerStats {
        private final Customer customer;
        private int invoiceCount = 0;
        private double totalAmount = 0;
        
        public CustomerStats(Customer customer) {
            this.customer = customer;
        }
        
        public void addInvoice(double amount) {
            invoiceCount++;
            totalAmount += amount;
        }
        
        public Customer getCustomer() { return customer; }
        public int getInvoiceCount() { return invoiceCount; }
        public double getTotalAmount() { return totalAmount; }
    }

    // Lớp hỗ trợ thống kê nhân viên
    private static class EmployeeStats {
        private final Employee employee;
        private int invoiceCount = 0;
        private double totalAmount = 0;
        
        public EmployeeStats(Employee employee) {
            this.employee = employee;
        }
        
        public void addInvoice(double amount) {
            invoiceCount++;
            totalAmount += amount;
        }
        
        public Employee getEmployee() { return employee; }
        public int getInvoiceCount() { return invoiceCount; }
        public double getTotalAmount() { return totalAmount; }
    }

    // Thêm class ProductSalesStats vào cuối file InvoiceService.java
    private static class ProductSalesStats {
        private final String productId;
        private final String productName;
        private int totalQuantity = 0;
        private double totalRevenue = 0;
        
        public ProductSalesStats(String productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }
        
        public void addSale(int quantity, double pricePerUnit) {
            this.totalQuantity += quantity;
            this.totalRevenue += quantity * pricePerUnit;
        }
        
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getTotalQuantity() { return totalQuantity; }
        public double getTotalRevenue() { return totalRevenue; }
    }
} 