package models;

import interfaces.SystemInterfaces.Identifiable;
import interfaces.SystemInterfaces.Printable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Invoice implements Identifiable, Printable, Serializable {
    private String id;
    private Date date;
    private Customer customer;
    private Employee employee;
    private List<InvoiceDetail> items;
    private double totalAmount;

    public Invoice(String id, Customer customer, Employee employee) {
        this.id = id;
        this.date = new Date();
        this.customer = customer;
        this.employee = employee;
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    // Inner class for invoice details
    public static class InvoiceDetail implements Serializable {
        private Product product;
        private int quantity;
        private double price;

        public InvoiceDetail(Product product, int quantity, double price) {
            this.product = product;
            this.quantity = quantity;
            this.price = price;
        }

        public double getSubtotal() {
            return quantity * price;
        }

        // Getters
        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
    }

    public void addItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (product.getQuantity() <= 0) {
            throw new IllegalArgumentException("Sản phẩm " + product.getName() + " đã hết hàng");
        }
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Số lượng trong kho không đủ cho sản phẩm: " + product.getName() + 
                " (Còn lại: " + product.getQuantity() + ")");
        }

        // Kiểm tra xem sản phẩm đã có trong hóa đơn chưa
        Optional<InvoiceDetail> existingItem = items.stream()
            .filter(item -> item.getProduct().getId().equals(product.getId()))
            .findFirst();

        if (existingItem.isPresent()) {
            // Nếu đã có, kiểm tra tổng số lượng
            InvoiceDetail detail = existingItem.get();
            int newQuantity = detail.getQuantity() + quantity;
            if (product.getQuantity() < newQuantity) {
                throw new IllegalArgumentException("Tổng số lượng vượt quá số lượng trong kho");
            }
            items.remove(detail);
            items.add(new InvoiceDetail(product, newQuantity, product.getPrice()));
        } else {
            // Nếu chưa có, thêm mới
            items.add(new InvoiceDetail(product, quantity, product.getPrice()));
        }

        // Cập nhật số lượng sản phẩm
        product.setQuantity(product.getQuantity() - quantity);
        calculateTotal();
    }

    private void calculateTotal() {
        this.totalAmount = items.stream()
                .mapToDouble(InvoiceDetail::getSubtotal)
                .sum();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Date getDate() { return date; }
    public Customer getCustomer() { return customer; }
    public Employee getEmployee() { return employee; }
    public List<InvoiceDetail> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Invoice [ID: %s, Date: %s]\n", id, date));
        sb.append(String.format("Khách hàng: %s\n", customer.getName()));
        sb.append(String.format("Nhân viên: %s\n", employee.getName()));
        sb.append("Sản phẩm:\n");
        for (InvoiceDetail item : items) {
            sb.append(String.format("- %s x%d: %,dđ\n", 
                item.getProduct().getName(), 
                item.getQuantity(), 
                (int)item.getSubtotal()));
        }
        sb.append(String.format("Tổng tiền: %,dđ", (int)totalAmount));
        return sb.toString();
    }

    @Override
    public void display() {
        System.out.println(getInfo());
    }
} 