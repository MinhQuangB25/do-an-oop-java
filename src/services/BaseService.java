package services;

import utils.FileHandler;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.io.IOException;

public abstract class BaseService<T> {
    protected FileHandler<T> fileHandler;
    protected List<T> items;
    protected final String filename;
    protected Scanner scanner;

    protected BaseService(String filename) {
        this.filename = filename;
        this.fileHandler = new FileHandler<>();
        this.scanner = new Scanner(System.in);
    }

    // Các phương thức CRUD cơ bản
    protected abstract void loadItems();
    public abstract Optional<T> findById(String id);
    protected abstract void validateInput(String id, String name);

    // Các phương thức tiện ích xử lý đầu vào
    protected String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    protected double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so hop le!");
            }
        }
    }

    protected int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so nguyen hop le!");
            }
        }
    }

    // Phương thức hiển thị
    public void displayFromFile() {
        try {
            List<String> lines = fileHandler.readAllLines(filename);
            if (lines.isEmpty()) {
                System.out.println("Danh sach trong!");
                return;
            }

            for (String line : lines) {
                if (!line.startsWith("=====") && !line.startsWith("----")) {
                    System.out.println(line);
                    System.out.println("----------------------------------------");
                }
            }
        } catch (IOException e) {
            System.err.println("Loi khi doc file: " + e.getMessage());
        }
    }

    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
} 