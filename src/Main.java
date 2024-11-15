import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import models.*;
import services.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static ProductService productService = new ProductService();
    private static CustomerService customerService = new CustomerService();
    private static EmployeeService employeeService = new EmployeeService();
    private static InvoiceService invoiceService = new InvoiceService(customerService, employeeService, productService);

    static {
        employeeService.setInvoiceService(invoiceService);
    }

    public static void main(String[] args) {
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
        System.out.println("1. Tim theo ten");
        System.out.println("2. Tim theo ma");
        System.out.println("0. Quay lai");
        
        int choice = getIntInput("Nhap lua chon: ");
        switch (choice) {
            case 1 -> {
                String keyword = getStringInput("Nhap ten san pham can tim: ");
                List<Product> products = productService.findByName(keyword);
                if (products.isEmpty()) {
                    System.out.println("Khong tim thay san pham nao!");
                    return;
                }
                System.out.println("\nKet qua tim kiem:");
                products.forEach(product -> {
                    System.out.println("----------------------------------------");
                    product.display();
                });
            }
            case 2 -> {
                String id = getStringInput("Nhap ma san pham can tim: ");
                Optional<Product> productOpt = productService.findById(id);
                if (productOpt.isEmpty()) {
                    System.out.println("Khong tim thay san pham!");
                    return;
                }
                System.out.println("\nKet qua tim kiem:");
                System.out.println("----------------------------------------");
                productOpt.get().display();
            }
            case 0 -> { }
            default -> System.out.println("Lua chon khong hop le!");
        }
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

        // Cập nhật thông tin chung
        String name = getStringInput("Nhap ten moi (Enter de giu nguyen): ");
        if (!name.isEmpty()) product.setName(name);

        String priceStr = getStringInput("Nhap gia moi (Enter de giu nguyen): ");
        if (!priceStr.isEmpty()) product.setPrice(Double.parseDouble(priceStr));

        String quantityStr = getStringInput("Nhap so luong moi (Enter de giu nguyen): ");
        if (!quantityStr.isEmpty()) product.setQuantity(Integer.parseInt(quantityStr));

        // Cập nhật thông tin riêng theo loại sản phẩm
        if (product instanceof Computer) {
            Computer computer = (Computer) product;
            String cpu = getStringInput("Nhap CPU moi (Enter de giu nguyen): ");
            if (!cpu.isEmpty()) computer.setCpu(cpu);

            String ram = getStringInput("Nhap RAM moi (Enter de giu nguyen): ");
            if (!ram.isEmpty()) computer.setRam(ram);

            String hardDrive = getStringInput("Nhap o cung moi (Enter de giu nguyen): ");
            if (!hardDrive.isEmpty()) computer.setHardDrive(hardDrive);
        } else if (product instanceof Accessory) {
            Accessory accessory = (Accessory) product;
            String type = getStringInput("Nhap loai phu kien moi (Enter de giu nguyen): ");
            if (!type.isEmpty()) accessory.setType(type);
        }

        // Cập nhật sản phẩm
        productService.updateProduct(product);
        System.out.println("Cap nhat san pham thanh cong!");
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
            System.out.println("0. Quay lai");

            int choice = getIntInput("Nhap lua chon: ");
            switch (choice) {
                case 1 -> addCustomer();
                case 2 -> displayCustomers();
                case 3 -> searchCustomers();
                case 4 -> updateCustomer();
                case 5 -> deleteCustomer();
                case 6 -> customerPurchase();
                case 0 -> { return; }
                default -> System.out.println("Lua chon khong hop le!");
            }
        }
    }

    private static void addCustomer() {
        System.out.println("\n=== THEM KHACH HANG MOI ===");
        
        // Kiểm tra và nhập ID
        String id;
        while (true) {
            id = getStringInput("Nhap ma khach hang (VD: KH001): ");
            if (id.isEmpty()) {
                System.out.println("Ma khach hang khong duoc de trong!");
                continue;
            }
            if (customerService.findById(id).isPresent()) {
                System.out.println("Ma khach hang da ton tai!");
                continue;
            }
            break;
        }

        // Kiểm tra và nhập tên
        String name;
        while (true) {
            name = getStringInput("Nhap ten khach hang: ");
            if (name.isEmpty()) {
                System.out.println("Ten khach hang khong duoc de trong!");
                continue;
            }
            break;
        }

        // Kiểm tra và nhập địa chỉ
        String address;
        while (true) {
            address = getStringInput("Nhap dia chi: ");
            if (address.isEmpty()) {
                System.out.println("Dia chi khong duoc de trong!");
                continue;
            }
            break;
        }

        // Kiểm tra và nhập số điện thoại
        String phone;
        while (true) {
            phone = getStringInput("Nhap so dien thoai: ");
            if (phone.isEmpty()) {
                System.out.println("So dien thoai khong duoc de trong!");
                continue;
            }
            if (!phone.matches("\\d{10,11}")) {
                System.out.println("So dien thoai khong hop le (can 10-11 so)!");
                continue;
            }
            break;
        }

        try {
            Customer customer = new Customer(id, name, address, phone);
            customerService.addCustomer(customer);
            System.out.println("Them khach hang thanh cong!");
            System.out.println("\nThong tin khach hang vua them:");
            customer.display();
        } catch (Exception e) {
            System.out.println("Loi khi them khach hang: " + e.getMessage());
        }
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
        
        System.out.println("\nKet qua tim kiem:");
        for (Customer customer : customers) {
            System.out.println("----------------------------------------");
            customer.display();
        }
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

    private static void customerPurchase() {
        System.out.println("\n=== MUA HANG ===");
        
        // Kiểm tra danh sách sản phẩm trước
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("Không có sản phẩm nào trong cửa hàng!");
            return;
        }
        
        // Chọn khách hàng
        Customer customer = null;
        while (customer == null) {
            String customerId = getStringInput("Nhap ma khach hang (Enter de them khach hang moi): ");
            if (customerId.isEmpty()) {
                System.out.println("\n=== THEM KHACH HANG MOI ===");
                addCustomer();
                continue;
            }
            
            Optional<Customer> customerOpt = customerService.findById(customerId);
            if (customerOpt.isEmpty()) {
                System.out.println("Không tìm thấy khách hàng! Vui lòng thử lại hoặc thêm mới.");
                continue;
            }
            customer = customerOpt.get();
        }

        // Chọn nhân viên bán hàng
        Employee employee = null;
        while (employee == null) {
            String employeeId = getStringInput("\nNhap ma nhan vien ban hang: ");
            Optional<Employee> employeeOpt = employeeService.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                System.out.println("Không tìm thấy nhân viên! Vui lòng thử lại.");
                continue;
            }
            employee = employeeOpt.get();
        }

        // Tạo hóa đơn mới
        String invoiceId = generateInvoiceId();
        Invoice invoice = new Invoice(invoiceId, customer, employee);

        // Hiển thị danh sách sản phẩm
        System.out.println("\nDanh sach san pham hien co:");
        productService.displayProductsFromFile();

        // Thêm sản phẩm vào hóa đơn
        boolean hasAddedItems = false;
        while (true) {
            try {
                System.out.println("\nThem san pham vao gio hang (nhap ma san pham trong de ket thuc):");
                String productId = getStringInput("Nhap ma san pham: ");
                if (productId.isEmpty()) {
                    if (!hasAddedItems) {
                        System.out.println("Vui lòng thêm ít nhất một sản phẩm!");
                        continue;
                    }
                    break;
                }

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
                System.out.printf("Gia: %,d VND%n", (int)product.getPrice());
                System.out.println("So luong ton: " + product.getQuantity());
                
                int quantity = getValidQuantity(product.getQuantity());
                invoice.addItem(product, quantity);
                hasAddedItems = true;
                System.out.println("Da them san pham vao gio hang!");
                
                // Hiển thị tổng tiền tạm tính
                System.out.printf("Tong tien tam tinh: %,d VND%n", (int)invoice.getTotalAmount());
                
            } catch (Exception e) {
                System.out.println("Loi: " + e.getMessage());
            }
        }

        // Xác nhận và lưu hóa đơn
        if (confirmPurchase(invoice)) {
            try {
                invoiceService.createInvoice(invoice);
                System.out.println("\nMua hang thanh cong!");
                System.out.println("Chi tiet hoa don:");
                invoice.display();
            } catch (Exception e) {
                handlePurchaseError(invoice, e);
            }
        }
    }

    private static int getValidQuantity(int maxQuantity) {
        while (true) {
            int quantity = getIntInput("Nhap so luong mua: ");
            if (quantity <= 0) {
                System.out.println("So luong khong hop le! Vui long nhap lai.");
                continue;
            }
            if (quantity > maxQuantity) {
                System.out.println("So luong vuot qua hang ton kho! Toi da: " + maxQuantity);
                continue;
            }
            return quantity;
        }
    }

    private static boolean confirmPurchase(Invoice invoice) {
        System.out.println("\nHoa don tam tinh:");
        invoice.display();
        String confirm = getStringInput("\nXac nhan mua hang? (Y/N): ");
        return confirm.equalsIgnoreCase("Y");
    }

    private static void handlePurchaseError(Invoice invoice, Exception e) {
        System.out.println("Loi khi tao hoa don: " + e.getMessage());
        // Hoàn trả số lượng sản phẩm
        for (Invoice.InvoiceDetail detail : invoice.getItems()) {
            Product p = detail.getProduct();
            p.setQuantity(p.getQuantity() + detail.getQuantity());
            productService.updateProduct(p);
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
        
        // Kiểm tra và nhập ID
        String id;
        while (true) {
            id = getStringInput("Nhap ma nhan vien (VD: NV001): ");
            if (id.isEmpty()) {
                System.out.println("Ma nhan vien khong duoc de trong!");
                continue;
            }
            if (employeeService.findById(id).isPresent()) {
                System.out.println("Ma nhan vien da ton tai!");
                continue;
            }
            break;
        }

        // Kiểm tra và nhập tên
        String name;
        while (true) {
            name = getStringInput("Nhap ten nhan vien: ");
            if (name.isEmpty()) {
                System.out.println("Ten nhan vien khong duoc de trong!");
                continue;
            }
            break;
        }

        // Kiểm tra và nhập vị trí
        String position;
        while (true) {
            position = getStringInput("Nhap vi tri/chuc vu: ");
            if (position.isEmpty()) {
                System.out.println("Vi tri/chuc vu khong duoc de trong!");
                continue;
            }
            break;
        }

        // Kiểm tra và nhập lương
        double basicSalary;
        while (true) {
            try {
                basicSalary = getDoubleInput("Nhap luong co ban: ");
                if (basicSalary <= 0) {
                    System.out.println("Luong co ban phai lon hon 0!");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Luong khong hop le!");
            }
        }

        try {
            Employee employee = new Employee(id, name, position, basicSalary);
            employeeService.addEmployee(employee);
            System.out.println("Them nhan vien thanh cong!");
            System.out.println("\nThong tin nhan vien vua them:");
            employee.display();
        } catch (Exception e) {
            System.out.println("Loi khi them nhan vien: " + e.getMessage());
        }
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
        String employeeId = getStringInput("Nhap ma nhan vien: ");
        
        Optional<Employee> employeeOpt = employeeService.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            System.out.println("Khong tim thay nhan vien!");
            return;
        }
        
        Employee employee = employeeOpt.get();
        double totalSales = employeeService.calculateTotalSales(employeeId);
        
        System.out.println("\nThong tin doanh so:");
        System.out.println("----------------------------------------");
        System.out.printf("Nhan vien: %s (ID: %s)\n", employee.getName(), employee.getId());
        System.out.printf("Tong doanh so: %,.0fđ\n", totalSales);
        System.out.println("----------------------------------------");
    }

    private static void manageInvoices() {
        while (true) {
            System.out.println("\n===== QUAN LY HOA DON =====");
            System.out.println("1. Tao hoa don moi");
            System.out.println("2. Xem danh sach hoa don");
            System.out.println("3. Tim kiem hoa don theo ma");
            System.out.println("4. Tim kiem hoa don theo ngay");
            System.out.println("0. Quay lai");

            int choice = getIntInput("Nhap lua chon: ");
            switch (choice) {
                case 1 -> customerPurchase();
                case 2 -> displayInvoices();
                case 3 -> findInvoiceById();
                case 4 -> findInvoiceByDate();
                case 0 -> { return; }
                default -> System.out.println("Lua chon khong hop le!");
            }
        }
    }

    private static void findInvoiceByDate() {
        System.out.println("\n=== TIM HOA DON THEO NGAY ===");
        System.out.println("Nhap ngay (dd/MM/yyyy): ");
        Date searchDate = getDateInput();
        
        if (searchDate == null) {
            System.out.println("Ngay khong hop le!");
            return;
        }

        List<Invoice> invoices = invoiceService.findByDate(searchDate);
        if (invoices.isEmpty()) {
            System.out.println("Khong tim thay hoa don nao!");
            return;
        }

        System.out.println("\nDanh sach hoa don tim thay:");
        for (Invoice invoice : invoices) {
            System.out.println("----------------------------------------");
            invoice.display();
        }
    }

    private static Date getDateInput() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        while (true) {
            try {
                String dateStr = scanner.nextLine().trim();
                if (dateStr.isEmpty()) {
                    return null;
                }
                return sdf.parse(dateStr);
            } catch (ParseException e) {
                System.out.println("Ngay khong hop le! Vui long nhap lai (dd/MM/yyyy): ");
            }
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

    private static String generateInvoiceId() {
        // Format: HDyyMMddxxxx (HD + năm 2 số + tháng + ngày + 4 số random)
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        String datePart = sdf.format(new Date());
        int randomNum = (int) (Math.random() * 10000); // Random từ 0-9999
        return String.format("HD%s%04d", datePart, randomNum);
    }
} 