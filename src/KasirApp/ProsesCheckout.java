package KasirApp;

import KasirApp.TambahPesanan.Order;
import KasirApp.TambahPesanan.OrderItem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.File; // Impor kelas File untuk membuka PDF

public class ProsesCheckout extends JFrame {

    private final Order currentOrder;
    private final TambahPesanan mainFrame;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    
    // UI Components
    private JLabel totalLabel;
    private JComboBox<String> paymentMethodComboBox;
    private JTextField amountPaidField;
    private JTextArea receiptArea;
    private JPanel cashPanel;
    private JButton processButton; // Dideklarasikan sebagai field
    private JButton printPDFButton; // Tombol untuk mencetak PDF

    // Kelas untuk Mengelola Pembayaran
    static class Payment {
        private String paymentMethod;
        private double amountPaid;
        private final double totalAmount;
        private double change;

        public Payment(double totalAmount) {
            this.totalAmount = totalAmount;
            this.change = 0;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public boolean calculateChange(String amountText) throws NumberFormatException {
            if (!this.paymentMethod.equals("cash")) return true;

            this.amountPaid = Double.parseDouble(amountText);
            if (this.amountPaid < this.totalAmount) {
                JOptionPane.showMessageDialog(null, "Jumlah yang dibayar tidak cukup!");
                return false;
            }
            this.change = this.amountPaid - this.totalAmount;
            return true;
        }

        // printReceipt()
        public void printReceipt(JTextArea receiptArea, NumberFormat formatter, Order order) {
            receiptArea.setText(""); 
            receiptArea.append("====================================\n");
            receiptArea.append("         STRUK PEMBAYARAN          \n");
            receiptArea.append("====================================\n");

            // Menambahkan Tanggal dan Waktu
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            String currentDateTime = LocalDateTime.now().format(dtf); 
            receiptArea.append("Tanggal & Waktu: " + currentDateTime + "\n");

            // Detail Item
            for(OrderItem item : order.getItems()) {
                receiptArea.append(String.format("%-20s %3d x %s\n", 
                item.getProduct().name(), 
                item.getQuantity(), 
                formatter.format(item.getProduct().price())
            ));
        }

        receiptArea.append("------------------------------------\n");
        receiptArea.append(String.format("%-20s %s\n", "Total Pembayaran:", formatter.format(this.totalAmount)));
    
        if (this.paymentMethod.equals("cash")) {
            receiptArea.append(String.format("%-20s %s\n", "Jumlah yang Dibayar:", formatter.format(this.amountPaid)));
            receiptArea.append(String.format("%-20s %s\n", "Kembalian:", formatter.format(this.change)));
        } 
    
        receiptArea.append("Metode Pembayaran: " + this.paymentMethod.toUpperCase() + "\n");
        receiptArea.append("====================================\n");
        }

        
        public String getSuccessMessage(NumberFormat formatter) {
            if (this.paymentMethod.equals("cash")) {
                return "Pembayaran Berhasil! Kembalian Anda: " + formatter.format(this.change);
            } else {
                return "Pembayaran dengan " + this.paymentMethod.toUpperCase() + " Berhasil!";
            }
        }
    }

