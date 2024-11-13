package models;

import interfaces.SystemInterfaces.Identifiable;
import interfaces.SystemInterfaces.Printable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Employee implements Identifiable, Printable, Serializable {
    private String id;
    private String name;
    private String position;
    private double basicSalary;
    private List<Invoice> salesInvoices;

    public Employee(String id, String name, String position, double basicSalary) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.basicSalary = basicSalary;
        this.salesInvoices = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }

    public List<Invoice> getSalesInvoices() {
        return salesInvoices;
    }

    public void addSalesInvoice(Invoice invoice) {
        salesInvoices.add(invoice);
    }

    public double calculateTotalSales() {
        return salesInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
    }

    @Override
    public String getInfo() {
        return String.format("Employee [ID: %s, Name: %s, Position: %s, Basic Salary: %.2f]",
                           id, name, position, basicSalary);
    }

    @Override
    public void display() {
        System.out.println(getInfo());
    }
} 