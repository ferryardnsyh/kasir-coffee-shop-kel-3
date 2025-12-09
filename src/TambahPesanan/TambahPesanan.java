package TambahPesanan;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TambahPesanan extends JFrame {

    private final List<Product> menu = new ArrayList<>();
    private final Order currentOrder = new Order();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JLabel totalLabel;

    public TambahPesanan() {
        initData();
        initUI();
    }

    private void initData() {
        menu.add(new Product("K01", "Espresso", 18000));
        menu.add(new Product("K02", "Americano", 20000));
        menu.add(new Product("K03", "Cafe Latte", 25000));
        menu.add(new Product("K04", "Cappuccino", 28000));
        menu.add(new Product("M01", "Croissant", 22000));
        menu.add(new Product("M02", "Donat Coklat", 15000));
        menu.add(new Product("M03", "Cheese Cake", 35000));
    }

    private void initUI() {
        setTitle("Sistem Kasir Coffee Shop");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(AppTheme.COLOR_BACKGROUND);

        add(createMenuPanel(), BorderLayout.WEST);
        add(createCartPanel(), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane createMenuPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBackground(AppTheme.COLOR_BACKGROUND);
        panel.setBorder(AppTheme.createTitledBorder("Daftar Menu"));

        for (Product product : menu) {
            JButton btn = createMenuButton(product);
            panel.add(btn);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(240, 0));
        return scrollPane;
    }

    private JPanel createCartPanel() {
        String[] columns = {"Nama Menu", "Harga", "Jumlah Dipesan", "Subtotal"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        cartTable = new JTable(cartTableModel);
        applyTableStyle(cartTable);

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.COLOR_BACKGROUND);
        panel.setBorder(AppTheme.createTitledBorder("Menu yang dipesan"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(20, 25, 20, 25)
        ));

        totalLabel = new JLabel("Total: " + currencyFormatter.format(0));
        totalLabel.setFont(AppTheme.FONT_TITLE);
        totalLabel.setForeground(Color.DARK_GRAY);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttons.setOpaque(false);

        // Penambahan button edit menu
        JButton btnEdit = AppTheme.createFlatButton("Edit Menu", new Color(241, 196, 15), Color.BLACK);
        btnEdit.addActionListener(e -> editSelectedItem());

        JButton btnDelete = AppTheme.createFlatButton("Hapus Menu", AppTheme.COLOR_DANGER, Color.WHITE);
        btnDelete.addActionListener(e -> removeSelectedItem());

        JButton btnCheckout = AppTheme.createFlatButton("Bayar / Checkout", AppTheme.COLOR_SUCCESS, Color.WHITE);
        btnCheckout.setFont(AppTheme.FONT_BOLD);
        btnCheckout.setPreferredSize(new Dimension(180, 45));
        btnCheckout.addActionListener(e -> processCheckout());

        buttons.add(btnEdit);
        buttons.add(btnDelete);
        buttons.add(btnCheckout);

        panel.add(totalLabel, BorderLayout.WEST);
        panel.add(buttons, BorderLayout.EAST);

        return panel;
    }

    private JButton createMenuButton(Product product) {
        JButton btn = AppTheme.createFlatButton(product.name(), Color.WHITE, Color.DARK_GRAY);
        btn.setFont(AppTheme.FONT_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setToolTipText(currencyFormatter.format(product.price()));
        btn.addActionListener(e -> addItemToCart(product));
        return btn;
    }

    private void applyTableStyle(JTable table) {
        table.setFont(AppTheme.FONT_REGULAR);
        table.setRowHeight(35);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(225, 235, 245));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(AppTheme.FONT_BOLD);
        header.setBackground(AppTheme.COLOR_PRIMARY);
        header.setForeground(Color.BLACK);
        header.setOpaque(true);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

    private void addItemToCart(Product product) {
        currentOrder.addProduct(product);
        updateCartDisplay();
    }

    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Pilih item di keranjang yang ingin dihapus.");
            return;
        }
        currentOrder.removeItem(selectedRow);
        updateCartDisplay();
    }

    private void editSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Pilih item yang ingin diedit.");
            return;
        }

        OrderItem selectedItem = currentOrder.getItems().get(selectedRow);

        String input = JOptionPane.showInputDialog(
                this,
                "Masukkan jumlah baru untuk \"" + selectedItem.getProduct().name() + "\":",
                selectedItem.getQuantity()
        );

        if (input == null) return;

        try {
            int newQty = Integer.parseInt(input);

            if (newQty <= 0) {
                showWarning("Jumlah harus lebih dari 0.");
                return;
            }

            selectedItem.setQuantity(newQty);
            updateCartDisplay();

        } catch (NumberFormatException e) {
            showError("Input tidak valid! Masukkan angka.");
        }
    }

    private void processCheckout() {
        if (currentOrder.isEmpty()) {
            showError("Keranjang masih kosong!");
            return;
        }

        // Arahkan ke halaman ProsesCheckout dan berikan referensi ke TambahPesanan
        ProsesCheckout.showCheckoutPage(currentOrder, this);
    }

    public void updateCartDisplay() {
        cartTableModel.setRowCount(0);
        for (OrderItem item : currentOrder.getItems()) {
            cartTableModel.addRow(new Object[]{
                    item.getProduct().name(),
                    currencyFormatter.format(item.getProduct().price()),
                    item.getQuantity(),
                    currencyFormatter.format(item.getSubtotal())
            });
        }
        totalLabel.setText("Total: " + currencyFormatter.format(currentOrder.calculateTotal()));
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new TambahPesanan().setVisible(true));
    }

    static class AppTheme {
        public static final Color COLOR_PRIMARY = new Color(52, 152, 219);
        public static final Color COLOR_SUCCESS = new Color(46, 204, 113);
        public static final Color COLOR_DANGER = new Color(231, 76, 60);
        public static final Color COLOR_BACKGROUND = new Color(236, 240, 241);

        public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
        public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
        public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);

        public static JButton createFlatButton(String text, Color bg, Color fg) {
            JButton btn = new JButton(text);
            btn.setFont(FONT_REGULAR);
            btn.setBackground(bg);
            btn.setForeground(fg);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(new CompoundBorder(
                    new LineBorder(bg.darker(), 1),
                    new EmptyBorder(8, 15, 8, 15)
            ));
            return btn;
        }

        public static CompoundBorder createTitledBorder(String title) {
            return new CompoundBorder(
                    BorderFactory.createTitledBorder(null, title, 0, 0, FONT_BOLD, Color.DARK_GRAY),
                    new EmptyBorder(10, 10, 10, 10)
            );
        }
    }

    static record Product(String code, String name, double price) {}

    public static class OrderItem { // Dibuat public static agar bisa diakses ProsesCheckout
        private final Product product;
        private int quantity;

        public OrderItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public void addQuantity(int amount) { this.quantity += amount; }

        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getSubtotal() { return product.price() * quantity; }

        public Product getProduct() { return product; }

        public int getQuantity() { return quantity; }
    }

    public static class Order { // Dibuat public static agar bisa diakses ProsesCheckout
        private final List<OrderItem> items = new ArrayList<>();

        public void addProduct(Product product) {
            OrderItem existing = findItem(product);
            if (existing != null) {
                existing.addQuantity(1);
            } else {
                items.add(new OrderItem(product, 1));
            }
        }

        public void removeItem(int index) {
            if (index >= 0 && index < items.size()) items.remove(index);
        }

        public double calculateTotal() {
            return items.stream().mapToDouble(OrderItem::getSubtotal).sum();
        }

        public void clear() { items.clear(); }

        public boolean isEmpty() { return items.isEmpty(); }

        public List<OrderItem> getItems() { return items; }

        private OrderItem findItem(Product product) {
            return items.stream()
                    .filter(i -> i.getProduct().equals(product))
                    .findFirst().orElse(null);
        }
    }
}