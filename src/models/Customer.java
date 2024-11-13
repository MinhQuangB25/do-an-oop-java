package models;

import interfaces.SystemInterfaces.Identifiable;
import interfaces.SystemInterfaces.Printable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Customer implements Identifiable, Printable, Serializable {
    private String id;
    private String name;
    private String address;
    private String phone;
    private List<Invoice> invoices;
    private double totalPurchases;

    public Customer(String id, String name, String address, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.invoices = new ArrayList<>();
        this.totalPurchases = 0.0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public double getTotalPurchases() {
        return totalPurchases;
    }

    public void addInvoice(Invoice invoice) {
        invoices.add(invoice);
        totalPurchases += invoice.getTotalAmount();
    }

    public void displayPurchaseHistory() {
        if (invoices.isEmpty()) {
            System.out.println("Khách hàng chưa có đơn hàng nào!");
            return;
        }

        System.out.println("\n=== LỊCH SỬ MUA HÀNG ===");
        System.out.println("Khách hàng: " + name + " (ID: " + id + ")");
        System.out.println("Tổng giá trị đã mua: " + String.format("%,.0fđ", totalPurchases));
        System.out.println("\nChi tiết các đơn hàng:");
        
        for (Invoice invoice : invoices) {
            System.out.println("----------------------------------------");
            System.out.println("Mã hóa đơn: " + invoice.getId());
            System.out.println("Ngày mua: " + invoice.getDate());
            System.out.println("Nhân viên bán: " + invoice.getEmployee().getName());
            System.out.println("Các sản phẩm:");
            for (Invoice.InvoiceDetail detail : invoice.getItems()) {
                System.out.printf("- %s x%d: %,.0fđ%n", 
                    detail.getProduct().getName(),
                    detail.getQuantity(),
                    detail.getProduct().getPrice() * detail.getQuantity());
            }
            System.out.printf("Tổng tiền: %,.0fđ%n", invoice.getTotalAmount());
        }
        System.out.println("----------------------------------------");
    }

    public String getCustomerRank() {
        if (totalPurchases >= 100000000) return "VIP";
        if (totalPurchases >= 50000000) return "Gold";
        if (totalPurchases >= 20000000) return "Silver";
        return "Normal";
    }

    @Override
    public String getInfo() {
        return String.format("Customer [ID: %s, Name: %s, Address: %s, Phone: %s, Rank: %s]",
                           id, name, address, phone, getCustomerRank());
    }

    @Override
    public void display() {
        System.out.println(getInfo());
        System.out.printf("Tổng giá trị đã mua: %,.0fđ%n", totalPurchases);
    }
} 