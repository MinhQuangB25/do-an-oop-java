package services;

import models.Customer;
import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class CustomerService {
    private FileHandler<Customer> fileHandler;
    private static final String FILENAME = "customers.txt";

    public CustomerService() {
        this.fileHandler = new FileHandler<>();
    }

    public void addCustomer(Customer customer) {
        List<Customer> customers = getAllCustomers();
        if (customers == null) {
            customers = new ArrayList<>();
        }
        
        if (findById(customer.getId()).isPresent()) {
            throw new IllegalArgumentException("Ma khach hang da ton tai!");
        }
        
        customers.add(customer);
        
        fileHandler.setUpdatingFile(false);
        
        List<Customer> newCustomer = new ArrayList<>();
        newCustomer.add(customer);
        fileHandler.saveToFile(FILENAME, newCustomer);
    }

    public void updateCustomer(Customer customer) {
        try {
            List<String> lines = fileHandler.readAllLines(FILENAME);
            List<String> updatedLines = new ArrayList<>();
            boolean found = false;
            
            // Giữ lại header
            if (!lines.isEmpty()) {
                updatedLines.add(lines.get(0)); // Thêm dòng header
            }
            
            for (String line : lines) {
                if (line.startsWith("=====") || line.startsWith("----")) {
                    // Giữ lại các dòng phân cách
                    updatedLines.add(line);
                    continue;
                }
                
                if (line.contains("Customer [")) {
                    String customerInfo = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                    String[] parts = customerInfo.split(",");
                    String currentId = "";
                    
                    // Tìm ID của khách hàng trong dòng hiện tại
                    for (String part : parts) {
                        if (part.trim().startsWith("ID:")) {
                            currentId = part.substring(part.indexOf(":") + 1).trim();
                            break;
                        }
                    }
                    
                    // Nếu tìm thấy khách hàng cần cập nhật
                    if (currentId.equals(customer.getId())) {
                        found = true;
                        // Tạo dòng mới với thông tin đã cập nhật
                        String updatedLine = String.format("Customer [ID: %s, Name: %s, Address: %s, Phone: %s, Rank: %s]",
                            customer.getId(),
                            customer.getName(),
                            customer.getAddress(),
                            customer.getPhone());
                        updatedLines.add(updatedLine);
                    } else {
                        // Giữ nguyên dòng cũ nếu không phải khách hàng cần cập nhật
                        updatedLines.add(line);
                    }
                }
            }
            
            if (!found) {
                throw new IllegalArgumentException("Không tìm thấy khách hàng cần cập nhật!");
            }
            
            // Ghi lại toàn bộ file với nội dung đã cập nhật
            Path path = Paths.get(fileHandler.getDirectory() + FILENAME);
            Files.write(path, updatedLines, StandardCharsets.UTF_8);
            
        } catch (IOException e) {
            System.err.println("Lỗi khi cập nhật thông tin khách hàng: " + e.getMessage());
        }
    }

    public void deleteCustomer(String id) {
        List<Customer> customers = getAllCustomers();
        customers.removeIf(c -> c.getId().equals(id));
        fileHandler.setUpdatingFile(true);
        fileHandler.saveToFile(FILENAME, customers);
        fileHandler.setUpdatingFile(false);
    }

    public Optional<Customer> findById(String id) {
        try {
            List<String> lines = fileHandler.readAllLines(FILENAME);
            for (String line : lines) {
                if (line.contains("Customer [")) {
                    String customerInfo = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                    String[] parts = customerInfo.split(",");
                    
                    String customerId = "";
                    String customerName = "";
                    String address = "";
                    String phone = "";
                    String rank = "Normal";
                    
                    for (String part : parts) {
                        part = part.trim();
                        if (part.startsWith("ID:")) {
                            customerId = part.substring(part.indexOf(":") + 1).trim();
                        } else if (part.startsWith("Name:")) {
                            customerName = part.substring(part.indexOf(":") + 1).trim();
                        } else if (part.startsWith("Address:")) {
                            address = part.substring(part.indexOf(":") + 1).trim();
                        } else if (part.startsWith("Phone:")) {
                            phone = part.substring(part.indexOf(":") + 1).trim();
                        } else if (part.startsWith("Rank:")) {
                            rank = part.substring(part.indexOf(":") + 1).trim();
                        }
                    }
                    
                    if (customerId.equals(id)) {
                        Customer customer = new Customer(customerId, customerName, address, phone);
                        return Optional.of(customer);
                    }
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            System.err.println("Loi khi doc file khach hang: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Customer> findByName(String name) {
        List<Customer> results = new ArrayList<>();
        
        if (name == null || name.trim().isEmpty()) {
            return results;
        }

        String searchName = name.trim().toLowerCase();
        
        try {
            List<String> lines = fileHandler.readAllLines(FILENAME);
            for (String line : lines) {
                // Bỏ qua các dòng header và phân cách
                if (line.startsWith("=====") || line.startsWith("----") || line.trim().isEmpty()) {
                    continue;
                }
                
                if (line.contains("Customer [")) {
                    // Format: Customer [ID: KH001, Name: Nguyen Van A, Address: Ha Noi, Phone: 0123456789, Rank: Normal]
                    String customerInfo = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                    String[] parts = customerInfo.split(",");
                    
                    String id = "";
                    String customerName = "";
                    String address = "";
                    String phone = "";
                    String rank = "Normal";
                    
                    for (String part : parts) {
                        part = part.trim();
                        if (part.startsWith("ID:")) {
                            id = part.substring(part.indexOf(":") + 1).trim();
                        } else if (part.startsWith("Name:")) {
                            customerName = part.substring(part.indexOf(":") + 1).trim();
                        } else if (part.startsWith("Address:")) {
                            address = part.substring(part.indexOf(":") + 1).trim();
                        } else if (part.startsWith("Phone:")) {
                            phone = part.substring(part.indexOf(":") + 1).trim();
                        } else if (part.startsWith("Rank:")) {
                            rank = part.substring(part.indexOf(":") + 1).trim();
                        }
                    }
                    
                    // Kiểm tra nếu tên khách hàng chứa từ khóa tìm kiếm
                    if (!customerName.isEmpty() && 
                        customerName.toLowerCase().contains(searchName)) {
                        Customer customer = new Customer(id, customerName, address, phone);
                        results.add(customer);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file khách hàng: " + e.getMessage());
        }
        
        return results;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = fileHandler.loadFromFile(FILENAME);
        if (customers == null) {
            customers = new ArrayList<>();
        }
        return customers;
    }

    public void displayCustomersFromFile() {
        fileHandler.readTextFile(FILENAME);
    }
} 