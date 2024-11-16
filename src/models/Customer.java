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

    public Customer(String id, String name, String address, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.invoices = new ArrayList<>();
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

    public void addInvoice(Invoice invoice) {
        if (invoices == null) {
            invoices = new ArrayList<>();
        }
        invoices.add(invoice);
    }

    @Override
    public String getInfo() {
        return String.format("Customer [ID: %s, Name: %s, Address: %s, Phone: %s]",
                           id, name, address, phone);
    }

    @Override
    public void display() {
        System.out.println(getInfo());
    }
} 