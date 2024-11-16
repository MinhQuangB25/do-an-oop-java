package services;

import models.Invoice;
import models.Product;
import models.Customer;
import models.Employee;
import utils.FileHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.text.SimpleDateFormat;
//import java.text.ParseException;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

public class InvoiceService {
    private List<Invoice> invoices;
    private FileHandler<Invoice> fileHandler;
    private CustomerService customerService;
    private EmployeeService employeeService;
    private ProductService productService;
    private static final String FILENAME = "invoices.txt";
    //private String productId;

    public InvoiceService(CustomerService customerService, EmployeeService employeeService, ProductService productService) {
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.productService = productService;
        this.fileHandler = new FileHandler<Invoice>();
        this.fileHandler.setServices(customerService, employeeService, productService);
        this.invoices = new ArrayList<>();
        loadInvoices();
    }

    public void createInvoice(Invoice invoice) {
        try {
            // 1. Kiem tra thong tin dau vao
            Customer customer = customerService.findById(invoice.getCustomer().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Khong tim thay khach hang voi ma: " + invoice.getCustomer().getId()));
            
            Employee employee = employeeService.findById(invoice.getEmployee().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Khong tim thay nhan vien voi ma: " + invoice.getEmployee().getId()));

            // 2. Kiem tra va cap nhat so luong san pham
            double totalAmount = 0;
            List<String> invoiceLines = new ArrayList<>();
            invoiceLines.add(String.format("Invoice [ID: %s, Date: %s]", 
                invoice.getId(), 
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(invoice.getDate())));
            invoiceLines.add("Khach hang: " + customer.getId() + " - " + customer.getName());
            invoiceLines.add("Nhan vien: " + employee.getId() + " - " + employee.getName());
            invoiceLines.add("Chi tiet san pham:");
            
            for (Invoice.InvoiceDetail detail : invoice.getItems()) {
                Product product = productService.findById(detail.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Khong tim thay san pham: " + detail.getProduct().getId()));
                
                int requestedQuantity = detail.getQuantity();
                if (product.getQuantity() < requestedQuantity) {
                    throw new IllegalStateException(
                        String.format("San pham %s chi con %d trong kho, khong du so luong yeu cau (%d)",
                            product.getName(), product.getQuantity(), requestedQuantity));
                }
                
                // Tinh tien cho tung san pham
                double itemTotal = product.getPrice() * requestedQuantity;
                totalAmount += itemTotal;
                
                // Cap nhat so luong trong kho
                product.setQuantity(product.getQuantity() - requestedQuantity);
                productService.updateProduct(product);
                
                // Them thong tin san pham vao hoa don
                invoiceLines.add(String.format("- %s (Ma: %s) x%d: %,.0f VND", 
                    product.getName(), 
                    product.getId(),
                    requestedQuantity, 
                    itemTotal));
            }

            // 3. Them tong tien vao hoa don
            invoiceLines.add(String.format("Tong tien: %,.0f VND", totalAmount));
            
            // 4. Luu hoa don vao file
            fileHandler.writeFormattedText(FILENAME, "===== HOA DON BAN HANG =====", invoiceLines);
            
            // 5. Cap nhat thong tin khach hang va nhan vien
            customer.addInvoice(invoice);
            customerService.updateCustomer(customer);
            
            employee.addSalesInvoice(invoice);
            employeeService.updateEmployee(employee);

            // 6. Hien thi hoa don cho khach hang
            System.out.println("\n========== HOA DON THANH TOAN ==========");
            for (String line : invoiceLines) {
                System.out.println(line);
            }
            System.out.println("========================================");
            
        } catch (Exception e) {
            // 7. Rollback neu co loi
            for (Invoice.InvoiceDetail detail : invoice.getItems()) {
                try {
                    Product product = productService.findById(detail.getProduct().getId()).orElse(null);
                    if (product != null) {
                        product.setQuantity(product.getQuantity() + detail.getQuantity());
                        productService.updateProduct(product);
                    }
                } catch (Exception rollbackError) {
                    System.err.println("Loi khi rollback san pham: " + rollbackError.getMessage());
                }
            }
            throw new RuntimeException("Loi khi tao hoa don: " + e.getMessage());
        }
    }

    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = fileHandler.loadFromFile(FILENAME);
        return invoices != null ? invoices : new ArrayList<>();
    }

    public void displayInvoicesFromFile() {
        try {
            List<String> lines = fileHandler.readAllLines(FILENAME);
            if (lines.isEmpty()) {
                System.out.println("Chưa có hóa đơn nào!");
                return;
            }
            
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file hóa đơn: " + e.getMessage());
        }
    }

    public void updateInvoiceFormat() {
        fileHandler.updateInvoiceFormat(FILENAME);
    }

    public Optional<Invoice> findById(String id) {
        loadInvoices();
        return invoices.stream()
            .filter(invoice -> invoice.getId().equals(id))
            .findFirst();
    }

    public List<Invoice> findByDate(Date date) {
        loadInvoices();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String searchDate = sdf.format(date);
        
        return invoices.stream()
            .filter(invoice -> sdf.format(invoice.getDate()).equals(searchDate))
            .collect(Collectors.toList());
    }

    private void loadInvoices() {
        try {
            List<String> lines = fileHandler.readAllLines(FILENAME);
            invoices = new ArrayList<>();
            
            Invoice currentInvoice = null;
            boolean isReadingProducts = false;
            
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("===") || line.startsWith("---")) {
                    continue;
                }
                
                if (line.contains("Invoice [")) {
                    currentInvoice = parseInvoiceLine(line);
                    if (currentInvoice != null) {
                        invoices.add(currentInvoice);
                    }
                    isReadingProducts = false;
                } else if (currentInvoice != null) {
                    if (line.startsWith("Khach hang:")) {
                        String[] parts = line.substring("Khach hang:".length()).trim().split("-");
                        if (parts.length > 0) {
                            String customerId = parts[0].trim();
                            final Invoice invoice = currentInvoice;
                            customerService.findById(customerId).ifPresent(invoice::setCustomer);
                        }
                    } else if (line.startsWith("Nhan vien:")) {
                        String[] parts = line.substring("Nhan vien:".length()).trim().split("-");
                        if (parts.length > 0) {
                            String employeeId = parts[0].trim();
                            final Invoice invoice = currentInvoice;
                            employeeService.findById(employeeId).ifPresent(invoice::setEmployee);
                        }
                    } else if (line.startsWith("Chi tiet san pham:")) {
                        isReadingProducts = true;
                    } else if (isReadingProducts && line.startsWith("-")) {
                        // Parse thông tin sản phẩm
                        String productInfo = line.substring(1).trim();
                        int maIndex = productInfo.indexOf("(Ma:");
                        int xIndex = productInfo.indexOf("x");
                        int priceIndex = productInfo.lastIndexOf(":");
                        
                        if (maIndex > 0 && xIndex > 0 && priceIndex > 0) {
                            String productId = productInfo.substring(maIndex + 4, productInfo.indexOf(")")).trim();
                            int quantity = Integer.parseInt(productInfo.substring(xIndex + 1, priceIndex).trim());
                            
                            final Invoice invoice = currentInvoice;
                            productService.findById(productId).ifPresent(product -> {
                                invoice.addItem(product, quantity);
                            });
                        }
                    } else if (line.startsWith("Tong tien:")) {
                        // Parse tổng tiền nếu cần
                        //String amountStr = line.substring("Tong tien:".length())
                        //    .trim()
                        //    .replace("VND", "")
                        //    .replace(",", "")
                        //    .trim();
                        try {
                            //double totalAmount = Double.parseDouble(amountStr);
                            // Không cần set totalAmount vì nó được tính tự động từ items
                        } catch (NumberFormatException e) {
                            System.err.println("Lỗi khi parse số tiền: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Loi khi doc file hoa don: " + e.getMessage());
        }
    }

    private Invoice parseInvoiceLine(String line) {
        try {
            // Format: Invoice [ID: xxx, Date: yyyy-MM-dd HH:mm:ss]
            String content = line.trim();
            if (!content.startsWith("Invoice [") || !content.endsWith("]")) {
                return null;
            }

            // Tách nội dung trong dấu []
            content = content.substring(content.indexOf("[") + 1, content.lastIndexOf("]"));
            
            // Tách ID
            String id = content.substring(content.indexOf("ID:") + 3, content.indexOf(",")).trim();
            
            // Tách Date
            String dateStr = content.substring(content.indexOf("Date:") + 5).trim();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(dateStr);
            
            return new Invoice(id, date);
        } catch (Exception e) {
            System.err.println("Lỗi khi parse hóa đơn: " + e.getMessage());
            return null;
        }
    }

    public List<Invoice> getCustomerInvoices(String customerId) {
        loadInvoices();
        return invoices.stream()
                .filter(invoice -> invoice.getCustomer() != null && 
                                 invoice.getCustomer().getId().equals(customerId))
                .sorted((i1, i2) -> i2.getDate().compareTo(i1.getDate())) // Sắp xếp theo ngày mới nhất
                .collect(Collectors.toList());
    }

    // Thống kê doanh thu theo khoảng thời gian
    public double calculateRevenue(Date startDate, Date endDate) {
        loadInvoices();
        return invoices.stream()
            .filter(invoice -> {
                Date invoiceDate = invoice.getDate();
                return !invoiceDate.before(startDate) && !invoiceDate.after(endDate);
            })
            .mapToDouble(this::calculateInvoiceTotal)
            .sum();
    }

    // Thống kê số lượng hóa đơn theo khoảng thời gian
    public int countInvoices(Date startDate, Date endDate) {
        loadInvoices();
        return (int) invoices.stream()
            .filter(invoice -> {
                Date invoiceDate = invoice.getDate();
                return !invoiceDate.before(startDate) && !invoiceDate.after(endDate);
            })
            .count();
    }

    // Thống kê doanh thu theo khách hàng
    public Map<Customer, Double> calculateRevenueByCustomer(Date startDate, Date endDate) {
        loadInvoices();
        return invoices.stream()
            .filter(invoice -> {
                Date invoiceDate = invoice.getDate();
                return !invoiceDate.before(startDate) && !invoiceDate.after(endDate);
            })
            .filter(invoice -> invoice.getCustomer() != null)
            .collect(Collectors.groupingBy(
                Invoice::getCustomer,
                Collectors.summingDouble(this::calculateInvoiceTotal)
            ));
    }

    // Thống kê doanh thu theo nhân viên
    public Map<Employee, Double> calculateRevenueByEmployee(Date startDate, Date endDate) {
        loadInvoices();
        return invoices.stream()
            .filter(invoice -> {
                Date invoiceDate = invoice.getDate();
                return !invoiceDate.before(startDate) && !invoiceDate.after(endDate);
            })
            .filter(invoice -> invoice.getEmployee() != null)
            .collect(Collectors.groupingBy(
                Invoice::getEmployee,
                Collectors.summingDouble(this::calculateInvoiceTotal)
            ));
    }

    // Thống kê sản phẩm bán chạy
    public Map<Product, Integer> getTopSellingProducts(Date startDate, Date endDate) {
        loadInvoices();
        Map<Product, Integer> productQuantities = new HashMap<>();
        
        invoices.stream()
            .filter(invoice -> {
                Date invoiceDate = invoice.getDate();
                return !invoiceDate.before(startDate) && !invoiceDate.after(endDate);
            })
            .flatMap(invoice -> invoice.getItems().stream())
            .forEach(detail -> {
                Product product = detail.getProduct();
                productQuantities.merge(product, detail.getQuantity(), Integer::sum);
            });
        
        return productQuantities;
    }

    // Hiển thị báo cáo thống kê
    public void displayStatistics(Date startDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        System.out.println("\n========== BAO CAO THONG KE ==========");
        System.out.println("Tu ngay: " + sdf.format(startDate));
        System.out.println("Den ngay: " + sdf.format(endDate));
        System.out.println("----------------------------------------");
        
        // Thống kê cơ bản
        double totalRevenue = calculateRevenue(startDate, endDate);
        int totalInvoices = countInvoices(startDate, endDate);
        System.out.printf("Tong doanh thu: %,.0f VND%n", totalRevenue);
        System.out.printf("Tong so hoa don: %d%n", totalInvoices);
        System.out.println("----------------------------------------");
        
        // Thống kê theo khách hàng
        System.out.println("Doanh thu theo khach hang:");
        calculateRevenueByCustomer(startDate, endDate).forEach((customer, revenue) -> {
            System.out.printf("- %s: %,.0f VND%n", customer.getName(), revenue);
        });
        System.out.println("----------------------------------------");
        
        // Thống kê theo nhân viên
        System.out.println("Doanh thu theo nhân viên:");
        calculateRevenueByEmployee(startDate, endDate).forEach((employee, revenue) -> {
            System.out.printf("- %s: %,.0f VND%n", employee.getName(), revenue);
        });
        System.out.println("----------------------------------------");
        
        // Thống kê sản phẩm bán chạy
        System.out.println("Top san pham ban chay:");
        getTopSellingProducts(startDate, endDate).entrySet().stream()
            .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
            .limit(5) // Hiển thị top 5 sản phẩm
            .forEach(entry -> {
                System.out.printf("- %s: %d san pham%n", 
                    entry.getKey().getName(), entry.getValue());
            });
        System.out.println("========================================");
    }

    // Helper method để tính tổng tiền của một hóa đơn
    private double calculateInvoiceTotal(Invoice invoice) {
        return invoice.getItems().stream()
            .mapToDouble(detail -> detail.getProduct().getPrice() * detail.getQuantity())
            .sum();
    }
} 