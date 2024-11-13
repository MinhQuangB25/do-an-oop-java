package services;

import models.Invoice;
import models.Product;
import models.Customer;
import models.Employee;
import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceService {
    private FileHandler<Invoice> fileHandler;
    private CustomerService customerService;
    private EmployeeService employeeService;
    private static final String FILENAME = "invoices.txt";

    public InvoiceService(CustomerService customerService, EmployeeService employeeService) {
        this.fileHandler = new FileHandler<>();
        this.customerService = customerService;
        this.employeeService = employeeService;
    }

    public void createInvoice(Invoice invoice) {
        try {
            // Kiểm tra customer và employee có tồn tại không
            Customer customer = customerService.findById(invoice.getCustomer().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Không tìm thấy khách hàng với mã: " + invoice.getCustomer().getId()));
            
            Employee employee = employeeService.findById(invoice.getEmployee().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Không tìm thấy nhân viên với mã: " + invoice.getEmployee().getId()));

            // Cập nhật thông tin
            invoice.setCustomer(customer);
            invoice.setEmployee(employee);

            // Lưu hóa đơn vào file chính
            List<Invoice> invoices = getAllInvoices();
            invoices.add(invoice);
            
            // Chỉ lưu hóa đơn dưới dạng text, không ghi đè file .dat
            fileHandler.saveInvoiceToText(invoice, FILENAME);

            // Cập nhật thông tin khách hàng và nhân viên
            customer.addInvoice(invoice);
            employee.addSalesInvoice(invoice);
            
            // Lưu lại thông tin customer và employee
            customerService.updateCustomer(customer);
            employeeService.updateEmployee(employee);
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu hóa đơn: " + e.getMessage());
        }
    }

    public Optional<Invoice> findById(String id) {
        return getAllInvoices().stream()
                .filter(i -> i.getId().equals(id))
                .findFirst();
    }

    public List<Invoice> findByCustomerId(String customerId) {
        return getAllInvoices().stream()
                .filter(i -> i.getCustomer().getId().equals(customerId))
                .toList();
    }

    public List<Invoice> findByEmployeeId(String employeeId) {
        return getAllInvoices().stream()
                .filter(i -> i.getEmployee().getId().equals(employeeId))
                .toList();
    }

    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = fileHandler.loadFromFile(FILENAME);
        return invoices != null ? invoices : new ArrayList<>();
    }

    public void displayInvoicesFromFile() {
        fileHandler.readTextFile(FILENAME);
    }
} 