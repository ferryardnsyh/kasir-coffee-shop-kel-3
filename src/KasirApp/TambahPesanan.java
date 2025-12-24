package KasirApp;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TambahPesanan extends JFrame {

    private final List<Product> menu = new ArrayList<>();
    private final Order currentOrder = new Order();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    
    // [MODIFIKASI 1] Ubah format jadi Tanggal Lengkap + Jam
    // Contoh output: 24-12-2025 14:30
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JLabel totalLabel;
    private JLabel dateLabel; 

    public TambahPesanan() {
        initData();
        initUI();
        startClock();
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
        setSize(1100, 700); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(AppTheme.COLOR_BACKGROUND);

        add(createMenuPanel(), BorderLayout.WEST);
        add(createCartPanel(), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss", new Locale("id", "ID"));
            if(dateLabel != null) dateLabel.setText(sdf.format(new Date()));
        });
        timer.start();
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
        scrollPane.setPreferredSize(new Dimension(220, 0));
        return scrollPane;
    }

    private JPanel createCartPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10)); 
        mainPanel.setBackground(AppTheme.COLOR_BACKGROUND);
        mainPanel.setBorder(AppTheme.createTitledBorder("Daftar Pesanan"));

        // Panel Jam Digital (Header)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        dateLabel = new JLabel("Loading date...");
        dateLabel.setFont(AppTheme.FONT_BOLD);
        dateLabel.setForeground(Color.DARK_GRAY);
        headerPanel.add(new JLabel("Waktu Sekarang: "));
        headerPanel.add(dateLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // [MODIFIKASI 2] Judul Kolom diubah
        String[] columns = {"Waktu Pesan", "Nama Menu", "Harga", "Qty", "Subtotal", "Status"};
        
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        cartTable = new JTable(cartTableModel);
        applyTableStyle(cartTable);

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
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
        
        JButton btnStatus = AppTheme.createFlatButton("Ubah Status", Color.ORANGE, Color.BLACK);
        btnStatus.addActionListener(e -> changeStatusItem());

        JButton btnEdit = AppTheme.createFlatButton("Edit Qty", new Color(52, 152, 219), Color.WHITE);
        btnEdit.addActionListener(e -> editSelectedItem());

        JButton btnDelete = AppTheme.createFlatButton("Hapus", AppTheme.COLOR_DANGER, Color.WHITE);
        btnDelete.addActionListener(e -> removeSelectedItem());

        JButton btnCheckout = AppTheme.createFlatButton("Bayar", AppTheme.COLOR_SUCCESS, Color.WHITE);
        btnCheckout.setFont(AppTheme.FONT_BOLD);
        btnCheckout.setPreferredSize(new Dimension(120, 45));
        btnCheckout.addActionListener(e -> processCheckout());

        buttons.add(btnStatus);
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
        table.setRowHeight(40); 
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(225, 235, 245));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(AppTheme.FONT_BOLD);
        header.setBackground(AppTheme.COLOR_PRIMARY);
        header.setForeground(Color.BLACK);
        header.setOpaque(true);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        
        // [MODIFIKASI 3] Atur ulang lebar kolom
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(130); // Diperlebar untuk Tanggal & Jam
        cm.getColumn(1).setPreferredWidth(200); 
        cm.getColumn(2).setPreferredWidth(100); 
        cm.getColumn(3).setPreferredWidth(50);  
        cm.getColumn(4).setPreferredWidth(120); 
        cm.getColumn(5).setPreferredWidth(100); 
    }

    private void addItemToCart(Product product) {
        currentOrder.addProduct(product);
        updateCartDisplay();
    }

    public void updateCartDisplay() {
        cartTableModel.setRowCount(0);
        for (OrderItem item : currentOrder.getItems()) {
            cartTableModel.addRow(new Object[]{
                    // [MODIFIKASI 4] Gunakan format baru (Tanggal + Jam)
                    item.getTimeAdded().format(dateTimeFormatter),
                    
                    item.getProduct().name(),
                    currencyFormatter.format(item.getProduct().price()),
                    item.getQuantity(),
                    currencyFormatter.format(item.getSubtotal()),
                    item.getStatus()
            });
        }
        totalLabel.setText("Total: " + currencyFormatter.format(currentOrder.calculateTotal()));
    }

    private void removeSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Pilih item yang ingin dihapus.");
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
        String input = JOptionPane.showInputDialog(this, "Masukkan jumlah baru:", selectedItem.getQuantity());
        if (input == null) return;
        try {
            int newQty = Integer.parseInt(input);
            if (newQty <= 0) return;
            selectedItem.setQuantity(newQty);
            updateCartDisplay();
        } catch (NumberFormatException e) {
            showError("Input angka saja!");
        }
    }
    
    private void changeStatusItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Pilih item untuk ubah status.");
            return;
        }
        OrderItem selectedItem = currentOrder.getItems().get(selectedRow);
        
        String[] options = {"Menunggu", "Sedang Dibuat", "Selesai"};
        String newStatus = (String) JOptionPane.showInputDialog(
                this, "Pilih status baru:", "Ubah Status",
                JOptionPane.QUESTION_MESSAGE, null, options, selectedItem.getStatus());
                
        if (newStatus != null) {
            selectedItem.setStatus(newStatus);
            updateCartDisplay();
        }
    }

    private void processCheckout() {
        if (currentOrder.isEmpty()) {
            showError("Keranjang kosong!");
            return;
        }
        currentOrder.setAllStatus("Dipesan");
        updateCartDisplay();
        ProsesCheckout.showCheckoutPage(currentOrder, this);
    }

    private void showWarning(String msg) { JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TambahPesanan().setVisible(true));
    }

    // --- CLASS PENDUKUNG ---

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
            btn.setBorder(new CompoundBorder(new LineBorder(bg.darker(), 1), new EmptyBorder(8, 15, 8, 15)));
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

    public static class OrderItem {
        private final Product product;
        private int quantity;
        private LocalDateTime timeAdded; 
        private String status;

        public OrderItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
            this.timeAdded = LocalDateTime.now(); 
            this.status = "Menunggu";             
        }

        public void addQuantity(int amount) { this.quantity += amount; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getSubtotal() { return product.price() * quantity; }
        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        
        public LocalDateTime getTimeAdded() { return timeAdded; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class Order {
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
        
        public void setAllStatus(String newStatus) {
            for(OrderItem item : items) {
                item.setStatus(newStatus);
            }
        }

        private OrderItem findItem(Product product) {
            return items.stream().filter(i -> i.getProduct().equals(product)).findFirst().orElse(null);
        }
    }
}