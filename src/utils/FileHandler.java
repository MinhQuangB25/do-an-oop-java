package utils;

import models.*;
import services.CustomerService;
import services.EmployeeService;
import services.ProductService;
import interfaces.SystemInterfaces.Printable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class FileHandler<T> {
    private String directory = "do-an-oop-lam-lai/data/";
    private boolean isUpdatingFile = false;
    private CustomerService customerService;
    private EmployeeService employeeService;
    private ProductService productService;

    public FileHandler() {
        createDataDirectoryIfNotExists();
    }

    private void createDataDirectoryIfNotExists() {
        File dataDir = new File(directory);
        if (!dataDir.exists()) {
            System.out.println("Thư mục data không tồn tại: " + dataDir.getAbsolutePath());
            dataDir.mkdirs();
        } 
    }

    public void setUpdatingFile(boolean updating) {
        this.isUpdatingFile = updating;
    }

    public String getDirectory() {
        return directory;
    }

    public void saveToFile(String filename, List<T> data) {
        try {
            Path path = Paths.get(directory + filename);
            List<String> lines = new ArrayList<>();
            
            // Nếu đang cập nhật toàn bộ file
            if (isUpdatingFile) {
                // Thêm header cho file mới
                lines.add("===== DANH SACH " + getHeaderType(filename) + " =====");
                
                // Thêm tất cả dữ liệu
                for (T item : data) {
                    lines.add("----------------------------------------");
                    if (item instanceof Printable) {
                        lines.add(((Printable) item).getInfo());
                    }
                }
                
                // Ghi đè toàn bộ file
                Files.write(path, lines, StandardCharsets.UTF_8);
            } else {
                // Chế độ thêm mới (append)
                if (!Files.exists(path)) {
                    // Tạo file mới với header nếu chưa tồn tại
                    lines.add("===== DANH SACH " + getHeaderType(filename) + " =====");
                    Files.write(path, lines, StandardCharsets.UTF_8);
                }
                
                // Đọc nội dung hiện tại của file
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                
                // Thêm dữ liệu mới vào cuối
                for (T item : data) {
                    lines.add("----------------------------------------");
                    if (item instanceof Printable) {
                        lines.add(((Printable) item).getInfo());
                    }
                }
                
                // Ghi lại toàn bộ nội dung vào file
                Files.write(path, lines, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("Loi khi luu file: " + e.getMessage());
        }
    }

    private String getHeaderType(String filename) {
        if (filename.contains("customer")) return "CUSTOMER";
        if (filename.contains("product")) return "SAN PHAM";
        if (filename.contains("employee")) return "NHAN VIEN";
        if (filename.contains("invoice")) return "HOA DON";
        return "";
    }

    public void saveInvoiceToText(Invoice invoice, String filename) {
        try {
            Path path = Paths.get(directory + filename);
            List<String> lines = new ArrayList<>();
            
            // Nếu file chưa tồn tại, tạo header
            if (!Files.exists(path)) {
                lines.add("===== HOA DON BAN HANG =====");
            } else {
                // Nếu file đã tồn tại, đọc toàn bộ nội dung cũ
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            }
            
            // Thêm hóa đơn mới vào cuối với format mới
            lines.add("----------------------------------------");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            lines.add(String.format("Invoice [ID: %s, Date: %s]", 
                invoice.getId(),
                sdf.format(invoice.getDate())));
            lines.add("----------------------------------------");
            
            // Thông tin khách hàng
            lines.add(String.format("Khach hang: %s - %s", 
                invoice.getCustomer().getId(),
                invoice.getCustomer().getName()));
            lines.add("----------------------------------------");
            
            // Thông tin nhân viên
            lines.add(String.format("Nhan vien: %s - %s", 
                invoice.getEmployee().getId(),
                invoice.getEmployee().getName()));
            lines.add("----------------------------------------");
            
            // Chi tiết sản phẩm
            lines.add("Chi tiet san pham:");
            lines.add("----------------------------------------");
            
            for (Invoice.InvoiceDetail detail : invoice.getItems()) {
                lines.add(String.format("- %s (Ma: %s) x%d: %,.0f VND", 
                    detail.getProduct().getName(),
                    detail.getProduct().getId(),
                    detail.getQuantity(),
                    detail.getProduct().getPrice() * detail.getQuantity()));
                lines.add("----------------------------------------");
            }
            
            // Tổng tiền
            lines.add(String.format("Tong tien: %,.0f VND", invoice.getTotalAmount()));
            lines.add("----------------------------------------");
            
            // Ghi toàn bộ nội dung vào file
            Files.write(path, lines, StandardCharsets.UTF_8);
                
        } catch (IOException e) {
            System.err.println("Loi khi luu hoa don: " + e.getMessage());
        }
    }

    public void readTextFile(String filename) {
        try {
            List<String> lines = readAllLines(filename);
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> loadFromFile(String filename) {
        List<T> items = new ArrayList<>();
        try {
            List<String> lines = readAllLines(filename);
            
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("===") || line.startsWith("---")) {
                    continue;
                }
                // Parsing logic for different types
                if (line.contains("Computer [")) {
                    T item = (T) parseComputer(line);
                    if (item != null) {
                        items.add(item);
                    }
                } else if (line.contains("Accessory [")) {
                    T item = (T) parseAccessory(line);
                    if (item != null) {
                        items.add(item);
                    }
                } else if (line.contains("Customer [")) {
                    T item = (T) parseCustomer(line);
                    if (item != null) items.add(item);
                } else if (line.contains("Employee [")) {
                    T item = (T) parseEmployee(line);
                    if (item != null) items.add(item);
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
        }
        return items;
    }

    private Computer parseComputer(String line) {
        try {
            Map<String, String> data = extractData(line);
            return new Computer(
                data.getOrDefault("ID", "").trim(),
                data.getOrDefault("Name", "").trim(),
                Double.parseDouble(data.getOrDefault("Price", "0").trim().replace(",", "")),
                Integer.parseInt(data.getOrDefault("Quantity", "0").trim()),
                data.getOrDefault("CPU", "").trim(),
                data.getOrDefault("RAM", "").trim(),
                data.getOrDefault("Hard Drive", "").trim()
            );
        } catch (Exception e) {
            System.err.println("Lỗi khi parse Computer: " + e.getMessage());
            return null;
        }
    }

    private Accessory parseAccessory(String line) {
        try {
            Map<String, String> data = extractData(line);
            return new Accessory(
                data.getOrDefault("ID", "").trim(),
                data.getOrDefault("Name", "").trim(),
                Double.parseDouble(data.getOrDefault("Price", "0").trim().replace(",", "")),
                Integer.parseInt(data.getOrDefault("Quantity", "0").trim()),
                data.getOrDefault("Type", "").trim()
            );
        } catch (Exception e) {
            System.err.println("Lỗi khi parse Accessory: " + e.getMessage());
            return null;
        }
    }

   

    private Map<String, String> extractData(String line) {
        Map<String, String> data = new LinkedHashMap<>();
        try {
            String content = line.substring(line.indexOf("[") + 1, line.lastIndexOf("]"));
            String[] pairs = content.split(",(?=\\s*(?!Basic Salary:)[A-Za-z]+:)");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    
                    if (!key.equals("Basic Salary") || !data.containsKey(key)) {
                        data.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi extract data: " + e.getMessage());
        }
        return data;
    }

    public void writeFormattedText(String filename, String header, List<String> contentLines) {
        try {
            Path path = Paths.get(directory + filename);
            List<String> lines = new ArrayList<>();
            
            // Nếu file không tồn tại, tạo mới với header
            if (!Files.exists(path)) {
                lines.add(header);
            } else {
                // Nếu file đã tồn tại, đọc nội dung cũ
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            }
            
            // Thêm nội dung mới vào cuối
            for (String content : contentLines) {
                lines.add("----------------------------------------");
                lines.add(content);
            }
            lines.add("----------------------------------------");
            
            // Ghi toàn bộ nội dung vào file
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Loi khi ghi file: " + e.getMessage());
        }
    }

    public List<String> readAllLines(String filename) throws IOException {
        Path path = Paths.get(directory + filename);
        if (Files.exists(path)) {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        }
        return new ArrayList<>();
    }

    public void updateInvoiceFormat(String filename) {
        try {
            Path path = Paths.get(directory + filename);
            if (!Files.exists(path)) {
                return;
            }

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<String> updatedLines = new ArrayList<>();

            for (String line : lines) {
                if (line.contains("đ")) {
                    // Thay thế "đ" bằng " VND" cho các dòng có giá tiền
                    line = line.replace("đ", " VND");
                }
                updatedLines.add(line);
            }

            // Ghi lại file với nội dung đã cập nhật
            Files.write(path, updatedLines, StandardCharsets.UTF_8);
            
        } catch (IOException e) {
            System.err.println("Lỗi khi cập nhật file hóa đơn: " + e.getMessage());
        }
    }

    public void setServices(CustomerService customerService, EmployeeService employeeService, ProductService productService) {
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.productService = productService;
    }

    private Customer parseCustomer(String line) {
        try {
            Map<String, String> data = extractData(line);
            String id = data.getOrDefault("ID", "").trim();
            String name = data.getOrDefault("Name", "").trim();
            String address = data.getOrDefault("Address", "").trim();
            String phone = data.getOrDefault("Phone", "").trim();
            
            // Kiểm tra nếu phone chứa chữ và address chứa số, đổi chỗ cho nhau
            if (phone.matches(".*[a-zA-Z].*") && address.matches("\\d+")) {
                String temp = phone;
                phone = address;
                address = temp;
            }
            
            return new Customer(id, name, address, phone);
        } catch (Exception e) {
            System.err.println("Lỗi khi parse Customer: " + e.getMessage());
            return null;
        }
    }

    private Employee parseEmployee(String line) {
        try {
            Map<String, String> data = extractData(line);
            String basicSalaryStr = data.getOrDefault("Basic Salary", "0").trim();
            // Loại bỏ dấu phẩy và chuyển đổi sang double
            double basicSalary = Double.parseDouble(basicSalaryStr.replace(",", ""));
            
            Employee employee = new Employee(
                data.getOrDefault("ID", "").trim(),
                data.getOrDefault("Name", "").trim(),
                data.getOrDefault("Phone", "").trim(),
                data.getOrDefault("Address", "").trim(),
                data.getOrDefault("Position", "").trim(),
                basicSalary
            );
            return employee;
        } catch (Exception e) {
            System.err.println("Lỗi khi parse Employee: " + e.getMessage());
            e.printStackTrace(); // Thêm dòng này để debug
            return null;
        }
    }

    public List<Invoice> loadInvoicesFromFile(String filename) {
        List<Invoice> invoices = new ArrayList<>();
        try {
            List<String> lines = readAllLines(filename);
            Invoice currentInvoice = null;
            Customer customer = null;
            Employee employee = null;
            List<Invoice.InvoiceDetail> items = new ArrayList<>();
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("=====") || line.equals("----------------------------------------")) {
                    continue;
                }

                if (line.startsWith("Invoice [ID:")) {
                    // Lưu hóa đơn trước đó nếu có đầy đủ thông tin
                    if (currentInvoice != null && customer != null && employee != null) {
                        currentInvoice.setCustomer(customer);
                        currentInvoice.setEmployee(employee);
                        currentInvoice.setItems(new ArrayList<>(items));
                        invoices.add(currentInvoice);
                    }
                    
                    // Parse thông tin hóa đơn mới
                    String id = line.substring(line.indexOf("ID:") + 4, line.indexOf(",")).trim();
                    String dateStr = line.substring(line.indexOf("Date:") + 6, line.indexOf("]")).trim();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = sdf.parse(dateStr);
                    
                    currentInvoice = new Invoice(id, date);
                    items = new ArrayList<>(); // Reset items list
                } else if (line.startsWith("Khach hang:")) {
                    try {
                        String[] parts = line.substring("Khach hang:".length()).trim().split("-", 2);
                        String customerId = parts[0].trim();
                        customer = customerService.findById(customerId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng: " + customerId));
                    } catch (Exception e) {
                        System.err.println("Lỗi khi xử lý thông tin khách hàng: " + e.getMessage());
                        customer = null; // Reset customer nếu có lỗi
                    }
                } else if (line.startsWith("Nhan vien:")) {
                    try {
                        String[] parts = line.substring("Nhan vien:".length()).trim().split("-", 2);
                        String employeeId = parts[0].trim();
                        employee = employeeService.findById(employeeId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + employeeId));
                    } catch (Exception e) {
                        System.err.println("Lỗi khi xử lý thông tin nhân viên: " + e.getMessage());
                        employee = null; // Reset employee nếu có lỗi
                    }
                } else if (line.startsWith("-") && line.contains("(Ma:") && line.contains("x")) {
                    try {
                        // Parse chi tiết sản phẩm
                        String productInfo = line.substring(1).trim();
                        int maIndex = productInfo.indexOf("(Ma:");
                        int closeParenIndex = productInfo.indexOf(")", maIndex);
                        int xIndex = productInfo.indexOf("x", closeParenIndex);
                        
                        // Lấy tên sản phẩm
                       // String productName = productInfo.substring(0, maIndex).trim();
                        // Lấy mã sản phẩm
                        String productId = productInfo.substring(maIndex + 4, closeParenIndex).trim();
                        // Lấy số lượng
                        String quantityStr = productInfo.substring(xIndex + 1, productInfo.indexOf(":")).trim();
                        int quantity = Integer.parseInt(quantityStr);
                        
                        // Tìm sản phẩm trong service
                        Product product = productService.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + productId));
                        
                        if (currentInvoice != null) {
                            items.add(new Invoice.InvoiceDetail(product, quantity));
                        }
                    } catch (Exception e) {
                        System.err.println("Lỗi khi xử lý chi tiết sản phẩm: " + e.getMessage());
                    }
                }
            }
            
            // Xử lý hóa đơn cuối cùng
            if (currentInvoice != null && customer != null && employee != null) {
                currentInvoice.setCustomer(customer);
                currentInvoice.setEmployee(employee);
                currentInvoice.setItems(new ArrayList<>(items));
                invoices.add(currentInvoice);
            }
            
        } catch (Exception e) {
            System.err.println("Lỗi khi đọc file hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
        return invoices;
    }
} 