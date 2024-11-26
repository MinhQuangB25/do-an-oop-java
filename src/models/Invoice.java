package models;

import interfaces.SystemInterfaces.Identifiable;
import interfaces.SystemInterfaces.Printable;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Invoice implements Identifiable, Printable, Serializable {
    private String id;
    private Date date;
    private Customer customer;
    private Employee employee;
    private List<InvoiceDetail> items;
    private double totalAmount;

    public Invoice(String id, Customer customer, Employee employee) {
        this.id = id;
        this.customer = customer;
        this.employee = employee;
        this.date = new Date();
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    public Invoice(String id, Date date) {
        this.id = id;
        this.date = date;
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    @Override
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    
    public List<InvoiceDetail> getItems() { return items; }
    public void setItems(List<InvoiceDetail> items) { 
        this.items = items;
        calculateTotal();
    }

    public double getTotalAmount() { return totalAmount; }

    public void addItem(Product product, int quantity) {
        items.add(new InvoiceDetail(product, quantity));
        calculateTotal();
    }

    private void calculateTotal() {
        this.totalAmount = items.stream()
                .mapToDouble(InvoiceDetail::getSubtotal)
                .sum();
    }

    @Override
    public String getInfo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Invoice [ID: %s, Date: %s]\n", id, sdf.format(date)));
        sb.append(String.format("Khach hang: %s - %s\n", customer.getId(), customer.getName()));
        sb.append(String.format("Nhan vien: %s - %s\n", employee.getId(), employee.getName()));
        sb.append("San pham:\n");
        for (InvoiceDetail item : items) {
            sb.append(String.format("- %s x%d: %,.0f VND\n", 
                item.getProduct().getName(),
                item.getQuantity(),
                item.getSubtotal()));
        }
        sb.append(String.format("Tong tien: %,.0f VND", totalAmount));
        return sb.toString();
    }

    @Override
    public void display() {
        System.out.println("\n========== CHI TIET HOA DON ==========");
        System.out.println(getInfo());
        System.out.println("======================================");
    }

    public static class InvoiceDetail implements Serializable {
        private Product product;
        private int quantity;

        public InvoiceDetail(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public double getSubtotal() { return product.getPrice() * quantity; }
    }
} 