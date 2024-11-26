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
        // Kiểm tra và chuẩn hóa hardDrive
        String formattedHardDrive = hardDrive;
        if (formattedHardDrive.endsWith(", Hard Drive: GB")) {
            formattedHardDrive = formattedHardDrive.substring(0, formattedHardDrive.indexOf(", Hard Drive: GB"));
        }
        if (!formattedHardDrive.contains("GB")) {
            formattedHardDrive = formattedHardDrive + "GB";
        }

        return String.format("Computer [ID: %s, Name: %s, Price: %,.0f, Quantity: %d, " +
                           "CPU: %s, RAM: %s, Hard Drive: %s]",
                           getId(), getName(), getPrice(), getQuantity(), 
                           cpu, 
                           ram.contains("GB") ? ram : ram + "GB", 
                           formattedHardDrive);
    }

    @Override
    public void display() {
        // Sử dụng cùng logic với getInfo()
        String formattedHardDrive = hardDrive;
        if (formattedHardDrive.endsWith(", Hard Drive: GB")) {
            formattedHardDrive = formattedHardDrive.substring(0, formattedHardDrive.indexOf(", Hard Drive: GB"));
        }
        if (!formattedHardDrive.contains("GB")) {
            formattedHardDrive = formattedHardDrive + "GB";
        }

        System.out.printf("Computer [ID: %s, Name: %s, Price: %,.0f, Quantity: %d, " +
                   "CPU: %s, RAM: %s, Hard Drive: %s]%n",
                   getId(), getName(), getPrice(), getQuantity(), 
                   cpu, 
                   ram.contains("GB") ? ram : ram + "GB", 
                   formattedHardDrive);
    }

} 