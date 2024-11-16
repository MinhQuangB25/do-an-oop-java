package services;

import models.Employee;
import models.Invoice;
import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

public class EmployeeService {
    private FileHandler<Employee> fileHandler;
    private static final String FILENAME = "employees.txt";
    private InvoiceService invoiceService;
    private List<Employee> employees;

    public EmployeeService() {
        this.fileHandler = new FileHandler<>();
        this.employees = new ArrayList<>();
    }

    public void setInvoiceService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    public void addEmployee(Employee employee) {
        loadEmployees();
        
        if (findById(employee.getId()).isPresent()) {
            System.out.println("Nhan vien voi ma " + employee.getId() + " da ton tai!");
            return;
        }
        
        // Kiểm tra và đảm bảo position không null hoặc rỗng
        String position = employee.getPosition();
        if (position == null || position.trim().isEmpty()) {
            position = "N/A"; // Giá trị mặc định nếu position trống
            employee.setPosition(position);
        }
        
        // Tao format dung cho nhan vien moi
        List<String> employeeLines = new ArrayList<>();
        String employeeInfo = String.format("Employee [ID: %s, Name: %s, Position: %s, Basic Salary: %,.0f]",
            employee.getId().trim(),
            employee.getName().trim(),
            position.trim(),
            employee.getBasicSalary());
        employeeLines.add(employeeInfo);
        
        // Them vao file
        fileHandler.setUpdatingFile(false);
        fileHandler.writeFormattedText(FILENAME, "===== DANH SACH NHAN VIEN =====", employeeLines);
        
        // Cap nhat danh sach trong bo nho
        employees.add(employee);
    }

    public void updateEmployee(Employee employee) {
        loadEmployees();
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getId().equals(employee.getId())) {
                employees.set(i, employee);
                fileHandler.setUpdatingFile(true);
                fileHandler.saveToFile(FILENAME, employees);
                fileHandler.setUpdatingFile(false);
                return;
            }
        }
    }

    public void deleteEmployee(String id) {
        loadEmployees();
        employees.removeIf(e -> e.getId().equals(id));
        fileHandler.setUpdatingFile(true);
        fileHandler.saveToFile(FILENAME, employees);
        fileHandler.setUpdatingFile(false);
    }

    public Optional<Employee> findById(String id) {
        try {
            List<String> lines = fileHandler.readAllLines(FILENAME);
            for (String line : lines) {
                // Bỏ qua các dòng header và phân cách
                if (line.startsWith("=====") || line.startsWith("----") || line.trim().isEmpty()) {
                    continue;
                }
                
                if (line.contains("Employee [")) {
                    String employeeInfo = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                    String[] parts = employeeInfo.split(",");
                    
                    String employeeId = "";
                    String name = "";
                    String position = "";
                    double basicSalary = 0.0;
                    
                    for (String part : parts) {
                        part = part.trim();
                        if (part.startsWith("ID:")) {
                            employeeId = part.substring(part.indexOf(":") + 1).trim();
                        } else if (part.startsWith("Name:")) {
                            name = part.substring(part.indexOf(":") + 1).trim();
                        } else if (part.startsWith("Position:")) {
                            position = part.substring(part.indexOf(":") + 1).trim();
                        } else if (part.startsWith("Basic Salary:")) {
                            String salaryStr = part.substring(part.indexOf(":") + 1).trim()
                                .replace(",", "")  // Xóa dấu phẩy trong số
                                .replace("VND", "").trim(); // Xóa đơn vị tiền tệ
                            basicSalary = Double.parseDouble(salaryStr);
                        }
                    }
                    
                    if (employeeId.equals(id)) {
                        Employee employee = new Employee(employeeId, name, "", "", position, basicSalary);
                        return Optional.of(employee);
                    }
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            System.err.println("Loi khi doc file nhan vien: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Employee> findByName(String name) {
        loadEmployees();
        return employees.stream()
                .filter(e -> e.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    public double calculateTotalSales(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return 0.0;
        }
        
        double totalSales = 0.0;
        try {
            // Đọc tất cả các dòng từ file hóa đơn
            List<String> lines = fileHandler.readAllLines("invoices.txt");
            boolean isCurrentInvoice = false;
            boolean foundEmployee = false;
            
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("===")) {
                    continue;
                }
                
                // Bắt đầu một hóa đơn mới
                if (line.contains("Invoice [")) {
                    isCurrentInvoice = true;
                    foundEmployee = false;
                    continue;
                }
                
                // Kiểm tra nhân viên trong hóa đơn
                if (isCurrentInvoice && line.startsWith("Nhan vien:")) {
                    String employeeInfo = line.substring("Nhan vien:".length()).trim();
                    String currentEmployeeId = employeeInfo.split("-")[0].trim();
                    if (currentEmployeeId.equals(employeeId)) {
                        foundEmployee = true;
                    }
                }
                
                // Nếu tìm thấy nhân viên và đến dòng tổng tiền, cộng vào tổng doanh số
                if (foundEmployee && line.startsWith("Tong tien:")) {
                    String amountStr = line.substring("Tong tien:".length())
                        .trim()
                        .replace("VND", "")
                        .replace(",", "")
                        .trim();
                    try {
                        totalSales += Double.parseDouble(amountStr);
                        isCurrentInvoice = false;
                        foundEmployee = false;
                    } catch (NumberFormatException e) {
                        System.err.println("Lỗi khi parse số tiền: " + e.getMessage());
                    }
                }
            }
            
            return totalSales;
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file hóa đơn: " + e.getMessage());
            return 0.0;
        }
    }

    public List<Employee> getAllEmployees() {
        loadEmployees();
        return new ArrayList<>(employees);
    }

    public void displayEmployeesFromFile() {
        fileHandler.readTextFile(FILENAME);
    }

    private void loadEmployees() {
        List<Employee> loadedEmployees = fileHandler.loadFromFile(FILENAME);
        if (loadedEmployees != null) {
            employees = loadedEmployees;
        }
    }
} 