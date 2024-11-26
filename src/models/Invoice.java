package models;

import interfaces.SystemInterfaces.Identifiable;
import interfaces.SystemInterfaces.Printable;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Invoice implements Identifiable, Printable, Serializable {
    private final String id;
    private final Date date;
    private Customer customer;
    private Employee employee;
    private List<InvoiceDetail> items;
    private double totalAmount;

    public Invoice(String id, Date date) {
        this.id = id;
        this.date = date;
        this.items = new ArrayList<>();
    }

    // Getters
    @Override
    public String getId() { return id; }
    
    @Override
    public void setId(String id) {
        // Không thực hiện gì vì id là final
        throw new UnsupportedOperationException("Cannot change invoice ID after creation");
    }
    
    public Date getDate() { return date; }
    public Customer getCustomer() { return customer; }
    public Employee getEmployee() { return employee; }
    public List<InvoiceDetail> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    
    // Setters
    public void setCustomer(Customer customer) { this.customer = customer; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public void setItems(List<InvoiceDetail> items) { 
        this.items = items;
        calculateTotal();
    }

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
        private final Product product;
        private final int quantity;

        public InvoiceDetail(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public double getSubtotal() { return product.getPrice() * quantity; }
    }
} 