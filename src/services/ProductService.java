package services;

import models.Product;
import models.Computer;
import models.Accessory;
import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class ProductService {
    private List<Product> products;
    private FileHandler<Product> fileHandler;
    private static final String FILENAME = "products.txt";

    public ProductService() {
        this.fileHandler = new FileHandler<>();
        this.products = new ArrayList<>();
        loadProducts();
    }

    public void addProduct(Product product) {
        if (findById(product.getId()).isPresent()) {
            System.out.println("Sản phẩm với mã " + product.getId() + " đã tồn tại!");
            return;
        }
        
        products.add(product);
        fileHandler.setUpdatingFile(true);
        fileHandler.saveToFile(FILENAME, products);
        fileHandler.setUpdatingFile(false);
        System.out.println("Thêm sản phẩm thành công!");
    }

    public void updateProduct(Product product) {
        if (product == null) return;
        
        Optional<Product> existingProduct = findById(product.getId());
        if (existingProduct.isEmpty()) {
            System.out.println("Không tìm thấy sản phẩm với mã: " + product.getId());
            return;
        }

        // Cập nhật sản phẩm trong danh sách
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(product.getId())) {
                products.set(i, product);
                break;
            }
        }

        // Lưu vào file
        fileHandler.setUpdatingFile(true);
        fileHandler.saveToFile(FILENAME, products);
        fileHandler.setUpdatingFile(false);
        System.out.println("Cập nhật sản phẩm thành công!");
    }

    public boolean updateProductQuantity(String productId, int quantity) {
        Optional<Product> productOpt = findById(productId);
        if (productOpt.isEmpty()) {
            System.out.println("Không tìm thấy sản phẩm với mã: " + productId);
            return false;
        }
        
        Product product = productOpt.get();
        int currentQuantity = product.getQuantity();
        
        if (currentQuantity < quantity) {
            System.out.println("Số lượng tồn kho không đủ!");
            return false;
        }
        
        // Cập nhật số lượng mới
        product.setQuantity(currentQuantity - quantity);
        
        // Lưu vào file
        fileHandler.setUpdatingFile(true);
        fileHandler.saveToFile(FILENAME, products);
        fileHandler.setUpdatingFile(false);
        
        System.out.println("Cập nhật số lượng sản phẩm thành công!");
        return true;
    }

    // Các phương thức khác giữ nguyên
    private void loadProducts() {
        try {
            List<String> lines = fileHandler.readAllLines(FILENAME);
            products = new ArrayList<>();
            
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("===") || line.startsWith("---")) {
                    continue;
                }
                
                if (line.contains("Computer [")) {
                    Computer computer = parseComputer(line);
                    if (computer != null) {
                        products.add(computer);
                    }
                } else if (line.contains("Accessory [")) {
                    Accessory accessory = parseAccessory(line);
                    if (accessory != null) {
                        products.add(accessory);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
        }
    }

    private Computer parseComputer(String line) {
        try {
            // Format: Computer [ID: xxx, Name: xxx, Price: xxx, Quantity: xxx, CPU: xxx, RAM: xxx, Hard Drive: xxx]
            String content = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
            String[] parts = content.split(",");
            
            String id = parts[0].substring(parts[0].indexOf(":") + 1).trim();
            String name = parts[1].substring(parts[1].indexOf(":") + 1).trim();
            double price = Double.parseDouble(parts[2].substring(parts[2].indexOf(":") + 1).trim());
            int quantity = Integer.parseInt(parts[3].substring(parts[3].indexOf(":") + 1).trim());
            String cpu = parts[4].substring(parts[4].indexOf(":") + 1).trim();
            String ram = parts[5].substring(parts[5].indexOf(":") + 1).trim();
            String hardDrive = parts[6].substring(parts[6].indexOf(":") + 1).trim();
            
            return new Computer(id, name, price, quantity, cpu, ram, hardDrive);
        } catch (Exception e) {
            System.err.println("Lỗi khi parse Computer: " + e.getMessage());
            return null;
        }
    }

    private Accessory parseAccessory(String line) {
        try {
            // Format: Accessory [ID: xxx, Name: xxx, Price: xxx, Quantity: xxx, Type: xxx]
            String content = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
            String[] parts = content.split(",");
            
            String id = parts[0].substring(parts[0].indexOf(":") + 1).trim();
            String name = parts[1].substring(parts[1].indexOf(":") + 1).trim();
            double price = Double.parseDouble(parts[2].substring(parts[2].indexOf(":") + 1).trim());
            int quantity = Integer.parseInt(parts[3].substring(parts[3].indexOf(":") + 1).trim());
            String type = parts[4].substring(parts[4].indexOf(":") + 1).trim();
            
            return new Accessory(id, name, price, quantity, type);
        } catch (Exception e) {
            System.err.println("Lỗi khi parse Accessory: " + e.getMessage());
            return null;
        }
    }

    public Optional<Product> findById(String id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public List<Product> findByName(String name) {
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void deleteProduct(String id) {
        products.removeIf(p -> p.getId().equals(id));
        fileHandler.setUpdatingFile(true);
        fileHandler.saveToFile(FILENAME, products);
        fileHandler.setUpdatingFile(false);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public void displayProductsFromFile() {
        System.out.println("\n=== DANH SACH SAN PHAM ===");
        List<Product> computers = products.stream()
                .filter(p -> p instanceof Computer)
                .collect(Collectors.toList());
        
        List<Product> accessories = products.stream()
                .filter(p -> p instanceof Accessory)
                .collect(Collectors.toList());

        if (!computers.isEmpty()) {
            System.out.println("\n===== COMPUTERS =====");
            computers.forEach(p -> {
                System.out.println("----------------------------------------");
                System.out.println(p.getInfo());
            });
        }

        if (!accessories.isEmpty()) {
            System.out.println("\n===== ACCESSORIES =====");
            accessories.forEach(p -> {
                System.out.println("----------------------------------------");
                System.out.println(p.getInfo());
            });
        }
        System.out.println("----------------------------------------");
    }
} 