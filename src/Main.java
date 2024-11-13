import models.*;
import services.*;
import java.util.Scanner;
import java.util.List;
import java.util.Optional;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static ProductService productService = new ProductService();
    private static CustomerService customerService = new CustomerService();
    private static EmployeeService employeeService = new EmployeeService();
    private static InvoiceService invoiceService = new InvoiceService(customerService, employeeService);

    public static void main(String[] args) {
        if (invoiceService.getAllInvoices().isEmpty()) {
            initializeSampleData();
        }
        
        while (true) {
            showMainMenu();
            int choice = getIntInput("Nhap lua chon cua ban: ");
            
            switch (choice) {
                case 1 -> manageProducts();
                case 2 -> manageCustomers();
                case 3 -> manageEmployees();
                case 4 -> manageInvoices();
                case 0 -> {
                    System.out.println("Cam on ban da su dung chuong trinh!");
                    return;
                }
                default -> System.out.println("Lua chon khong hop le!");
            }
        }
    }

    private static void showMainMenu() {
        System.out.println("\n===== QUAN LY CUA HANG MAY TINH =====");
        System.out.println("1. Quan ly san pham");
        System.out.println("2. Quan ly khach hang");
        System.out.println("3. Quan ly nhan vien");
        System.out.println("4. Quan ly hoa don");
        System.out.println("0. Thoat");
    }

    private static void manageProducts() {
        while (true) {
            System.out.println("\n===== QUAN LY SAN PHAM =====");
            System.out.println("1. Them may tinh");
            System.out.println("2. Them phu kien");
            System.out.println("3. Xem danh sach san pham");
            System.out.println("4. Tim kiem san pham");
            System.out.println("5. Cap nhat san pham");
            System.out.println("6. Xoa san pham");
            System.out.println("0. Quay lai");

            int choice = getIntInput("Nhap lua chon: ");
            switch (choice) {
                case 1 -> addComputer();
                case 2 -> addAccessory();
                case 3 -> displayProducts();
                case 4 -> searchProducts();
                case 5 -> updateProduct();
                case 6 -> deleteProduct();
                case 0 -> { return; }
                default -> System.out.println("Lua chon khong hop le!");
            }
        }
    }

    private static void addComputer() {
        System.out.println("\n=== THEM MAY TINH MOI ===");
        String id = getStringInput("Nhap ma may tinh: ");
        String name = getStringInput("Nhap ten may tinh: ");
        double price = getDoubleInput("Nhap gia: ");
        int quantity = getIntInput("Nhap so luong: ");
        String cpu = getStringInput("Nhap CPU: ");
        String ram = getStringInput("Nhap RAM: ");
        String hardDrive = getStringInput("Nhap o cung: ");

        Computer computer = new Computer(id, name, price, quantity, cpu, ram, hardDrive);
        productService.addProduct(computer);
        System.out.println("Them may tinh thanh cong!");
    }

    private static void addAccessory() {
        System.out.println("\n=== THEM PHU KIEN MOI ===");
        String id = getStringInput("Nhap ma phu kien: ");
        String name = getStringInput("Nhap ten phu kien: ");
        double price = getDoubleInput("Nhap gia: ");
        int quantity = getIntInput("Nhap so luong: ");
        String type = getStringInput("Nhap loai phu kien: ");

        Accessory accessory = new Accessory(id, name, price, quantity, type);
        productService.addProduct(accessory);
        System.out.println("Them phu kien thanh cong!");
    }

    private static void displayProducts() {
        System.out.println("\n=== DANH SACH SAN PHAM ===");
        productService.displayProductsFromFile();
    }

    private static void searchProducts() {
        System.out.println("\n=== TIM KIEM SAN PHAM ===");
        String keyword = getStringInput("Nhap ten san pham can tim: ");
        List<Product> products = productService.findByName(keyword);
        if (products.isEmpty()) {
            System.out.println("Khong tim thay san pham nao!");
            return;
        }
        products.forEach(Product::display);
    }

    private static void updateProduct() {
        System.out.println("\n=== CAP NHAT SAN PHAM ===");
        String id = getStringInput("Nhap ma san pham can cap nhat: ");
        Optional<Product> productOpt = productService.findById(id);
        
        if (productOpt.isEmpty()) {
            System.out.println("Khong tim thay san pham!");
            return;
        }

        Product product = productOpt.get();
        System.out.println("Thong tin san pham hien tai:");
        product.display();

        // Cap nhat thong tin co ban
        String name = getStringInput("Nhap ten moi (Enter de giu nguyen): ");
        if (!name.isEmpty()) product.setName(name);

        String priceStr = getStringInput("Nhap gia moi (Enter de giu nguyen): ");
        if (!priceStr.isEmpty()) product.setPrice(Double.parseDouble(priceStr));

        String quantityStr = getStringInput("Nhap so luong moi (Enter de giu nguyen): ");
        if (!quantityStr.isEmpty()) product.setQuantity(Integer.parseInt(quantityStr));

        // Cap nhat thong tin rieng cua tung loai san pham
        if (product instanceof Computer) {
            updateComputerDetails((Computer) product);
        } else if (product instanceof Accessory) {
            updateAccessoryDetails((Accessory) product);
        }

        productService.updateProduct(product);
        System.out.println("Cap nhat san pham thanh cong!");
    }

    private static void updateComputerDetails(Computer computer) {
        String cpu = getStringInput("Nhap CPU moi (Enter de giu nguyen): ");
        if (!cpu.isEmpty()) computer.setCpu(cpu);

        String ram = getStringInput("Nhap RAM moi (Enter de giu nguyen): ");
        if (!ram.isEmpty()) computer.setRam(ram);

        String hardDrive = getStringInput("Nhap o cung moi (Enter de giu nguyen): ");
        if (!hardDrive.isEmpty()) computer.setHardDrive(hardDrive);
    }

    private static void updateAccessoryDetails(Accessory accessory) {
        String type = getStringInput("Nhap loai phu kien moi (Enter de giu nguyen): ");
        if (!type.isEmpty()) accessory.setType(type);
    }

    private static void deleteProduct() {
        System.out.println("\n=== XOA SAN PHAM ===");
        String id = getStringInput("Nhap ma san pham can xoa: ");
        
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            System.out.println("San pham can xoa:");
            product.get().display();
            
            String confirm = getStringInput("Ban co chac chan muon xoa? (Y/N): ");
            if (confirm.equalsIgnoreCase("Y")) {
                productService.deleteProduct(id);
                System.out.println("Da xoa san pham thanh cong!");
            } else {
                System.out.println("Huy xoa san pham!");
            }
        } else {
            System.out.println("Khong tim thay san pham!");
        }
    }

    private static void manageCustomers() {
        while (true) {
            System.out.println("\n===== QUAN LY KHACH HANG =====");
            System.out.println("1. Them khach hang");
            System.out.println("2. Xem danh sach khach hang");
            System.out.println("3. Tim kiem khach hang");
            System.out.println("4. Cap nhat khach hang");
            System.out.println("5. Xoa khach hang");
            System.out.println("6. Mua hang");
            System.out.println("7. Xem lich su mua hang");
            System.out.println("0. Quay lai");

            int choice = getIntInput("Nhap lua chon: ");
            switch (choice) {
                case 1 -> addCustomer();
                case 2 -> displayCustomers();
                case 3 -> searchCustomers();
                case 4 -> updateCustomer();
                case 5 -> deleteCustomer();
                case 6 -> customerPurchase();
                case 7 -> viewCustomerHistory();
                case 0 -> { return; }
                default -> System.out.println("Lua chon khong hop le!");
            }
        }
    }

    private static void addCustomer() {
        System.out.println("\n=== THEM KHACH HANG MOI ===");
        String id = getStringInput("Nhap ma khach hang: ");
        String name = getStringInput("Nhap ten khach hang: ");
        String address = getStringInput("Nhap dia chi: ");
        String phone = getStringInput("Nhap so dien thoai: ");

        Customer customer = new Customer(id, name, address, phone);
        customerService.addCustomer(customer);
        System.out.println("Them khach hang thanh cong!");
    }

    private static void displayCustomers() {
        System.out.println("\n=== DANH SACH KHACH HANG ===");
        customerService.displayCustomersFromFile();
    }

    private static void searchCustomers() {
        System.out.println("\n=== TIM KIEM KHACH HANG ===");
        String keyword = getStringInput("Nhap ten khach hang can tim: ");
        List<Customer> customers = customerService.findByName(keyword);
        if (customers.isEmpty()) {
            System.out.println("Khong tim thay khach hang nao!");
            return;
        }
        customers.forEach(Customer::display);
    }

    private static void updateCustomer() {
        System.out.println("\n=== CAP NHAT KHACH HANG ===");
        String id = getStringInput("Nhap ma khach hang can cap nhat: ");
        Optional<Customer> customerOpt = customerService.findById(id);
        
        if (customerOpt.isEmpty()) {
            System.out.println("Khong tim thay khach hang!");
            return;
        }

        Customer customer = customerOpt.get();
        System.out.println("Thong tin khach hang hien tai:");
        customer.display();

        String name = getStringInput("Nhap ten moi (Enter de giu nguyen): ");
        if (!name.isEmpty()) customer.setName(name);

        String address = getStringInput("Nhap dia chi moi (Enter de giu nguyen): ");
        if (!address.isEmpty()) customer.setAddress(address);

        String phone = getStringInput("Nhap so dien thoai moi (Enter de giu nguyen): ");
        if (!phone.isEmpty()) customer.setPhone(phone);

        customerService.updateCustomer(customer);
        System.out.println("Cap nhat khach hang thanh cong!");
    }

    private static void deleteCustomer() {
        System.out.println("\n=== XOA KHACH HANG ===");
        String id = getStringInput("Nhap ma khach hang can xoa: ");
        Optional<Customer> customerOpt = customerService.findById(id);
        
        if (customerOpt.isEmpty()) {
            System.out.println("Khong tim thay khach hang!");
            return;
        }

        customerService.deleteCustomer(id);
        System.out.println("Xoa khach hang thanh cong!");
    }

    private static void viewCustomerHistory() {
        System.out.println("\n=== XEM LICH SU MUA HANG ===");
        String id = getStringInput("Nhap ma khach hang: ");
        Optional<Customer> customerOpt = customerService.findById(id);
        
        if (customerOpt.isEmpty()) {
            System.out.println("Khong tim thay khach hang!");
            return;
        }

        customerOpt.get().displayPurchaseHistory();
    }

    private static void customerPurchase() {
        System.out.println("\n=== MUA HANG ===");
        
        // Chọn khách hàng
        String customerId = getStringInput("Nhap ma khach hang: ");
        Optional<Customer> customerOpt = customerService.findById(customerId);
        if (customerOpt.isEmpty()) {
            System.out.println("Không tìm thấy khách hàng! Vui lòng đăng ký trước khi mua hàng.");
            return;
        }
        Customer customer = customerOpt.get();

        // Chọn nhân viên bán hàng
        String employeeId = getStringInput("\nNhap ma nhan vien ban hang: ");
        Optional<Employee> employeeOpt = employeeService.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            System.out.println("Không tìm thấy nhân viên!");
            return;
        }
        Employee employee = employeeOpt.get();

        // Tạo hóa đơn mới
        String invoiceId = "HD" + System.currentTimeMillis();
        Invoice invoice = new Invoice(invoiceId, customer, employee);

        // Hiển thị danh sách sản phẩm
        System.out.println("\nDanh sach san pham hien co:");
        productService.displayProductsFromFile();

        // Thêm sản phẩm vào hóa đơn
        while (true) {
            System.out.println("\nThem san pham vao gio hang (nhap ma san pham trong de ket thuc):");
            String productId = getStringInput("Nhap ma san pham: ");
            if (productId.isEmpty()) break;

            Optional<Product> productOpt = productService.findById(productId);
            if (productOpt.isEmpty()) {
                System.out.println("Khong tim thay san pham!");
                continue;
            }

            Product product = productOpt.get();
            if (product.getQuantity() <= 0) {
                System.out.println("San pham da het hang!");
                continue;
            }

            System.out.println("San pham: " + product.getName());
            System.out.printf("Gia: %,dđ%n", (int)product.getPrice());
            System.out.println("So luong ton: " + product.getQuantity());
            
            int quantity = getIntInput("Nhap so luong mua: ");
            if (quantity <= 0) {
                System.out.println("So luong khong hop le!");
                continue;
            }

            if (quantity > product.getQuantity()) {
                System.out.println("So luong vuot qua hang ton kho!");
                continue;
            }

            try {
                invoice.addItem(product, quantity);
                // Cập nhật số lượng tồn kho
                product.setQuantity(product.getQuantity() - quantity);
                System.out.println("Da them san pham vao gio hang!");
            } catch (IllegalArgumentException e) {
                System.out.println("Loi: " + e.getMessage());
            }
        }

        // Xác nhận và lưu hóa đơn
        if (invoice.getItems().isEmpty()) {
            System.out.println("Gio hang trong, huy giao dich!");
            return;
        }

        // Hiển thị hóa đơn tạm tính
        System.out.println("\nHoa don tam tinh:");
        invoice.display();

        String confirm = getStringInput("\nXac nhan mua hang? (Y/N): ");
        if (confirm.equalsIgnoreCase("Y")) {
            try {
                // Lưu hóa đơn và cập nhật thông tin liên quan
                invoiceService.createInvoice(invoice);
                // Cập nhật số lượng sản phẩm trong file
                for (Invoice.InvoiceDetail detail : invoice.getItems()) {
                    productService.updateProduct(detail.getProduct());
                }
                System.out.println("\nMua hang thanh cong!");
                System.out.println("Chi tiet hoa don:");
                invoice.display();
            } catch (Exception e) {
                System.out.println("Loi khi tao hoa don: " + e.getMessage());
                // Hoàn trả số lượng sản phẩm nếu có lỗi
                for (Invoice.InvoiceDetail detail : invoice.getItems()) {
                    Product p = detail.getProduct();
                    p.setQuantity(p.getQuantity() + detail.getQuantity());
                    productService.updateProduct(p);
                }
            }
        } else {
            System.out.println("Da huy giao dich!");
            // Hoàn trả số lượng sản phẩm khi hủy
            for (Invoice.InvoiceDetail detail : invoice.getItems()) {
                Product p = detail.getProduct();
                p.setQuantity(p.getQuantity() + detail.getQuantity());
                productService.updateProduct(p);
            }
        }
    }

    private static void manageEmployees() {
        while (true) {
            System.out.println("\n===== QUAN LY NHAN VIEN =====");
            System.out.println("1. Them nhan vien");
            System.out.println("2. Xem danh sach nhan vien");
            System.out.println("3. Tim kiem nhan vien");
            System.out.println("4. Cap nhat nhan vien");
            System.out.println("5. Xoa nhan vien");
            System.out.println("6. Xem doanh so nhan vien");
            System.out.println("0. Quay lai");

            int choice = getIntInput("Nhap lua chon: ");
            switch (choice) {
                case 1 -> addEmployee();
                case 2 -> displayEmployees();
                case 3 -> searchEmployees();
                case 4 -> updateEmployee();
                case 5 -> deleteEmployee();
                case 6 -> viewEmployeeSales();
                case 0 -> { return; }
                default -> System.out.println("Lua chon khong hop le!");
            }
        }
    }

    private static void addEmployee() {
        System.out.println("\n=== THEM NHAN VIEN MOI ===");
        String id = getStringInput("Nhap ma nhan vien: ");
        String name = getStringInput("Nhap ten nhan vien: ");
        String position = getStringInput("Nhap chuc vu: ");
        double salary = getDoubleInput("Nhap luong co ban: ");

        Employee employee = new Employee(id, name, position, salary);
        employeeService.addEmployee(employee);
        System.out.println("Them nhan vien thanh cong!");
    }

    private static void displayEmployees() {
        System.out.println("\n=== DANH SACH NHAN VIEN ===");
        employeeService.displayEmployeesFromFile();
    }

    private static void searchEmployees() {
        System.out.println("\n=== TIM KIEM NHAN VIEN ===");
        String keyword = getStringInput("Nhap ten nhan vien can tim: ");
        List<Employee> employees = employeeService.findByName(keyword);
        if (employees.isEmpty()) {
            System.out.println("Khong tim thay nhan vien nao!");
            return;
        }
        employees.forEach(Employee::display);
    }

    private static void updateEmployee() {
        System.out.println("\n=== CAP NHAT NHAN VIEN ===");
        String id = getStringInput("Nhap ma nhan vien can cap nhat: ");
        Optional<Employee> employeeOpt = employeeService.findById(id);
        
        if (employeeOpt.isEmpty()) {
            System.out.println("Khong tim thay nhan vien!");
            return;
        }

        Employee employee = employeeOpt.get();
        System.out.println("Thong tin nhan vien hien tai:");
        employee.display();

        String name = getStringInput("Nhap ten moi (Enter de giu nguyen): ");
        if (!name.isEmpty()) employee.setName(name);

        String position = getStringInput("Nhap chuc vu moi (Enter de giu nguyen): ");
        if (!position.isEmpty()) employee.setPosition(position);

        String salaryStr = getStringInput("Nhap luong co ban moi (Enter de giu nguyen): ");
        if (!salaryStr.isEmpty()) employee.setBasicSalary(Double.parseDouble(salaryStr));

        employeeService.updateEmployee(employee);
        System.out.println("Cap nhat nhan vien thanh cong!");
    }

    private static void deleteEmployee() {
        System.out.println("\n=== XOA NHAN VIEN ===");
        String id = getStringInput("Nhap ma nhan vien can xoa: ");
        Optional<Employee> employeeOpt = employeeService.findById(id);
        
        if (employeeOpt.isEmpty()) {
            System.out.println("Khong tim thay nhan vien!");
            return;
        }

        employeeService.deleteEmployee(id);
        System.out.println("Xoa nhan vien thanh cong!");
    }

    private static void viewEmployeeSales() {
        System.out.println("\n=== XEM DOANH SO NHAN VIEN ===");
        String id = getStringInput("Nhap ma nhan vien: ");
        Optional<Employee> employeeOpt = employeeService.findById(id);
        
        if (employeeOpt.isEmpty()) {
            System.out.println("Khong tim thay nhan vien!");
            return;
        }

        Employee employee = employeeOpt.get();
        double totalSales = employeeService.calculateTotalSales(id);
        System.out.println("Nhan vien: " + employee.getName());
        System.out.println("Tong doanh so: $" + String.format("%.2f", totalSales));
    }

    private static void manageInvoices() {
        while (true) {
            System.out.println("\n===== QUAN LY HOA DON =====");
            System.out.println("1. Tao hoa don moi");
            System.out.println("2. Xem danh sach hoa don");
            System.out.println("3. Tim hoa don theo ma");
            System.out.println("4. Tim hoa don theo khach hang");
            System.out.println("5. Tim hoa don theo nhan vien");
            System.out.println("0. Quay lai");

            int choice = getIntInput("Nhap lua chon: ");
            switch (choice) {
                case 1 -> createNewInvoice();
                case 2 -> displayInvoices();
                case 3 -> findInvoiceById();
                case 4 -> findInvoicesByCustomer();
                case 5 -> findInvoicesByEmployee();
                case 0 -> { return; }
                default -> System.out.println("Lua chon khong hop le!");
            }
        }
    }

    private static void createNewInvoice() {
        System.out.println("\n=== TAO HOA DON MOI ===");
        
        // Chon khach hang
        String customerId = getStringInput("Nhap ma khach hang: ");
        Optional<Customer> customerOpt = customerService.findById(customerId);
        if (customerOpt.isEmpty()) {
            System.out.println("Khong tim thay khach hang!");
            return;
        }

        // Chon nhan vien
        String employeeId = getStringInput("Nhap ma nhan vien ban hang: ");
        Optional<Employee> employeeOpt = employeeService.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            System.out.println("Khong tim thay nhan vien!");
            return;
        }

        // Tao hoa don moi
        String invoiceId = "INV" + System.currentTimeMillis();
        Invoice invoice = new Invoice(invoiceId, customerOpt.get(), employeeOpt.get());

        // Them san pham vao hoa don
        while (true) {
            System.out.println("\nThem san pham vao hoa don (nhap ma san pham trong de ket thuc):");
            String productId = getStringInput("Nhap ma san pham: ");
            if (productId.isEmpty()) break;

            Optional<Product> productOpt = productService.findById(productId);
            if (productOpt.isEmpty()) {
                System.out.println("Khong tim thay san pham!");
                continue;
            }

            Product product = productOpt.get();
            System.out.println("San pham: " + product.getName());
            System.out.println("So luong ton: " + product.getQuantity());
            
            int quantity = getIntInput("Nhap so luong mua: ");
            if (quantity <= 0) {
                System.out.println("So luong khong hop le!");
                continue;
            }

            try {
                invoice.addItem(product, quantity);
                System.out.println("Da them san pham vao hoa don!");
            } catch (IllegalArgumentException e) {
                System.out.println("Loi: " + e.getMessage());
            }
        }

        // Kiem tra va luu hoa don
        if (invoice.getItems().isEmpty()) {
            System.out.println("Hoa don trong, khong the luu!");
            return;
        }

        try {
            invoiceService.createInvoice(invoice);
            customerOpt.get().addInvoice(invoice);
            employeeOpt.get().addSalesInvoice(invoice);
            System.out.println("\nTao hoa don thanh cong!");
            System.out.println("Chi tiet hoa don:");
            invoice.display();
        } catch (IllegalStateException e) {
            System.out.println("Loi khi tao hoa don: " + e.getMessage());
        }
    }

    private static void displayInvoices() {
        System.out.println("\n=== DANH SACH HOA DON ===");
        invoiceService.displayInvoicesFromFile();
    }

    private static void findInvoiceById() {
        System.out.println("\n=== TIM HOA DON THEO MA ===");
        String id = getStringInput("Nhap ma hoa don: ");
        Optional<Invoice> invoiceOpt = invoiceService.findById(id);
        
        if (invoiceOpt.isEmpty()) {
            System.out.println("Khong tim thay hoa don!");
            return;
        }
        
        System.out.println("Chi tiet hoa don:");
        invoiceOpt.get().display();
    }

    private static void findInvoicesByCustomer() {
        System.out.println("\n=== TIM HOA DON THEO KHACH HANG ===");
        String customerId = getStringInput("Nhap ma khach hang: ");
        List<Invoice> invoices = invoiceService.findByCustomerId(customerId);
        
        if (invoices.isEmpty()) {
            System.out.println("Khong tim thay hoa don nao cua khach hang nay!");
            return;
        }
        
        System.out.println("Danh sach hoa don:");
        invoices.forEach(Invoice::display);
    }

    private static void findInvoicesByEmployee() {
        System.out.println("\n=== TIM HOA DON THEO NHAN VIEN ===");
        String employeeId = getStringInput("Nhap ma nhan vien: ");
        List<Invoice> invoices = invoiceService.findByEmployeeId(employeeId);
        
        if (invoices.isEmpty()) {
            System.out.println("Khong tim thay hoa don nao cua nhan vien nay!");
            return;
        }
        
        System.out.println("Danh sach hoa don:");
        invoices.forEach(Invoice::display);
    }

    private static void initializeSampleData() {
        Customer customer = new Customer("KH001", "Nguyen Van A", "Ha Noi", "0123456789");
        customerService.addCustomer(customer);
        
        Employee employee = new Employee("NV001", "Le Van X", "Sales", 5000000.00);
        employeeService.addEmployee(employee);
        
        Computer computer = new Computer("C001", "Dell XPS 13", 1299.99, 5, "Intel i7", "16GB", "512GB SSD");
        productService.addProduct(computer);
        
        Invoice invoice = new Invoice("HD001", customer, employee);
        try {
            invoice.addItem(computer, 1);
            invoiceService.createInvoice(invoice);
        } catch (Exception e) {
            System.err.println("Error creating sample invoice: " + e.getMessage());
        }
    }

    // Utility methods for input handling
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so nguyen hop le!");
            }
        }
    }

    private static double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so thuc hop le!");
            }
        }
    }
} 