    public ProsesCheckout(Order currentOrder, TambahPesanan mainFrame) {
        this.currentOrder = currentOrder;
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setTitle("Proses Checkout");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Atur ukuran minimal
        setMinimumSize(new Dimension(500, 550)); 

        JPanel mainPanel = new JPanel();
        // Menggunakan GridBagLayout untuk responsivitas yang superior
        mainPanel.setLayout(new GridBagLayout()); 
        mainPanel.setBackground(new Color(250, 250, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Default untuk sebagian besar komponen

        // Total (Baris 0)
        totalLabel = new JLabel("Total: " + currencyFormatter.format(currentOrder.calculateTotal()), JLabel.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 24));
        totalLabel.setForeground(new Color(46, 204, 113));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(totalLabel, gbc);

        // Metode Pembayaran (Baris 1)
        JLabel methodLabel = new JLabel("Pilih Metode Pembayaran:");
        methodLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(methodLabel, gbc);

        String[] paymentMethods = {"cash", "card", "ewallet", "transfer"};
        paymentMethodComboBox = new JComboBox<>(paymentMethods);
        paymentMethodComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(paymentMethodComboBox, gbc);

        // Input Cash (Baris 2)
        cashPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cashPanel.setOpaque(false);
        JLabel amountLabel = new JLabel("Jumlah Uang Diberikan (Rp): ");
        amountPaidField = new JTextField(10);
        cashPanel.add(amountLabel);
        cashPanel.add(amountPaidField);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 1.0;
        mainPanel.add(cashPanel, gbc);
        cashPanel.setVisible(paymentMethodComboBox.getSelectedItem().equals("cash"));

        // Tombol Proses Pembayaran (Baris 3)
        processButton = new JButton("Bayar Sekarang");
        processButton.setFont(new Font("Arial", Font.BOLD, 16));
        processButton.setBackground(new Color(46, 204, 113));
        processButton.setForeground(Color.WHITE);
        processButton.setFocusPainted(false);
        processButton.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0; // Tombol mengisi horizontal
        mainPanel.add(processButton, gbc);

        // Tombol Cetak PDF (Baris 4)
        printPDFButton = new JButton("Cetak Struk PDF");
        printPDFButton.setFont(new Font("Arial", Font.BOLD, 16));
        printPDFButton.setBackground(new Color(52, 152, 219));
        printPDFButton.setForeground(Color.WHITE);
        printPDFButton.setFocusPainted(false);
        printPDFButton.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 1.0;
        mainPanel.add(printPDFButton, gbc);

        // Area Struk (Baris 5)
        receiptArea = new JTextArea(10, 30);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        receiptArea.setEditable(false);
        receiptArea.setBackground(new Color(240, 240, 240));
        receiptArea.setBorder(BorderFactory.createTitledBorder("Struk Transaksi"));
        
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; 
        gbc.weightx = 1.0; // Melebar secara horizontal saat frame di-resize
        gbc.weighty = 1.0; // PENTING: Melebar secara vertikal saat frame di-resize
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPane, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Listener untuk dropdown metode pembayaran
        paymentMethodComboBox.addActionListener(e -> {
            boolean isCash = paymentMethodComboBox.getSelectedItem().equals("cash");
            cashPanel.setVisible(isCash);
            pack(); 
        });

        // Action Listener untuk tombol proses pembayaran (Bayar)
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePayment();
            }
        });

        // Action Listener untuk tombol Cetak PDF
        printPDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printReceiptToPDF();
            }
        });

        pack(); 
    }

    private void printReceiptToPDF() {
    String receiptText = receiptArea.getText(); // Ambil teks struk dari JTextArea
    String filePath = "struk_pembayaran.pdf"; // Tentukan lokasi dan nama file PDF yang akan disimpan

    try {
        // Membuat file PDF
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();
        document.add(new Paragraph(receiptText)); // Tambahkan teks struk ke dalam PDF
        document.close();

        // Membuka file PDF menggunakan aplikasi pembaca PDF default
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(new File(filePath));  // Membuka file PDF setelah dicetak
        }

        // Tampilkan pesan sukses
        JOptionPane.showMessageDialog(this, "Struk berhasil dicetak dan dibuka!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat mencetak struk.", "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

    private void handlePayment() {
        String paymentMethod = (String) paymentMethodComboBox.getSelectedItem();
        double totalAmount = currentOrder.calculateTotal();
        Payment payment = new Payment(totalAmount);
        payment.setPaymentMethod(paymentMethod);
        
        try {
            if (paymentMethod.equals("cash")) {
                if (amountPaidField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Masukkan jumlah uang yang diberikan!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!payment.calculateChange(amountPaidField.getText())) {
                    return; 
                }
            }
            
            // 1. Cetak Struk ke JTextArea
            payment.printReceipt(receiptArea, currencyFormatter, currentOrder);
            
            // 2. Tampilkan pesan sukses
            String successMessage = payment.getSuccessMessage(currencyFormatter);
            JOptionPane.showMessageDialog(this, successMessage, "Pembayaran Berhasil", JOptionPane.INFORMATION_MESSAGE);

            // 3. Ubah status jendela ke mode Selesai (Jendela tetap terbuka)
            transitionToCompletionState();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Masukkan jumlah yang valid (angka)!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void transitionToCompletionState() {
        // 1. Nonaktifkan kontrol pembayaran
        paymentMethodComboBox.setEnabled(false);
        amountPaidField.setEnabled(false);
        
        // 2. Ubah teks tombol dan warna
        processButton.setText("Selesai / Tutup");
        processButton.setBackground(new Color(52, 152, 219)); 

        // 3. Hapus listener lama dan tambahkan listener baru
        for(ActionListener al : processButton.getActionListeners()) {
            processButton.removeActionListener(al);
        }

        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Lakukan pembersihan dan tutup jendela
                currentOrder.clear();
                mainFrame.updateCartDisplay(); 
                dispose(); 
            }
        });
    }

    public static void showCheckoutPage(Order currentOrder, TambahPesanan mainFrame) {
        SwingUtilities.invokeLater(() -> new ProsesCheckout(currentOrder, mainFrame).setVisible(true));
    }
}
