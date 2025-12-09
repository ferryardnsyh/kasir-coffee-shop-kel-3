package TambahPesanan;

import TambahPesanan.TambahPesanan.Order;
import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class ProsesCheckout extends JFrame {

    private final Order currentOrder;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private final TambahPesanan mainFrame; // Menyimpan referensi ke frame utama (TambahPesanan)

    // Komponen UI
    private JLabel totalLabel;
    private JTextField amountPaidField;
    private JButton payButton;
    private JRadioButton cashRadioButton;
    private JRadioButton debitRadioButton;
    private JPanel cashPanel; // Panel untuk cash payment fields

    // Constructor
    public ProsesCheckout(Order currentOrder, TambahPesanan mainFrame) {
        this.currentOrder = currentOrder;
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setTitle("Proses Checkout");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        // Panel Total
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalLabel = new JLabel("Total: " + currencyFormatter.format(currentOrder.calculateTotal()));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(46, 204, 113)); // Green color for total
        totalPanel.add(totalLabel);

        // Panel Metode Pembayaran
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cashRadioButton = new JRadioButton("Uang Cash");
        debitRadioButton = new JRadioButton("Kartu Debit");
        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cashRadioButton);
        paymentGroup.add(debitRadioButton);
        paymentPanel.add(cashRadioButton);
        paymentPanel.add(debitRadioButton);

        // Panel Cash Payment Fields
        cashPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        amountPaidField = new JTextField(15);
        amountPaidField.setEnabled(false);  // Disable until Cash is selected
        cashPanel.add(new JLabel("Jumlah yang diberikan: "));
        cashPanel.add(amountPaidField);

        // Panel Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        payButton = new JButton("Bayar");
        payButton.setFont(new Font("Arial", Font.BOLD, 16));
        payButton.setBackground(new Color(46, 204, 113)); // Green button
        payButton.setForeground(Color.WHITE);
        payButton.setPreferredSize(new Dimension(150, 40));
        payButton.setFocusPainted(false);
        payButton.addActionListener(e -> handlePayment());
        buttonPanel.add(payButton);

        // Menambahkan komponen ke dalam window
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(236, 240, 241)); // Light background color
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(totalPanel);
        mainPanel.add(Box.createVerticalStrut(10)); // Spacer
        mainPanel.add(paymentPanel);
        mainPanel.add(Box.createVerticalStrut(20)); // Spacer
        mainPanel.add(cashPanel);
        mainPanel.add(Box.createVerticalStrut(30)); // Spacer
        mainPanel.add(buttonPanel);

        add(mainPanel, BorderLayout.CENTER);

        // Menambahkan Listener untuk metode pembayaran
        cashRadioButton.addActionListener(e -> {
            amountPaidField.setEnabled(true);
            cashPanel.setVisible(true); // Show field for cash payment
        });
        debitRadioButton.addActionListener(e -> {
            amountPaidField.setEnabled(false);
            cashPanel.setVisible(false); // Hide field for debit payment
        });
    }

    private void handlePayment() {
        // Jika metode pembayaran menggunakan uang cash
        if (cashRadioButton.isSelected()) {
            try {
                // Memeriksa apakah field amountPaidField diisi
                if (amountPaidField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Masukkan jumlah uang yang diberikan!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double amountPaid = Double.parseDouble(amountPaidField.getText());
                double total = currentOrder.calculateTotal();
                if (amountPaid < total) {
                    JOptionPane.showMessageDialog(this, "Jumlah yang dibayar kurang dari total.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    double change = amountPaid - total;
                    JOptionPane.showMessageDialog(this, "Pembayaran berhasil! Kembalian: " + currencyFormatter.format(change), "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    // Mengosongkan keranjang setelah transaksi berhasil
                    currentOrder.clear();
                    mainFrame.updateCartDisplay(); // Memperbarui tampilan keranjang pada frame utama
                    this.dispose();  // Tutup halaman checkout setelah pembayaran
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Masukkan jumlah yang valid!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } 
        // Jika metode pembayaran menggunakan kartu debit
        else if (debitRadioButton.isSelected()) {
            JOptionPane.showMessageDialog(this, "Pembayaran dengan kartu debit berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            // Mengosongkan keranjang setelah transaksi berhasil
            currentOrder.clear();
            mainFrame.updateCartDisplay(); // Memperbarui tampilan keranjang pada frame utama
            this.dispose();  // Tutup halaman checkout setelah pembayaran
        } else {
            JOptionPane.showMessageDialog(this, "Pilih metode pembayaran.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Menampilkan halaman checkout
    public static void showCheckoutPage(Order currentOrder, TambahPesanan mainFrame) {
        SwingUtilities.invokeLater(() -> new ProsesCheckout(currentOrder, mainFrame).setVisible(true));
    }
}
