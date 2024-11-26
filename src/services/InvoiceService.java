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
import java.util.stream.Collectors;
import java.text.ParseException;
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
            System.err.println("Loi khi doc file hoa don: " + e.getMessage());
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
            Customer customer = null;
            while (customer == null) {
                String customerId = getStringInput("Nhap ma khach hang (Enter de them khach hang moi): ");
                
                if (customerId.isEmpty()) {
                    // Nhập thông tin khách hàng mới
                    String name = getStringInput("Nhap ten khach hang: ");
                    String address = getStringInput("Nhap dia chi: ");
                    String phone = getStringInput("Nhap so dien thoai: ");
                    
                    // Tạo ID mới dựa trên danh sách hiện có
                    List<Customer> customers = customerService.getAllItems();
                    int maxId = 0;
                    for (Customer c : customers) {
                        if (c.getId().startsWith("KH")) {
                            try {
                                int id = Integer.parseInt(c.getId().substring(2));
                                maxId = Math.max(maxId, id);
                            } catch (NumberFormatException e) {
                                continue;
                            }
                        }
                    }
                    String newId = "KH" + String.format("%03d", maxId + 1);
                    
                    // Tạo khách hàng mới
                    customer = new Customer(newId, name, address, phone);
                    
                    try {
                        customerService.addCustomer(customer);
                        System.out.println("Da them khach hang moi: " + customer.getId());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Loi khi them khach hang: " + e.getMessage());
                        customer = null; // Reset để nhập lại
                    }
                } else {
                    // Tìm khách hàng hiện có
                    Optional<Customer> existingCustomer = customerService.findById(customerId);
                    if (existingCustomer.isPresent()) {
                        customer = existingCustomer.get();
                    } else {
                        System.out.println("Khong tim thay khach hang! Vui long thu lai hoac Enter de them moi.");
                    }
                }
            }

            String employeeId = getStringInput("Nhap ma nhan vien: ");
            employeeService.loadItems();
            
            Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay nhan vien!"));
            
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyHHmm");
            String invoiceId = "HD" + sdf.format(new Date());
            
            Invoice invoice = new Invoice(invoiceId, new Date());
            invoice.setCustomer(customer);
            invoice.setEmployee(employee);
            
            boolean hasProducts = false;
            while (true) {
                String productId = getStringInput("Nhap ma san pham (Enter de ket thuc): ");
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
            }
            
            items.add(invoice);
            fileHandler.saveInvoiceToText(invoice, filename);
            System.out.println("Tao hoa don thanh cong!");
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
        System.out.println("1. Tim theo ma hoa don");
        System.out.println("0. Quay lai");
        
        int choice = getIntInput("Nhap lua chon: ");
        loadItems(); // Đảm bảo load dữ liệu mới nhất
        
        switch (choice) {
            case 1 -> {
                // Hiển thị danh sách hóa đơn trước
                System.out.println("\nDanh sach hoa don hien tai:");
                displayFromFile();
                
                // Tiếp tục với quy trình tìm kiếm
                String invoiceId = getStringInput("\nNhap ma hoa don can tim: ");
                List<Invoice> results = items.stream()
                    .filter(i -> i.getId().toLowerCase().contains(invoiceId.toLowerCase()))
                    .collect(Collectors.toList());
                    
                if (results.isEmpty()) {
                    System.out.println("Khong tim thay hoa don nao!");
                } else {
                    System.out.println("\nKet qua tim kiem:");
                    results.forEach(Invoice::display);
                }
            }
            case 0 -> { return; }
            default -> System.out.println("Lua chon khong hop le!");
        }
    }

    public void displayCustomerPurchaseHistory(String customerId) {
        loadItems();
        List<Invoice> customerInvoices = items.stream()
            .filter(i -> i.getCustomer() != null && 
                         i.getCustomer().getId().equals(customerId))
            .collect(Collectors.toList());
        
        if (customerInvoices.isEmpty()) {
            System.out.println("Khach hang chua co lich su mua hang!");
        } else {
            System.out.println("\n=== LICH SU MUA HANG ===");
            customerInvoices.forEach(Invoice::display);
        }
    }

    private void parseInvoicesFromLines(List<String> lines) {
        Invoice currentInvoice = null;
        List<String> currentInvoiceDetails = new ArrayList<>();
        
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            
            if (line.startsWith("===== HOA DON")) {
                continue;
            }

            try {
                if (line.contains("Invoice [")) {
                    if (currentInvoice != null) {
                        final Invoice finalInvoice = currentInvoice;
                        finalInvoice.setDisplayDetails(new ArrayList<>(currentInvoiceDetails));
                        items.add(finalInvoice);
                    }
                    
                    currentInvoice = parseInvoice(line);
                    if (currentInvoice != null) {
                        currentInvoiceDetails = new ArrayList<>();
                        currentInvoiceDetails.add(line);
                    }
                } else if (currentInvoice != null) {
                    final Invoice finalCurrentInvoice = currentInvoice;
                    currentInvoiceDetails.add(line);
                    
                    if (line.contains("Khach hang:")) {
                        String customerInfo = line.substring("Khach hang:".length()).trim();
                        String[] parts = customerInfo.split("-", 2);
                        if (parts.length > 1) {
                            String customerId = parts[0].trim();
                            customerService.findById(customerId).ifPresent(finalCurrentInvoice::setCustomer);
                        }
                    } else if (line.contains("Nhan vien:")) {
                        String employeeInfo = line.substring("Nhan vien:".length()).trim();
                        String[] parts = employeeInfo.split("-", 2);
                        if (parts.length > 1) {
                            String employeeId = parts[0].trim();
                            employeeService.findById(employeeId).ifPresent(finalCurrentInvoice::setEmployee);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi xử lý dòng: " + line);
                System.err.println("Chi tiết lỗi: " + e.getMessage());
                continue;
            }
        }
        
        if (currentInvoice != null) {
            currentInvoice.setDisplayDetails(new ArrayList<>(currentInvoiceDetails));
            items.add(currentInvoice);
        }
    }

    private Invoice parseInvoice(String line) {
        try {
            String content = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
            String[] parts = content.split(",");
            String id = parts[0].substring(parts[0].indexOf(":") + 1).trim();
            String dateString = parts[1].substring(parts[1].indexOf(":") + 1).trim();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(dateString);
            return new Invoice(id, date);
        } catch (ParseException e) {
            System.err.println("Li khi parse Invoice: " + e.getMessage());
            return null;
        }
    }

    public void displayInvoiceStatistics() {
        try {
            // Đọc dữ liệu từ file
            List<String> lines = fileHandler.readAllLines(filename);
            if (lines.isEmpty()) {
                System.out.println("Chua co hoa don nao!");
                return;
            }

            // Khởi tạo các biến thống kê
            double totalRevenue = 0;
            int totalProducts = 0;
            Map<String, Integer> productSales = new HashMap<>();
            Map<String, CustomerStats> customerStats = new HashMap<>();
            Map<String, EmployeeStats> employeeStats = new HashMap<>();

            // Biến tạm để theo dõi hóa đơn hiện tại
            String currentCustomerId = null;
            String currentEmployeeId = null;
            double currentInvoiceTotal = 0;

            // Duyệt qua từng dòng trong file
            for (String line : lines) {
                line = line.trim();
                
                if (line.contains("Khach hang:")) {
                    currentCustomerId = line.substring(line.indexOf(":") + 1).trim().split("-")[0].trim();
                } else if (line.contains("Nhan vien:")) {
                    currentEmployeeId = line.substring(line.indexOf(":") + 1).trim().split("-")[0].trim();
                } else if (line.contains("(Ma:") && line.contains("x")) {
                    // Xử lý dòng sản phẩm
                    String[] parts = line.split("x");
                    String productInfo = parts[0].trim();
                    String productId = productInfo.substring(productInfo.indexOf("(Ma:") + 4, productInfo.indexOf(")")).trim();
                    int quantity = Integer.parseInt(parts[1].split(":")[0].trim());
                    
                    totalProducts += quantity;
                    productSales.merge(productId, quantity, Integer::sum);
                } else if (line.startsWith("Tong tien:")) {
                    String amountStr = line.substring(line.indexOf(":") + 1)
                        .replace("VND", "")
                        .replace(",", "")
                        .trim();
                    currentInvoiceTotal = Double.parseDouble(amountStr);
                    totalRevenue += currentInvoiceTotal;

                    // Tạo bản sao của các biến để sử dụng trong lambda
                    final String customerId = currentCustomerId;
                    final double invoiceTotal = currentInvoiceTotal;

                    // Cập nhật thống kê cho khách hàng và nhân viên
                    if (customerId != null) {
                        customerService.findById(customerId).ifPresent(customer -> 
                            customerStats.computeIfAbsent(customerId, 
                                k -> new CustomerStats(customer))
                                .addInvoice(invoiceTotal));
                    }

                    final String employeeId = currentEmployeeId;
                    if (employeeId != null) {
                        employeeService.findById(employeeId).ifPresent(employee -> 
                            employeeStats.computeIfAbsent(employeeId, 
                                k -> new EmployeeStats(employee))
                                .addInvoice(invoiceTotal));
                    }

                    // Reset cho hóa đơn tiếp theo
                    currentCustomerId = null;
                    currentEmployeeId = null;
                    currentInvoiceTotal = 0;
                }
            }

            // Hiển thị thống kê
            System.out.println("\n=== THONG KE HOA DON ===");
            System.out.printf("Tong doanh thu: %.0f VND%n", totalRevenue);
            System.out.println("Tong so san pham da ban: " + totalProducts);

            // Hiển thị top sản phẩm bán chạy
            System.out.println("\nTop san pham ban chay:");
            productSales.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .forEach(entry -> {
                    String productId = entry.getKey();
                    int quantity = entry.getValue();
                    productService.findById(productId).ifPresent(product -> {
                        double revenue = quantity * product.getPrice();
                        System.out.printf("- %s - %s: %d san pham - %.0f VND%n",
                            product.getId(),
                            product.getName(),
                            quantity,
                            revenue);
                    });
                });

            // Hiển thị thống kê theo khách hàng
            System.out.println("\nTop khach hang:");
            customerStats.values().stream()
                .sorted((c1, c2) -> Double.compare(c2.getTotalAmount(), c1.getTotalAmount()))
                .limit(5)
                .forEach(stats -> {
                    System.out.printf("- %s - %s: %d hoa don - %.0f VND%n",
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
                    System.out.printf("- %s - %s: %d hoa don - %.0f VND%n",
                        stats.getEmployee().getId(),
                        stats.getEmployee().getName(),
                        stats.getInvoiceCount(),
                        stats.getTotalAmount());
                });

        } catch (IOException e) {
            System.err.println("Loi khi doc file hoa don: " + e.getMessage());
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
} 