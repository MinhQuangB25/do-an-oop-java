package services;

import models.Employee;
import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeService {
    private FileHandler<Employee> fileHandler;
    private static final String FILENAME = "employees.txt";

    public EmployeeService() {
        this.fileHandler = new FileHandler<>();
    }

    public void addEmployee(Employee employee) {
        List<Employee> employees = getAllEmployees();
        if (findById(employee.getId()).isPresent()) {
            System.out.println("Nhân viên với mã " + employee.getId() + " đã tồn tại!");
            return;
        }
        employees.add(employee);
        fileHandler.saveToFile(FILENAME, employees);
    }

    public void updateEmployee(Employee employee) {
        List<Employee> employees = getAllEmployees();
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getId().equals(employee.getId())) {
                employees.set(i, employee);
                fileHandler.saveToFile(FILENAME, employees);
                return;
            }
        }
    }

    public void deleteEmployee(String id) {
        List<Employee> employees = getAllEmployees();
        employees.removeIf(e -> e.getId().equals(id));
        fileHandler.saveToFile(FILENAME, employees);
    }

    public Optional<Employee> findById(String id) {
        return getAllEmployees().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }

    public List<Employee> findByName(String name) {
        return getAllEmployees().stream()
                .filter(e -> e.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    public double calculateTotalSales(String employeeId) {
        return findById(employeeId)
                .map(Employee::calculateTotalSales)
                .orElse(0.0);
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = fileHandler.loadFromFile(FILENAME);
        return employees != null ? employees : new ArrayList<>();
    }

    public void displayEmployeesFromFile() {
        fileHandler.readTextFile(FILENAME);
    }
} 