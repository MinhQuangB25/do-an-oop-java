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
    private String directory = "data/";
    private boolean isUpdatingFile = false;
    private CustomerService customerService;
    private EmployeeService employeeService;
   // private ProductService productService;

    public FileHandler() {
        createDataDirectoryIfNotExists();
    }

    private void createDataDirectoryIfNotExists() {
        File dataDir = new File(directory);
        if (!dataDir.exists()) {
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
            
            // Thêm header nếu file trống hoặc không tồn tại
            if (!Files.exists(path) || Files.size(path) == 0) {
                lines.add("===== DANH SACH " + getHeaderType(filename) + " =====");
            }
            
            // Thêm dữ liệu mới
            for (T item : data) {
                lines.add("----------------------------------------");
                if (item instanceof Printable) {
                    lines.add(((Printable) item).getInfo());
                }
            }
            
            // Nếu đang cập nhật file, ghi đè toàn bộ
            if (isUpdatingFile) {
                Files.write(path, lines, StandardCharsets.UTF_8);
            } else {
                // Nếu đang thêm mới, append vào cuối file
                if (!Files.exists(path)) {
                    Files.write(path, lines, StandardCharsets.UTF_8);
                } else {
                    Files.write(path, lines, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                }
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
                lines.add("===== DANH SACH HOA DON =====");
            } else {
                // Nếu file đã tồn tại, đọc toàn bộ nội dung cũ
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            }
            
            // Thêm hóa đơn mới vào cuối
            lines.add("----------------------------------------");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            lines.add(String.format("Invoice [ID: %s, Date: %s]", 
                invoice.getId(),
                sdf.format(invoice.getDate())));
            lines.add(String.format("Khach hang: %s", 
                invoice.getCustomer().getName()));
            lines.add(String.format("Nhan vien: %s", 
                invoice.getEmployee().getName()));
            lines.add("San pham:");
            
            for (var detail : invoice.getItems()) {
                lines.add(String.format("- %s x%d: %,.0f VND", 
                    detail.getProduct().getName(),
                    detail.getQuantity(),
                    detail.getProduct().getPrice() * detail.getQuantity()));
            }
            
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
            
            Invoice currentInvoice = null;
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("===") || line.startsWith("---")) {
                    continue;
                }
                
                if (line.contains("Computer[")) {
                    T item = (T) parseComputer(line);
                    if (item != null) items.add(item);
                } else if (line.contains("Accessory[")) {
                    T item = (T) parseAccessory(line);
                    if (item != null) items.add(item);
                } else if (line.contains("Invoice [")) {
                    currentInvoice = parseInvoice(line);
                    if (currentInvoice != null) {
                        items.add((T) currentInvoice);
                    }
                } else if (currentInvoice != null) {
                    // Xử lý thông tin chi tiết của hóa đơn
                    if (line.startsWith("Khach hang:")) {
                        String customerId = line.substring("Khach hang:".length()).trim();
                        if (customerService != null) {
                            customerService.findById(customerId).ifPresent(currentInvoice::setCustomer);
                        }
                    } else if (line.startsWith("Nhan vien:")) {
                        String employeeId = line.substring("Nhan vien:".length()).trim();
                        if (employeeService != null) {
                            employeeService.findById(employeeId).ifPresent(currentInvoice::setEmployee);
                        }
                    }
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
                data.get("id"),
                data.get("name"),
                Double.parseDouble(data.get("price")),
                Integer.parseInt(data.get("quantity")),
                data.get("cpu"),
                data.get("ram"),
                data.get("hardDrive")
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
                data.get("id"),
                data.get("name"),
                Double.parseDouble(data.get("price")),
                Integer.parseInt(data.get("quantity")),
                data.get("type")
            );
        } catch (Exception e) {
            System.err.println("Lỗi khi parse Accessory: " + e.getMessage());
            return null;
        }
    }

    private Invoice parseInvoice(String line) {
        try {
            Map<String, String> data = extractData(line);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(data.get("Date"));
            return new Invoice(data.get("ID"), date);
        } catch (Exception e) {
            System.err.println("Lỗi khi parse Invoice: " + e.getMessage());
            return null;
        }
    }

    private Map<String, String> extractData(String line) {
        Map<String, String> data = new HashMap<String, String>();
        try {
            String content = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
            for (String pair : content.split(",")) {
                String[] parts = pair.split("=");
                if (parts.length == 2) {
                    data.put(parts[0].trim(), parts[1].trim());
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
        //this.productService = productService;
    }
} 