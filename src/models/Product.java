package models;

import interfaces.SystemInterfaces.Discountable;
import interfaces.SystemInterfaces.Identifiable;
import interfaces.SystemInterfaces.Printable;
import java.io.Serializable;

public abstract class Product implements Discountable, Identifiable, Printable, Serializable {
    private String id;
    private String name;
    private double price;
    private int quantity;
    private static int totalProducts = 0;

    protected Product(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        totalProducts++;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Static method
    public static int getTotalProducts() {
        return totalProducts;
    }

    // Abstract method
    public abstract String getInfo();

    // Implement Discountable
    @Override
    public double calculateDiscount() {
        return 0.0; // Default implementation
    }

    @Override
    public void applyDiscount(double discountPercent) {
        this.price = price * (1 - discountPercent/100);
    }

    // Implement Printable
    @Override
    public void display() {
        System.out.println(getInfo());
    }
} 