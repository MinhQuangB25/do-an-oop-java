package models;

public class Computer extends Product {
    private String cpu;
    private String ram;
    private String hardDrive;

    public Computer(String id, String name, double price, int quantity, 
                   String cpu, String ram, String hardDrive) {
        super(id, name, price, quantity);
        this.cpu = cpu;
        this.ram = ram;
        this.hardDrive = hardDrive;
    }

    // Getters and Setters
    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }
    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }
    public String getHardDrive() { return hardDrive; }
    public void setHardDrive(String hardDrive) { this.hardDrive = hardDrive; }

    @Override
    public String getInfo() {
        return String.format("Computer [ID: %s, Name: %s, Price: %,.0f, Quantity: %d, " +
                           "CPU: %s, RAM: %s, Hard Drive: %s]",
                           getId(), getName(), getPrice(), getQuantity(), 
                           cpu, ram, hardDrive);
    }

   
} 