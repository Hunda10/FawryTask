import java.util.Date;

// الفئة الرئيسية للتجربة
public class ECommerceSystem {
    public static void main(String[] args) {
        // إنشاء منتجات
        Product cheese = new ExpirableShippableProduct("Cheese", 100, 10,
                new Date(System.currentTimeMillis() + 86400000 * 7), // صلاحية أسبوع
                0.4);
        Product biscuits = new ExpirableShippableProduct("Biscuits", 150, 5,
                new Date(System.currentTimeMillis() + 86400000 * 14), // صلاحية أسبوعين
                0.7);
        Product tv = new NonExpirableShippableProduct("TV", 10000, 3, 15.0);
        Product scratchCard = new NonExpirableNonShippableProduct("Mobile scratch card", 50, 100);

        // إنشاء عميل
        Customer customer = new Customer("Ahmed", 2000);

        // إنشاء عربة تسوق
        ShoppingCart cart = new ShoppingCart();

        // إضافة منتجات للعربة
        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(scratchCard, 1);

        try {
            // عملية الدفع
            CheckoutService.checkout(customer, cart);
        } catch (Exception e) {
            System.out.println("Error during checkout: " + e.getMessage());
        }

        // اختبار حالة عدم كفاية الرصيد
        Customer poorCustomer = new Customer("Poor", 100);
        ShoppingCart expensiveCart = new ShoppingCart();
        expensiveCart.add(tv, 1);

        try {
            CheckoutService.checkout(poorCustomer, expensiveCart);
        } catch (Exception e) {
            System.out.println("Expected error: " + e.getMessage());
        }
    }
}
