package KasirApp;

import KasirApp.TambahPesanan.Order;
import KasirApp.TambahPesanan.OrderItem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;
import java.io.FileWriter;

public class ProsesCheckout extends JFrame {

    private final Order currentOrder;
    private final TambahPesanan mainFrame;
    private final NumberFormat currencyFormatter =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private JLabel totalLabel;
    private JComboBox<String> paymentMethodComboBox;
    private JTextField amountPaidField;
    private JTextArea receiptArea;
    private JPanel cashPanel;
    private JButton processButton;

    // ================= PAYMENT =================
    static class Payment {
        private String paymentMethod;
        private double amountPaid;
        private final double totalAmount;
        private double change;

        public Payment(double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public boolean calculateChange(String amountText) {
            if (!paymentMethod.equals("cash")) return true;

            amountPaid = Double.parseDouble(amountText);
            if (amountPaid < totalAmount) {
                JOptionPane.showMessageDialog(null,
                        "Jumlah pembayaran kurang!");
                return false;
            }
            change = amountPaid - totalAmount;
            return true;
        }

        public void printReceipt(JTextArea area,
                                 NumberFormat fmt,
                                 Order order) {

            area.setText("");
            area.append("=========== STRUK ===========\n");

            for (OrderItem item : order.getItems()) {
                area.append(String.format(
                        "%s x%d  %s\n",
                        item.getProduct().name(),
                        item.getQuantity(),
                        fmt.format(item.getSubtotal())
                ));
            }

            area.append("-----------------------------\n");
            area.append("TOTAL : " + fmt.format(totalAmount) + "\n");

            if (paymentMethod.equals("cash")) {
                area.append("BAYAR : " + fmt.format(amountPaid) + "\n");
                area.append("KEMBALI : " + fmt.format(change) + "\n");
            }

            area.append("METODE : " + paymentMethod.toUpperCase() + "\n");
            area.append("=============================\n");
        }
    }

    // ================= CONSTRUCTOR =================
    public ProsesCheckout(Order currentOrder, TambahPesanan mainFrame) {
        this.currentOrder = currentOrder;
        this.mainFrame = mainFrame;
        initUI();
    }

    // ================= UI =================
    private void initUI() {
        setTitle("Checkout");
        setSize(500, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        totalLabel = new JLabel(
                "Total: " + currencyFormatter.format(currentOrder.calculateTotal()),
                JLabel.CENTER
        );
        totalLabel.setFont(new Font("Arial", Font.BOLD, 22));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(totalLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        panel.add(new JLabel("Metode Pembayaran"), gbc);

        paymentMethodComboBox = new JComboBox<>(
                new String[]{"cash", "card", "ewallet", "transfer"}
        );
        gbc.gridx = 1;
        panel.add(paymentMethodComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        cashPanel = new JPanel();
        cashPanel.add(new JLabel("Jumlah Bayar:"));
        amountPaidField = new JTextField(10);
        cashPanel.add(amountPaidField);
        panel.add(cashPanel, gbc);

        gbc.gridy++;

        processButton = new JButton("Bayar");
        processButton.setBackground(new Color(46,204,113));
        processButton.setForeground(Color.WHITE);
        panel.add(processButton, gbc);

        gbc.gridy++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        panel.add(new JScrollPane(receiptArea), gbc);

        add(panel);

        paymentMethodComboBox.addActionListener(e -> {
            cashPanel.setVisible(
                    paymentMethodComboBox.getSelectedItem().equals("cash")
            );
        });

        processButton.addActionListener(e -> handlePayment());
    }

    // ================= LOGIC PEMBAYARAN =================
    private void handlePayment() {
        String method = (String) paymentMethodComboBox.getSelectedItem();
        Payment payment = new Payment(currentOrder.calculateTotal());
        payment.setPaymentMethod(method);

        try {
            if (method.equals("cash")) {
                if (amountPaidField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Masukkan jumlah bayar!");
                    return;
                }
                if (!payment.calculateChange(amountPaidField.getText())) return;
            }

            // CETAK STRUK
            payment.printReceipt(receiptArea, currencyFormatter, currentOrder);

            // ================= ONLINE / OFFLINE =================
            if (!OdooService.isOdooOnline()) {
                simpanOffline();
                JOptionPane.showMessageDialog(this,
                        "Odoo OFFLINE. Transaksi disimpan lokal.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                try {
                    PosOrderService.createPosOrder(
                            currentOrder.calculateTotal(),
                            currentOrder.getItems()
                    );
                } catch (Exception ex) {
                    // DEBUG: tampilkan error asli dari Odoo
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Error Odoo:\n" + ex.getMessage(),
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);

                    // fallback offline
                    simpanOffline();
                }
            }

            transitionToFinish();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error pembayaran!\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= OFFLINE SAVE =================
    private void simpanOffline() {
        try (FileWriter fw = new FileWriter("offline_order.json", true)) {
            fw.write(currentOrder.toString() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= FINISH =================
    private void transitionToFinish() {
        processButton.setText("Selesai");
        for (ActionListener a : processButton.getActionListeners())
            processButton.removeActionListener(a);

        processButton.addActionListener(e -> {
            currentOrder.clear();
            mainFrame.updateCartDisplay();
            dispose();
        });
    }

    // ================= OPEN PAGE =================
    public static void showCheckoutPage(Order order, TambahPesanan frame) {
        SwingUtilities.invokeLater(
                () -> new ProsesCheckout(order, frame).setVisible(true)
        );
    }
}