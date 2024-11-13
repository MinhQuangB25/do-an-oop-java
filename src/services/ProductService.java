package services;

import models.Product;
import models.Computer;
import models.Accessory;
import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

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
        saveProducts();
        System.out.println("Thêm sản phẩm thành công!");
    }

    public void updateProduct(Product product) {
        if (product == null) return;
        
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(product.getId())) {
                products.set(i, product);
                saveProducts();
                break;
            }
        }
    }

    public void deleteProduct(String id) {
        if (id == null || id.trim().isEmpty()) {
            System.out.println("Mã sản phẩm không hợp lệ!");
            return;
        }

        // Kiểm tra sản phẩm tồn tại
        Optional<Product> productToDelete = findById(id.trim());
        if (productToDelete.isEmpty()) {
            System.out.println("Không tìm thấy sản phẩm!");
            return;
        }

        // Xóa sản phẩm khỏi danh sách
        products.removeIf(p -> p.getId().equals(id.trim()));
        
        try {
            // Lưu lại danh sách vào file .dat
            fileHandler.saveToFile(FILENAME, products);
            
            // Tạo lại nội dung cho file .txt
            String txtFilename = FILENAME.replace(".dat", ".txt");
            List<String> newLines = new ArrayList<>();
            newLines.add("===== DANH SACH PRODUCTS =====");
            
            if (!products.isEmpty()) {
                for (Product product : products) {
                    newLines.add("----------------------------------------");
                    newLines.add(product.getInfo());
                }
                newLines.add("----------------------------------------");
            }

            // Ghi đè file .txt
            Path txtPath = Paths.get(fileHandler.getDirectory() + txtFilename);
            Files.write(txtPath, newLines, StandardCharsets.UTF_8);
            
            System.out.println("Xóa sản phẩm thành công!");
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu file: " + e.getMessage());
        }
    }

    public Optional<Product> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return products.stream()
                .filter(p -> p.getId().equals(id.trim()))
                .findFirst();
    }

    public List<Product> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase().trim()))
                .toList();
    }

    public List<Computer> getAllComputers() {
        return products.stream()
                .filter(p -> p instanceof Computer)
                .map(p -> (Computer) p)
                .toList();
    }

    public List<Accessory> getAllAccessories() {
        return products.stream()
                .filter(p -> p instanceof Accessory)
                .map(p -> (Accessory) p)
                .toList();
    }

    public boolean checkStock(String productId, int quantity) {
        Optional<Product> productOpt = findById(productId);
        if (productOpt.isEmpty()) {
            return false;
        }
        Product product = productOpt.get();
        return product.getQuantity() >= quantity && product.getQuantity() > 0;
    }

    private void saveProducts() {
        if (products != null) {
            try {
                // Lưu file .dat và txt
                fileHandler.saveToFile(FILENAME, products);
            } catch (Exception e) {
                System.err.println("Loi khi luu file: " + e.getMessage());
            }
        }
    }

    private void loadProducts() {
        products = fileHandler.loadFromFile(FILENAME);
        if (products == null) {
            products = new ArrayList<>();
        }
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

    // Thêm phương thức kiểm tra sản phẩm có trong hóa đơn
    public boolean isProductInUse(String productId) {
        // TODO: Implement check if product is used in any invoice
        return false;
    }

    // Thêm phương thức kiểm tra và cập nhật số lượng
    public boolean updateProductQuantity(String productId, int quantity) {
        Optional<Product> productOpt = findById(productId);
        if (productOpt.isEmpty()) {
            return false;
        }
        
        Product product = productOpt.get();
        int currentQuantity = product.getQuantity();
        int newQuantity = currentQuantity - quantity;
        
        if (newQuantity < 0) {
            return false;
        }
        
        product.setQuantity(newQuantity);
        saveProducts();
        return true;
    }
} 