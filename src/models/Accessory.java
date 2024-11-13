package models;

public class Accessory extends Product {
    private String type;

    public Accessory(String id, String name, double price, int quantity, String type) {
        super(id, name, price, quantity);
        this.type = type;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String getInfo() {
        return String.format("Accessory [ID: %s, Name: %s, Price: %,.0f, Quantity: %d, Type: %s]",
                           getId(), getName(), getPrice(), getQuantity(), type);
    }

    @Override
    public double calculateDiscount() {
        // Accessories get 5% discount if quantity > 10
        return getQuantity() > 10 ? 5.0 : 0.0;
    }
} 