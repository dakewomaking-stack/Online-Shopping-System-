package onlineshopping_last_app;

// Helper class for JComboBox product items, formerly nested in AdminDashboardFrame
public class ProductItem {
    private int id;
    private String name;

    public ProductItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}