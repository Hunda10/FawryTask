import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// واجهة للعناصر القابلة للشحن
interface Shippable {
    String getName();
    double getWeight();
}

// واجهة للمنتجات القابلة للانتهاء
interface Expirable {
    boolean isExpired();
    Date getExpiryDate();
}

// فئة أساسية للمنتج
abstract class Product {
    private String name;
    private double price;
    private int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public abstract boolean requiresShipping();
}

// منتجات لا تنتهي صلاحيتها ولا تحتاج شحنًا
class NonExpirableNonShippableProduct extends Product {
    public NonExpirableNonShippableProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }

    @Override
    public boolean requiresShipping() {
        return false;
    }
}

// منتجات تنتهي صلاحيتها وتحتاج شحنًا
class ExpirableShippableProduct extends Product implements Expirable, Shippable {
    private Date expiryDate;
    private double weight;

    public ExpirableShippableProduct(String name, double price, int quantity, Date expiryDate, double weight) {
        super(name, price, quantity);
        this.expiryDate = expiryDate;
        this.weight = weight;
    }

    @Override
    public boolean isExpired() {
        return new Date().after(expiryDate);
    }

    @Override
    public Date getExpiryDate() {
        return expiryDate;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public boolean requiresShipping() {
        return true;
    }
}

// منتجات لا تنتهي صلاحيتها ولكن تحتاج شحنًا
class NonExpirableShippableProduct extends Product implements Shippable {
    private double weight;

    public NonExpirableShippableProduct(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public boolean requiresShipping() {
        return true;
    }
}

// فئة عنصر العربة
class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}

// فئة عربة التسوق
class ShoppingCart {
    private List<CartItem> items;

    public ShoppingCart() {
        this.items = new ArrayList<>();
    }

    public void add(Product product, int quantity) {
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock for " + product.getName());
        }

        // التحقق من انتهاء الصلاحية للمنتجات القابلة للانتهاء
        if (product instanceof Expirable) {
            Expirable expirableProduct = (Expirable) product;
            if (expirableProduct.isExpired()) {
                throw new IllegalArgumentException("Product " + product.getName() + " is expired");
            }
        }

        // البحث عن المنتج في العربة
        for (CartItem item : items) {
            if (item.getProduct().equals(product)) {
                item = new CartItem(product, item.getQuantity() + quantity);
                return;
            }
        }

        // إضافة منتج جديد إلى العربة
        items.add(new CartItem(product, quantity));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public double calculateSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public List<Shippable> getShippableItems() {
        List<Shippable> shippableItems = new ArrayList<>();
        for (CartItem item : items) {
            if (item.getProduct() instanceof Shippable) {
                for (int i = 0; i < item.getQuantity(); i++) {
                    shippableItems.add((Shippable) item.getProduct());
                }
            }
        }
        return shippableItems;
    }
}

// فئة العميل
class Customer {
    private String name;
    private double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void deductBalance(double amount) {
        if (balance < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        balance -= amount;
    }
}

// خدمة الشحن
class ShippingService {
    public static void shipItems(List<Shippable> items) {
        if (items.isEmpty()) return;

        System.out.println("** Shipment notice **");
        double totalWeight = 0;

        for (Shippable item : items) {
            System.out.printf("%dx %s    %.0fg%n",
                    countOccurrences(items, item),
                    item.getName(),
                    item.getWeight() * 1000);
            totalWeight += item.getWeight();
        }

        System.out.printf("Total package weight %.1fkg%n%n", totalWeight);
    }

    private static int countOccurrences(List<Shippable> items, Shippable target) {
        int count = 0;
        for (Shippable item : items) {
            if (item.equals(target)) count++;
        }
        return count;
    }

    public static double calculateShippingFees(List<Shippable> items) {
        if (items.isEmpty()) return 0;
        double totalWeight = items.stream().mapToDouble(Shippable::getWeight).sum();
        return Math.max(10, totalWeight * 5); // 5 جنيه لكل كيلوجرام، بحد أدنى 10 جنيه
    }
}

// فئة الدفع
class CheckoutService {
    public static void checkout(Customer customer, ShoppingCart cart) {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cannot checkout with empty cart");
        }

        // التحقق من توفر جميع المنتجات وعدم انتهاء صلاحيتها
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Product " + product.getName() + " is out of stock");
            }

            if (product instanceof Expirable && ((Expirable) product).isExpired()) {
                throw new IllegalStateException("Product " + product.getName() + " is expired");
            }
        }

        double subtotal = cart.calculateSubtotal();
        List<Shippable> shippableItems = cart.getShippableItems();
        double shippingFees = ShippingService.calculateShippingFees(shippableItems);
        double totalAmount = subtotal + shippingFees;

        if (customer.getBalance() < totalAmount) {
            throw new IllegalStateException("Insufficient customer balance");
        }

        // خصم المبلغ من رصيد العميل
        customer.deductBalance(totalAmount);

        // تحديث كمية المنتجات
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
        }

        // طباعة الفاتورة
        printReceipt(cart, subtotal, shippingFees, totalAmount, customer);

        // إرسال العناصر القابلة للشحن
        ShippingService.shipItems(shippableItems);
    }

    private static void printReceipt(ShoppingCart cart, double subtotal, double shippingFees, double totalAmount, Customer customer) {
        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.printf("%dx %s    %.0f%n",
                    item.getQuantity(),
                    item.getProduct().getName(),
                    item.getTotalPrice());
        }
        System.out.println("---");
        System.out.printf("Subtotal    %.0f%n", subtotal);
        System.out.printf("Shipping    %.0f%n", shippingFees);
        System.out.printf("Amount    %.0f%n", totalAmount);
        System.out.printf("Remaining balance: %.0f%n%n", customer.getBalance());
    }
}

