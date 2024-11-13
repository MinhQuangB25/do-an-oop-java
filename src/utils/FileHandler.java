package utils;

import models.*;
import interfaces.SystemInterfaces;
import interfaces.SystemInterfaces.Printable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class FileHandler<T> {
    private String directory = "data/";

    public String getDirectory() {
        return directory;
    }

    public void saveToFile(String filename, List<T> data) {
        try {
            Path path = Paths.get(directory + filename);
            List<String> lines = new ArrayList<>();
            
            if (!data.isEmpty()) {
                // Header chung cho tất cả các loại
                String className = data.get(0).getClass().getSimpleName().toUpperCase();
                if (className.equals("COMPUTER") || className.equals("ACCESSORY")) {
                    className = "PRODUCT";
                }
                lines.add("===== DANH SACH " + className + " =====");
                
                // Phân loại sản phẩm nếu là Product
                if (data.get(0) instanceof Product) {
                    List<Product> computers = data.stream()
                        .map(item -> (Product) item)
                        .filter(p -> p instanceof Computer)
                        .collect(Collectors.toList());
                    
                    List<Product> accessories = data.stream()
                        .map(item -> (Product) item)
                        .filter(p -> p instanceof Accessory)
                        .collect(Collectors.toList());

                    if (!computers.isEmpty()) {
                        lines.add("\n===== COMPUTERS =====");
                        for (Product computer : computers) {
                            lines.add("----------------------------------------");
                            lines.add(computer.getInfo());
                        }
                    }
                    
                    if (!accessories.isEmpty()) {
                        lines.add("\n===== ACCESSORIES =====");
                        for (Product accessory : accessories) {
                            lines.add("----------------------------------------");
                            lines.add(accessory.getInfo());
                        }
                    }
                } else {
                    // Xử lý cho các loại dữ liệu khác
                    for (T item : data) {
                        lines.add("----------------------------------------");
                        if (item instanceof Printable) {
                            lines.add(((Printable) item).getInfo());
                        }
                    }
                }
                lines.add("----------------------------------------");
            }
            
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Loi khi luu file: " + e.getMessage());
        }
    }

    public List<T> loadFromFile(String filename) {
        List<T> data = new ArrayList<>();
        Path path = Paths.get(directory + filename);
        
        try {
            if (!Files.exists(path)) {
                return data;
            }

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("=====") || line.startsWith("----")) {
                    continue;
                }

                T item = parseLine(line);
                if (item != null) {
                    data.add(item);
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
        }

        return data;
    }

    @SuppressWarnings("unchecked")
    private T parseLine(String line) {
        try {
            if (line.contains("[") && line.contains("]")) {
                String content = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                String[] parts = content.split(", ");
                Map<String, String> data = new HashMap<>();
                
                for (String part : parts) {
                    String[] keyValue = part.split(": ", 2);
                    if (keyValue.length == 2) {
                        data.put(keyValue[0], keyValue[1]);
                    }
                }

                if (line.startsWith("Customer")) {
                    return (T) new Customer(
                        data.get("ID"),
                        data.get("Name"),
                        data.get("Address"),
                        data.get("Phone")
                    );
                } else if (line.startsWith("Employee")) {
                    return (T) new Employee(
                        data.get("ID"),
                        data.get("Name"),
                        data.get("Position"),
                        Double.parseDouble(data.get("Basic Salary"))
                    );
                } else if (line.startsWith("Computer")) {
                    return (T) new Computer(
                        data.get("ID"),
                        data.get("Name"),
                        Double.parseDouble(data.get("Price").replace(",", "")),
                        Integer.parseInt(data.get("Quantity")),
                        data.get("CPU"),
                        data.get("RAM"),
                        data.get("Hard Drive")
                    );
                } else if (line.startsWith("Accessory")) {
                    return (T) new Accessory(
                        data.get("ID"),
                        data.get("Name"),
                        Double.parseDouble(data.get("Price").replace(",", "")),
                        Integer.parseInt(data.get("Quantity")),
                        data.get("Type")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi parse dòng: " + line);
            e.printStackTrace();
        }
        return null;
    }

    public void readTextFile(String filename) {
        try {
            Path path = Paths.get(directory + filename);
            if (Files.exists(path)) {
                Files.readAllLines(path, StandardCharsets.UTF_8)
                    .forEach(System.out::println);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
        }
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
                lines.add(String.format("- %s x%d: %,.0fđ", 
                    detail.getProduct().getName(),
                    detail.getQuantity(),
                    detail.getProduct().getPrice() * detail.getQuantity()));
            }
            
            lines.add(String.format("Tong tien: %,.0fđ", invoice.getTotalAmount()));
            lines.add("----------------------------------------");
            
            // Ghi toàn bộ nội dung vào file
            Files.write(path, lines, StandardCharsets.UTF_8);
                
        } catch (IOException e) {
            System.err.println("Loi khi luu hoa don: " + e.getMessage());
        }
    }

    public void writeFormattedText(String filename, String header, List<String> contentLines) {
        try {
            Path path = Paths.get(directory + filename);
            List<String> lines = new ArrayList<>();
            
            lines.add(header);
            for (String content : contentLines) {
                lines.add("----------------------------------------");
                lines.add(content);
            }
            lines.add("----------------------------------------");
            
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Loi khi ghi file: " + e.getMessage());
        }
    }
} 