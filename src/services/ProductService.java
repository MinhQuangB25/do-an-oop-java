package services;

import models.Product;
import models.Computer;
import models.Accessory;
//import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProductService extends BaseService<Product> {
    private static final String FILENAME = "products.txt";
    private List<Product> products;

    public ProductService() {
        super(FILENAME);
        products = new ArrayList<>();
        loadItems();
    }

    @Override
    protected void loadItems() {
        try {
            List<String> lines = fileHandler.readAllLines(filename);
            products = new ArrayList<>();
            items = new ArrayList<>();
            
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("===") || line.startsWith("---")) {
                    continue;
                }
                
                Product product = null;
                if (line.contains("Computer [")) {
                    product = parseComputer(line);
                } else if (line.contains("Accessory [")) {
                    product = parseAccessory(line);
                }
                
                if (product != null) {
                    products.add(product);
                    items.add(product);
                }
            }
        } catch (IOException e) {
            System.err.println("Loi khi doc file san pham: " + e.getMessage());
            products = new ArrayList<>();
            items = new ArrayList<>();
        }
    }

    @Override
    public Optional<Product> findById(String id) {
        loadItems();
        return products.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst();
    }

    @Override
    protected void validateInput(String id, String name) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma san pham khong duoc de trong!");
        }
        if (findById(id).isPresent()) {
            throw new IllegalArgumentException("Ma san pham da ton tai!");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten san pham khong duoc de trong!");
        }
    }

    public void addProduct(Product product) {
        if (findById(product.getId()).isPresent()) {
            System.out.println("Sản phẩm với mã " + product.getId() + " đã tồn tại!");
            return;
        }
        
        products.add(product);
        items = new ArrayList<>(products);
        fileHandler.saveToFile(FILENAME, List.of(product));
        System.out.println("Thêm sản phẩm thành công!");
    }

    public void updateProduct(Product product) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(product.getId())) {
                products.set(i, product);
                break;
            }
        }
        items = new ArrayList<>(products);

        fileHandler.setUpdatingFile(true);
        fileHandler.saveToFile(FILENAME, products);
        fileHandler.setUpdatingFile(false);
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
        
        try {
            List<String> lines = fileHandler.readAllLines(FILENAME);
            List<String> newLines = new ArrayList<>();
            
            for (String line : lines) {
                if (line.contains("ID: " + productId)) {
                    String newQuantity = String.valueOf(currentQuantity - quantity);
                    String modifiedLine = line.replaceFirst("Quantity: \\d+", "Quantity: " + newQuantity);
                    newLines.add(modifiedLine);
                } else {
                    newLines.add(line);
                }
            }
            
            fileHandler.setUpdatingFile(true);
            Files.write(Path.of(fileHandler.getDirectory() + FILENAME), newLines);
            fileHandler.setUpdatingFile(false);
            
            product.setQuantity(currentQuantity - quantity);
            
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId().equals(productId)) {
                    products.get(i).setQuantity(currentQuantity - quantity);
                    break;
                }
            }
            
            System.out.println("Cập nhật số lượng sản phẩm thành công!");
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi khi cập nhật số lượng: " + e.getMessage());
            return false;
        }
    }

    private Computer parseComputer(String line) {
        try {
            String content = line.substring(line.indexOf("[") + 1, line.lastIndexOf("]"));
            String[] parts = content.split(", ");
            
            String id = parts[0].split(": ")[1];
            String name = parts[1].split(": ")[1];
            double price = Double.parseDouble(parts[2].split(": ")[1].replace(",", ""));
            int quantity = Integer.parseInt(parts[3].split(": ")[1]);
            
            String cpu = parts[4].split(": ")[1];
            String ram = parts[5].split(": ")[1];
            String hardDrive = parts[6].split(": ")[1].replace("GB", "").trim();
            
            return new Computer(id, name, price, quantity, cpu, ram, hardDrive);
        } catch (Exception e) {
            System.err.println("Lỗi khi parse Computer: " + e.getMessage());
            return null;
        }
    }

    private Accessory parseAccessory(String line) {
        try {
            String content = line.substring(line.indexOf("[") + 1, line.lastIndexOf("]"));
            String[] parts = content.split(",");
            
            String id = getValue(parts, 0);
            String name = getValue(parts, 1);
            String priceStr = getValue(parts, 2);
            double price = Double.parseDouble(priceStr.replaceAll("[^0-9.]", ""));
            int quantity = Integer.parseInt(getValue(parts, 3));
            String type = getValue(parts, 4);
            
            return new Accessory(id, name, price, quantity, type);
        } catch (Exception e) {
            return null;
        }
    }

    private String getValue(String[] parts, int index) {
        if (index < parts.length) {
            String part = parts[index].trim();
            int colonIndex = part.indexOf(":");
            if (colonIndex != -1) {
                return part.substring(colonIndex + 1).trim();
            }
        }
        return "";
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
        loadItems();
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

    public void addComputer(String id, String name, double price, int quantity, 
                           String cpu, String ram, String hardDrive) {
        validateInput(id, name);
        
        if (!ram.toUpperCase().endsWith("GB")) {
            ram = ram + "GB";
        }
        if (!hardDrive.toUpperCase().endsWith("GB") && !hardDrive.toUpperCase().endsWith("TB")) {
            hardDrive = hardDrive + "GB";
        }
        
        Computer computer = new Computer(id, name, price, quantity, cpu, ram, hardDrive);
        addProduct(computer);
    }

    public void addAccessory(String id, String name, double price, int quantity, String type) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma san pham khong duoc de trong!");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten san pham khong duoc de trong!");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Gia san pham phai lon hon 0!");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("So luong khong duoc am!");
        }

        Accessory accessory = new Accessory(id, name, price, quantity, type);
        addProduct(accessory);
    }

    public void updateProductDetails(String id, String name, Double price, Integer quantity) {
        Optional<Product> productOpt = findById(id);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Khong tim thay san pham!");
        }

        Product product = productOpt.get();
        
        if (name != null && !name.trim().isEmpty()) {
            product.setName(name);
        }
        if (price != null && price > 0) {
            product.setPrice(price);
        }
        if (quantity != null && quantity >= 0) {
            product.setQuantity(quantity);
        }

        updateProduct(product);
    }

    public void deleteProductById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma san pham khong duoc de trong!");
        }

        Optional<Product> product = findById(id);
        if (product.isEmpty()) {
            throw new IllegalArgumentException("Khong tim thay san pham!");
        }

        try {
            List<String> lines = fileHandler.readAllLines(filename);
            List<String> newLines = new ArrayList<>();
            boolean skipProduct = false;

            for (String line : lines) {
                if (line.startsWith("=====") || line.isEmpty()) {
                    newLines.add(line);
                    continue;
                }
                
                if (line.startsWith("----")) {
                    if (!skipProduct) {
                        newLines.add(line);
                    }
                    skipProduct = false;
                    continue;
                }

                if (line.contains("ID: " + id)) {
                    skipProduct = true;
                    continue;
                }

                if (!skipProduct) {
                    newLines.add(line);
                }
            }

            Files.write(Paths.get(fileHandler.getDirectory() + filename), newLines);
            
            products.removeIf(p -> p.getId().equals(id));
            items = new ArrayList<>(products);
            
            System.out.println("Xoa san pham thanh cong!");
        } catch (IOException e) {
            System.err.println("Loi khi xoa san pham: " + e.getMessage());
            throw new RuntimeException("Xoa san pham that bai!", e);
        }
    }

    public List<Product> searchProducts(String searchKeyword) {
        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Tu khoa tim kiem khong duoc de trong!");
        }

        loadItems();
        final String keyword = searchKeyword.toLowerCase().trim();
        
        List<Product> results = products.stream()
            .filter(product -> 
                product.getId().toLowerCase().contains(keyword) ||
                product.getName().toLowerCase().contains(keyword))
            .collect(Collectors.toList());

        if (!results.isEmpty()) {
            System.out.println("----------------------------------------");
            for (Product product : results) {
                System.out.printf("%s%n", product.getInfo());
                System.out.println("----------------------------------------");
            }
        } else {
            System.out.println("Khong tim thay san pham nao!");
        }
        
        return results;
    }

    

    public Map<String, Integer> getProductStatistics() {
        loadItems();
        Map<String, Integer> stats = new HashMap<>();
        
        int totalProducts = products.size();
        long computerCount = products.stream()
            .filter(p -> p instanceof Computer)
            .count();
        long accessoryCount = products.stream()
            .filter(p -> p instanceof Accessory)
            .count();
        int lowStockCount = (int) products.stream()
            .filter(p -> p.getQuantity() < 5)
            .count();
        
        stats.put("total", totalProducts);
        stats.put("computers", (int) computerCount);
        stats.put("accessories", (int) accessoryCount);
        stats.put("lowStock", lowStockCount);
        
        return stats;
    }

    public double calculateTotalInventoryValue() {
        loadItems();
        return products.stream()
            .mapToDouble(p -> p.getPrice() * p.getQuantity())
            .sum();
    }

    public List<Product> getLowStockProducts(int threshold) {
        loadItems();
        return products.stream()
            .filter(p -> p.getQuantity() <= threshold)
            .collect(Collectors.toList());
    }

    public void displayInventoryStatistics() {
        Map<String, Integer> stats = getProductStatistics();
        double totalValue = calculateTotalInventoryValue();
        List<Product> lowStockProducts = getLowStockProducts(5);

        System.out.println("\n=== THONG KE KHO HANG ===");
        System.out.println("Tong so san pham: " + stats.get("total"));
        System.out.println("- May tinh: " + stats.get("computers"));
        System.out.println("- Phu kien: " + stats.get("accessories"));
        System.out.printf("Tong gia tri ton kho: %,.0f VND%n", totalValue);
        
        if (!lowStockProducts.isEmpty()) {
            System.out.println("\nSan pham sap het hang (< 5):");
            lowStockProducts.forEach(p -> 
                System.out.printf("- %s (Con lai: %d)%n", p.getName(), p.getQuantity()));
        }
    }

    public void validateStock() {
        loadItems();
        List<Product> invalidProducts = products.stream()
            .filter(p -> p.getQuantity() < 0 || p.getPrice() <= 0)
            .collect(Collectors.toList());

        if (!invalidProducts.isEmpty()) {
            System.out.println("\nCANH BAO: Phat hien san pham co du lieu khong hop le:");
            invalidProducts.forEach(p -> {
                if (p.getQuantity() < 0) {
                    System.out.printf("- %s: So luong am (%d)%n", p.getName(), p.getQuantity());
                }
                if (p.getPrice() <= 0) {
                    System.out.printf("- %s: Gia khong hop le (%,.0f)%n", p.getName(), p.getPrice());
                }
            });
        }
    }
} 