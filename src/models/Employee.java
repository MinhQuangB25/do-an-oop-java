package models;

import interfaces.SystemInterfaces.Identifiable;
import interfaces.SystemInterfaces.Printable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Employee implements Identifiable, Printable, Serializable {
    private String id;
    private String name;
    private String phone;
    private String address;
    private String position;
    private double basicSalary;
    private List<Invoice> invoices;

    // Constructor duy nhất với các tham số tùy chọn
    public Employee(String id, String name, String phone, String address, String position, double basicSalary) {
        this.id = id;
        this.name = name;
        this.phone = phone != null ? phone : "";
        this.address = address != null ? address : "";
        this.position = position != null ? position : "";
        this.basicSalary = basicSalary;
        this.invoices = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }

    @Override
    public String getInfo() {
        if (basicSalary > 0) {
            return String.format("Employee [ID: %s, Name: %s, Phone: %s, Address: %s, Position: %s, Basic Salary: %,.0f]",
                id, name, phone, address, position, basicSalary);
        } else {
            return String.format("Employee [ID: %s, Name: %s, Phone: %s, Address: %s, Position: %s]",
                id, name, phone, address, position);
        }
    }

    @Override
    public void display() {
        if (basicSalary > 0) {
            System.out.printf("Employee [ID: %s, Name: %s, Phone: %s, Address: %s, Position: %s, Basic Salary: %,.0f]%n",
                id, name, phone, address, position, basicSalary);
        } else {
            System.out.printf("Employee [ID: %s, Name: %s, Phone: %s, Address: %s, Position: %s]%n",
                id, name, phone, address, position);
        }
    }

    public void addSalesInvoice(Invoice invoice) {
        if (invoices == null) {
            invoices = new ArrayList<>();
        }
        invoices.add(invoice);
    }

    public List<Invoice> getSalesInvoices() {
        return invoices;
    }
} 