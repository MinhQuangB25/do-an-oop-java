package services;

import models.Employee;
//import models.Invoice;
//import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
//import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
//import java.text.SimpleDateFormat;

public class EmployeeService extends BaseService<Employee> {
    private InvoiceService invoiceService;

    public EmployeeService() {
        super("employees.txt");
    }

    public void setInvoiceService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Override
    protected void loadItems() {
        items = fileHandler.loadFromFile(filename);
        if (items == null) {
            items = new ArrayList<>();
        }
    }

    @Override
    public Optional<Employee> findById(String id) {
        loadItems();
        return items.stream()
            .filter(e -> e.getId().equals(id))
            .findFirst();
    }

    @Override
    protected void validateInput(String id, String name) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Ma nhan vien khong duoc de trong!");
        }
        if (findById(id).isPresent()) {
            throw new IllegalArgumentException("Ma nhan vien da ton tai!");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten nhan vien khong duoc de trong!");
        }
    }

    public void addEmployee(String id, String name, String address, String phone, String position) {
        validateInput(id, name);
        
        // Kiểm tra số điện thoại
        if (phone != null && !phone.isEmpty() && !phone.matches("\\d{10,11}")) {
            throw new IllegalArgumentException("So dien thoai khong hop le (can 10-11 so)!");
        }
        
        // Nhập và kiểm tra lương cơ bản
        double basicSalary = getDoubleInput("Nhap luong co ban: ");
        if (basicSalary < 0) {
            throw new IllegalArgumentException("Luong co ban khong duoc am!");
        }
        
        Employee employee = new Employee(id, name, phone, address, position, basicSalary);
        loadItems(); // Load danh sách hiện tại
        items.add(employee);
        
        // Chỉ lưu nhân viên mới vào cuối file
        fileHandler.setUpdatingFile(false); // Đảm bảo chế độ append
        fileHandler.saveToFile(filename, List.of(employee));
        
        System.out.println("Them nhan vien thanh cong!");
    }

    // Sửa lỗi visibility của getDoubleInput và đóng scanner
    protected double getDoubleInput(String prompt) {
        // Sử dụng scanner từ BaseService thay vì tạo mới
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so hop le!");
            }
        }
    }

    public void displayEmployeesFromFile() {
        displayFromFile();
    }

    public void searchEmployees() {
        loadItems();
        String keyword = getStringInput("Nhap tu khoa tim kiem: ");
        
        if (items == null) {
            items = new ArrayList<>();
        }
        
        List<Employee> results = items.stream()
            .filter(e -> e.getName().toLowerCase().contains(keyword.toLowerCase()))
            .collect(Collectors.toList());

        if (results.isEmpty()) {
            System.out.println("Khong tim thay nhan vien nao!");
            return;
        }

        System.out.println("\nKet qua tim kiem:");
        results.forEach(Employee::display);
    }

    public void updateEmployeeDetails(String id, String name, String address, String phone, String position) {
        Optional<Employee> employeeOpt = findById(id);
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Khong tim thay nhan vien!");
        }

        Employee employee = employeeOpt.get();
        // Lưu lại basic salary hiện tại
        double currentBasicSalary = employee.getBasicSalary();

        // Cập nhật thông tin mới
        if (name != null && !name.trim().isEmpty()) {
            employee.setName(name);
        }
        if (address != null && !address.trim().isEmpty()) {
            employee.setAddress(address);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            employee.setPhone(phone);
        }
        if (position != null && !position.trim().isEmpty()) {
            employee.setPosition(position);
        }
        // Giữ nguyên basic salary
        employee.setBasicSalary(currentBasicSalary);

        try {
            List<String> lines = fileHandler.readAllLines(filename);
            List<String> newLines = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                if (line.contains("ID: " + id)) {
                    // Thay thế dòng cũ bằng thông tin mới của nhân viên
                    newLines.add(employee.getInfo());
                    found = true;
                } else {
                    newLines.add(line);
                }
            }

            if (!found) {
                throw new IllegalArgumentException("Khong tim thay nhan vien trong file!");
            }

            // Ghi lại file với nội dung đã cập nhật
            Files.write(Paths.get(fileHandler.getDirectory() + filename), newLines);
            
            // Cập nhật trong danh sách items
            loadItems();
            
            System.out.println("Cap nhat nhan vien thanh cong!");
        } catch (IOException e) {
            System.err.println("Loi khi cap nhat nhan vien: " + e.getMessage());
            throw new RuntimeException("Cap nhat nhan vien that bai!", e);
        }
    }

    public void deleteEmployeeById(String id) {
        Optional<Employee> employeeOpt = findById(id);
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Khong tim thay nhan vien!");
        }

        try {
            List<String> lines = fileHandler.readAllLines(filename);
            List<String> newLines = new ArrayList<>();
            boolean skipEmployee = false;

            for (String line : lines) {
                // Nếu là header hoặc dòng phân cách, thêm vào bình thường
                if (line.startsWith("=====") || line.isEmpty()) {
                    newLines.add(line);
                    continue;
                }
                
                // Nếu là dòng phân cách và đang skip nhân viên, bỏ qua
                if (line.startsWith("----")) {
                    if (!skipEmployee) {
                        newLines.add(line);
                    }
                    skipEmployee = false;
                    continue;
                }

                // Nếu tìm thấy nhân viên cần xóa, bắt đầu skip
                if (line.contains("ID: " + id)) {
                    skipEmployee = true;
                    continue;
                }

                // Nếu không phải dòng cần skip, thêm vào danh sách mới
                if (!skipEmployee) {
                    newLines.add(line);
                }
            }

            // Ghi lại file với nội dung mới
            Files.write(Paths.get(fileHandler.getDirectory() + filename), newLines);
            
            // Cập nhật danh sách items
            loadItems();
            
            System.out.println("Xoa nhan vien thanh cong!");
        } catch (IOException e) {
            System.err.println("Loi khi xoa nhan vien: " + e.getMessage());
            throw new RuntimeException("Xoa nhan vien that bai!", e);
        }
    }

    // Thêm phương thức sử dụng invoiceService để tránh warning unused field
    public void viewEmployeePerformance() {
        System.out.println("\n=== THONG KE HIEU SUAT NHAN VIEN ===");
        loadItems();
        
        if (invoiceService == null) {
            System.out.println("Chua khoi tao InvoiceService!");
            return;
        }

        try {
            // Đọc trực tiếp từ file invoices.txt
            List<String> invoiceLines = fileHandler.readAllLines("invoices.txt");
            
            for (Employee employee : items) {
                System.out.printf("\nNhan vien: %s (%s)\n", employee.getName(), employee.getId());
                
                int invoiceCount = 0;
                double totalSales = 0.0;
                boolean isCurrentInvoice = false;
                String currentEmployeeId = "";
                double currentInvoiceTotal = 0.0;

                for (String line : invoiceLines) {
                    line = line.trim();
                    
                    // Bắt đầu hóa đơn mới
                    if (line.startsWith("Invoice [ID:")) {
                        isCurrentInvoice = true;
                        currentInvoiceTotal = 0.0;
                        continue;
                    }
                    
                    // Kiểm tra nhân viên
                    if (line.startsWith("Nhan vien:")) {
                        String employeeInfo = line.substring("Nhan vien:".length()).trim();
                        currentEmployeeId = employeeInfo.split("-")[0].trim();
                        continue;
                    }
                    
                    // Lấy tổng tiền
                    if (line.startsWith("Tong tien:")) {
                        String totalStr = line.substring("Tong tien:".length())
                                            .replace("VND", "")
                                            .replace(",", "")
                                            .trim();
                        try {
                            currentInvoiceTotal = Double.parseDouble(totalStr);
                            
                            // Nếu là hóa đơn của nhân viên hiện tại
                            if (isCurrentInvoice && currentEmployeeId.equals(employee.getId())) {
                                invoiceCount++;
                                totalSales += currentInvoiceTotal;
                            }
                            
                            isCurrentInvoice = false;
                        } catch (NumberFormatException e) {
                            System.err.println("Loi parse so tien: " + totalStr);
                        }
                    }
                }
                
                System.out.printf("- So luong hoa don: %d\n", invoiceCount);
                System.out.printf("- Tong doanh so: %,.0f VND\n", totalSales);
            }
            
        } catch (IOException e) {
            System.err.println("Loi doc file hoa don: " + e.getMessage());
        }
    }

    // ... rest of specific methods ...
} 