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
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            List<String> lines = fileHandler.readAllLines(FILENAME);
            Invoice currentInvoice = null;
            List<String> currentInvoiceDetails = new ArrayList<>();
            boolean isTargetInvoice = false;

            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("===") || line.startsWith("---")) {
                    continue;
                }

                if (line.contains("Invoice [")) {
                    // Nếu tìm thấy hóa đơn mới, xử lý hóa đơn hiện tại (nếu có)
                    if (currentInvoice != null && isTargetInvoice) {
                        return Optional.of(currentInvoice);
                    }

                    // Parse thông tin hóa đơn mới
                    currentInvoice = parseInvoiceLine(line);
                    currentInvoiceDetails.clear();
                    isTargetInvoice = (currentInvoice != null && currentInvoice.getId().equals(id));
                    if (isTargetInvoice) {
                        currentInvoiceDetails.add(line);
                    }
                } else if (currentInvoice != null && isTargetInvoice) {
                    // Thu thập thông tin chi tiết của hóa đơn
                    if (line.startsWith("Khach hang:")) {
                        String[] parts = line.substring("Khach hang:".length()).trim().split("-");
                        if (parts.length > 0) {
                            String customerId = parts[0].trim();
                            customerService.findById(customerId).ifPresent(currentInvoice::setCustomer);
                        }
                        currentInvoiceDetails.add(line);
                    } else if (line.startsWith("Nhan vien:")) {
                        String[] parts = line.substring("Nhan vien:".length()).trim().split("-");
                        if (parts.length > 0) {
                            String employeeId = parts[0].trim();
                            employeeService.findById(employeeId).ifPresent(currentInvoice::setEmployee);
                        }
                        currentInvoiceDetails.add(line);
                    } else if (line.startsWith("Chi tiet san pham:")) {
                        currentInvoiceDetails.add(line);
                    } else if (line.startsWith("-") || line.startsWith("Tong tien:")) {
                        currentInvoiceDetails.add(line);
                    }
                }
            }

            // Xử lý hóa đơn cuối cùng
            if (currentInvoice != null && isTargetInvoice) {
                // Lưu thông tin chi tiết vào hóa đơn để hiển thị
                currentInvoice.setDisplayDetails(currentInvoiceDetails);
                return Optional.of(currentInvoice);
            }

            return Optional.empty();
        } catch (IOException e) {
            System.err.println("Loi khi doc file hoa don: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Invoice> findByDate(Date searchDate) {
        if (searchDate == null) return new ArrayList<>();
        
        try {
            List<String> lines = fileHandler.readAllLines(FILENAME);
            List<Invoice> result = new ArrayList<>();
            Invoice currentInvoice = null;
            List<String> currentInvoiceDetails = new ArrayList<>();
            boolean isTargetInvoice = false;
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String searchDateStr = sdf.format(searchDate);
            
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("===") || line.startsWith("---")) {
                    continue;
                }
                
                if (line.contains("Invoice [")) {
                    // Nếu đã có hóa đơn trước đó và là hóa đơn cần tìm, thêm vào kết quả
                    if (currentInvoice != null && isTargetInvoice) {
                        currentInvoice.setDisplayDetails(new ArrayList<>(currentInvoiceDetails));
                        result.add(currentInvoice);
                    }
                    
                    // Parse hóa đơn mới
                    currentInvoice = parseInvoiceLine(line);
                    currentInvoiceDetails.clear();
                    currentInvoiceDetails.add(line);
                    
                    if (currentInvoice != null) {
                        String invoiceDateStr = sdf.format(currentInvoice.getDate());
                        isTargetInvoice = invoiceDateStr.equals(searchDateStr);
                    } else {
                        isTargetInvoice = false;
                    }
                } else if (currentInvoice != null && isTargetInvoice) {
                    // Thu thập thông tin chi tiết của hóa đơn
                    currentInvoiceDetails.add(line);
                    
                    if (line.startsWith("Khach hang:")) {
                        String[] parts = line.substring("Khach hang:".length()).trim().split("-");
                        if (parts.length > 0) {
                            String customerId = parts[0].trim();
                            customerService.findById(customerId).ifPresent(currentInvoice::setCustomer);
                        }
                    } else if (line.startsWith("Nhan vien:")) {
                        String[] parts = line.substring("Nhan vien:".length()).trim().split("-");
                        if (parts.length > 0) {
                            String employeeId = parts[0].trim();
                            employeeService.findById(employeeId).ifPresent(currentInvoice::setEmployee);
                        }
                    }
                }
            }
            
            // Xử lý hóa đơn cuối cùng nếu là hóa đơn cần tìm
            if (currentInvoice != null && isTargetInvoice) {
                currentInvoice.setDisplayDetails(new ArrayList<>(currentInvoiceDetails));
                result.add(currentInvoice);
            }
            
            return result;
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
            return new ArrayList<>();
        }
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
                        String amountStr = line.substring("Tong tien:".length())
                            .trim()
                            .replace("VND", "")
                            .replace(",", "")
                            .trim();
                        try {
                            double totalAmount = Double.parseDouble(amountStr);
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
} 