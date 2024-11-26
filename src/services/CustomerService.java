package services;

import models.Customer;
//import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.charset.StandardCharsets;
//import java.util.Scanner;
import java.util.stream.Collectors;

public class CustomerService extends BaseService<Customer> {
    public CustomerService() {
        super("customers.txt");
        fixCustomerData();
    }
    
    @Override
    protected void loadItems() {
        items = fileHandler.loadFromFile(filename);
        if (items == null) {
            items = new ArrayList<>();
        }
    }

    @Override
    public Optional<Customer> findById(String id) {
        loadItems();
        return items.stream()
            .filter(c -> c.getId().equals(id))
            .findFirst();
    }

    @Override
    protected void validateInput(String id, String name) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma khach hang khong duoc de trong!");
        }
        if (findById(id).isPresent()) {
            throw new IllegalArgumentException("Ma khach hang da ton tai!");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten khach hang khong duoc de trong!");
        }
    }

    public void searchItems() {
        String keyword = getStringInput("Nhap tu khoa tim kiem: ");
        loadItems(); // Đảm bảo load dữ liệu mới nhất
        
        List<Customer> results = items.stream()
            .filter(c -> c.getId().toLowerCase().contains(keyword.toLowerCase()) ||
                        c.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        c.getPhone().contains(keyword) ||
                        c.getAddress().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());
        
        if (results.isEmpty()) {
            System.out.println("Khong tim thay khach hang nao!");
            return;
        }

        System.out.println("\nKet qua tim kiem:");
        results.forEach(Customer::display);
    }

    public List<Customer> findByName(String name) {
        loadItems();
        String searchName = name.toLowerCase().trim();
        return items.stream()
            .filter(c -> c.getName().toLowerCase().contains(searchName))
            .collect(Collectors.toList());
    }

    public void updateCustomerDetails(String id, String name, String address, String phone) {
        Optional<Customer> customerOpt = findById(id);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Khong tim thay khach hang!");
        }

        Customer customer = customerOpt.get();

        // Cập nhật từng trường thông tin nếu có
        if (name != null && !name.trim().isEmpty()) {
            customer.setName(name);
        }
        if (address != null && !address.trim().isEmpty()) {
            customer.setAddress(address);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            if (!phone.matches("\\d{10,11}")) {
                throw new IllegalArgumentException("So dien thoai khong hop le (can 10-11 so)!");
            }
            customer.setPhone(phone);
        }

        updateCustomer(customer);
    }

    public void updateCustomer(Customer customer) {
        if (customer == null) return;
        
        loadItems(); // Load lại danh sách trước khi cập nhật
        
        // Tìm và cập nhật khách hàng trong danh sách
        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(customer.getId())) {
                items.set(i, customer);
                found = true;
                break;
            }
        }
        
        if (!found) {
            throw new IllegalArgumentException("Khong tim thay khach hang!");
        }
        
        // Đánh dấu là đang cập nhật toàn bộ file
        fileHandler.setUpdatingFile(true);
        fileHandler.saveToFile(filename, items);
        fileHandler.setUpdatingFile(false);
        
        System.out.println("Cap nhat khach hang thanh cong!");
    }

    public void deleteCustomer(String id) {
        Optional<Customer> customerOpt = findById(id);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Khong tim thay khach hang!");
        }

        // Load lại danh sách mới nhất
        loadItems();
        
        // Xóa khách hàng khỏi danh sách
        boolean removed = items.removeIf(c -> c.getId().equals(id));
        
        if (!removed) {
            throw new IllegalArgumentException("Xoa khach hang that bai!");
        }
        
        try {
            // Ghi đè toàn bộ file với danh sách mới
            fileHandler.setUpdatingFile(true);
            fileHandler.saveToFile(filename, items);
            fileHandler.setUpdatingFile(false);
            
            System.out.println("Xoa khach hang thanh cong!");
        } catch (Exception e) {
            System.err.println("Loi khi xoa khach hang: " + e.getMessage());
            throw new RuntimeException("Xoa khach hang that bai!", e);
        }
    }

    public void addCustomer(Customer customer) {
        validateInput(customer.getId(), customer.getName());
        
        if (customer.getPhone() != null && !customer.getPhone().isEmpty() 
            && !customer.getPhone().matches("\\d{10,11}")) {
            throw new IllegalArgumentException("So dien thoai khong hop le (can 10-11 so)!");
        }
        
        loadItems();
        items.add(customer);
        fileHandler.saveToFile(filename, List.of(customer));
        System.out.println("Them khach hang thanh cong!");
    }

    public List<Customer> getAllItems() {
        loadItems();
        return new ArrayList<>(items);
    }

    public void fixCustomerData() {
        loadItems();
        boolean needsUpdate = false;
        
        for (Customer customer : items) {
            String phone = customer.getPhone();
            String address = customer.getAddress();
            
            // Nếu phone chứa chữ và address chứa số, đổi chỗ cho nhau
            if (phone != null && address != null && 
                phone.matches(".*[a-zA-Z].*") && address.matches("\\d+")) {
                customer.setPhone(address);
                customer.setAddress(phone);
                needsUpdate = true;
            }
        }
        
        if (needsUpdate) {
            // Cập nhật lại file
            fileHandler.setUpdatingFile(true);
            fileHandler.saveToFile(filename, items);
            fileHandler.setUpdatingFile(false);
            System.out.println("Da sua lai thong tin khach hang!");
        }
    }
} 