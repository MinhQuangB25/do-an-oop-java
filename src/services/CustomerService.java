package services;

import models.Customer;
import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerService {
    private FileHandler<Customer> fileHandler;
    private static final String FILENAME = "customers.txt";

    public CustomerService() {
        this.fileHandler = new FileHandler<>();
    }

    public void addCustomer(Customer customer) {
        List<Customer> customers = getAllCustomers();
        if (findById(customer.getId()).isPresent()) {
            System.out.println("Khách hàng với mã " + customer.getId() + " đã tồn tại!");
            return;
        }
        customers.add(customer);
        fileHandler.saveToFile(FILENAME, customers);
    }

    public void updateCustomer(Customer customer) {
        List<Customer> customers = getAllCustomers();
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId().equals(customer.getId())) {
                customers.set(i, customer);
                fileHandler.saveToFile(FILENAME, customers);
                return;
            }
        }
    }

    public void deleteCustomer(String id) {
        List<Customer> customers = getAllCustomers();
        customers.removeIf(c -> c.getId().equals(id));
        fileHandler.saveToFile(FILENAME, customers);
    }

    public Optional<Customer> findById(String id) {
        return getAllCustomers().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    public List<Customer> findByName(String name) {
        return getAllCustomers().stream()
                .filter(c -> c.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = fileHandler.loadFromFile(FILENAME);
        return customers != null ? customers : new ArrayList<>();
    }

    public void displayCustomersFromFile() {
        fileHandler.readTextFile(FILENAME);
    }
} 