package models;

import interfaces.SystemInterfaces.Identifiable;
import interfaces.SystemInterfaces.Printable;
import java.io.Serializable;

public class Customer implements Identifiable, Printable, Serializable {
    private String id;
    private String name;
    private String address;
    private String phone;

    public Customer(String id, String name, String address, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
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

    @Override
    public String getInfo() {
        String displayPhone = phone;
        String displayAddress = address;
        
        if (phone != null && address != null && 
            phone.matches(".*[a-zA-Z].*") && address.matches("\\d+")) {
            displayPhone = address;
            displayAddress = phone;
        }
        
        return String.format("Customer [ID: %s, Name: %s, Address: %s, Phone: %s]",
                           id, name, displayAddress, displayPhone);
    }

    @Override
    public void display() {
        System.out.println(getInfo());
    }
} 