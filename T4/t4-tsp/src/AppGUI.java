import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AppGUI extends JFrame {

    private File selectedFile = null;
    private JLabel lblSelectedFile;
    private JCheckBox chkNearest;
    private JCheckBox chkSmallest;
    private JButton btnRun;

    public AppGUI() {
        setTitle("Caixeiro Viajante - Painel de Controle");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza
        setResizable(false);
        
        // Define cor de fundo e layout
        getContentPane().setBackground(new Color(245, 247, 250));
        setLayout(new BorderLayout());

        // Cabeçalho
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel headerLabel = new JLabel("Visualizador TSP T4");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Painel Central
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Seção Arquivo
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.setOpaque(false);
        JButton btnSelectFile = createStyledButton("Selecionar Arquivo", new Color(52, 73, 94));
        lblSelectedFile = new JLabel(" Nenhum arquivo...");
        lblSelectedFile.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSelectedFile.setForeground(Color.DARK_GRAY);
        
        filePanel.add(btnSelectFile);
        filePanel.add(lblSelectedFile);
        
        centerPanel.add(filePanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Espaçamento

        // Seção Heurísticas
        JPanel heuristicsPanel = new JPanel();
        heuristicsPanel.setLayout(new BoxLayout(heuristicsPanel, BoxLayout.Y_AXIS));
        heuristicsPanel.setBackground(Color.WHITE);
        heuristicsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblHeuristics = new JLabel("Escolha as Heurísticas para Visualizar:");
        lblHeuristics.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        chkNearest = new JCheckBox("Nearest Insertion (Inserção Mais Próxima)");
        chkNearest.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkNearest.setOpaque(false);
        chkNearest.setSelected(true);
        
        chkSmallest = new JCheckBox("Smallest Insertion (Menor Aumento)");
        chkSmallest.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkSmallest.setOpaque(false);
        chkSmallest.setSelected(true);

        heuristicsPanel.add(lblHeuristics);
        heuristicsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        heuristicsPanel.add(chkNearest);
        heuristicsPanel.add(chkSmallest);
        
        centerPanel.add(heuristicsPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Rodapé (Botão de Execução)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        btnRun = createStyledButton("GERAR VISUALIZAÇÃO", new Color(39, 174, 96));
        btnRun.setPreferredSize(new Dimension(300, 45));
        btnRun.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bottomPanel.add(btnRun);
        add(bottomPanel, BorderLayout.SOUTH);

        // Ações
        btnSelectFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")).getParentFile());
                int result = fileChooser.showOpenDialog(AppGUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    lblSelectedFile.setText(" " + selectedFile.getName());
                    lblSelectedFile.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    lblSelectedFile.setForeground(new Color(41, 128, 185));
                }
            }
        });

        btnRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile == null) {
                    JOptionPane.showMessageDialog(AppGUI.this, 
                        "Por favor, selecione um arquivo de base (ex: usa13509.txt) primeiro!", 
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                boolean doNearest = chkNearest.isSelected();
                boolean doSmallest = chkSmallest.isSelected();

                if (!doNearest && !doSmallest) {
                    JOptionPane.showMessageDialog(AppGUI.this, 
                        "Selecione no mínimo uma heurística para desenhar.", 
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Desabilita o botão para mostrar que está processando (útil para o usa13509.txt que é pesado)
                btnRun.setEnabled(false);
                btnRun.setText("PROCESSANDO... AGUARDE");

                // Roda em uma thread separada para não travar a interface
                new Thread(() -> {
                    processAndDraw(selectedFile, doNearest, doSmallest);
                    SwingUtilities.invokeLater(() -> {
                        btnRun.setEnabled(true);
                        btnRun.setText("GERAR VISUALIZAÇÃO");
                    });
                }).start();
            }
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? bgColor : Color.GRAY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void processAndDraw(File file, boolean doNearest, boolean doSmallest) {
        try {
            In in = new In(file.getAbsolutePath());
            int width = in.readInt();
            int height = in.readInt();

            List<Point> points = new ArrayList<>();
            Tour nearest = new Tour();
            Tour smallest = new Tour();

            while (!in.isEmpty()) {
                Point p = new Point(in.readDouble(), in.readDouble());
                points.add(p);
                
                if (doNearest) {
                    nearest.insertNearest(p);
                }
                if (doSmallest) {
                    smallest.insertSmallest(p);
                }
            }

            // Exibir as rotas criadas no Visualizador Web Moderno (HTML5)
            String nearestStr = doNearest ? tourToStringToJson(nearest.toString()) : "[]";
            String smallestStr = doSmallest ? tourToStringToJson(smallest.toString()) : "[]";
            String pointsJson = pointsToJson(points);
            
            String dataJs = "var tspData = {\n" +
              "  width: " + width + ",\n" +
              "  height: " + height + ",\n" +
              "  points: " + pointsJson + ",\n" +
              "  nearestTour: " + nearestStr + ",\n" +
              "  nearestLength: " + (doNearest ? nearest.length() : 0) + ",\n" +
              "  smallestTour: " + smallestStr + ",\n" +
              "  smallestLength: " + (doSmallest ? smallest.length() : 0) + "\n" +
            "};";

            File frontendDir = new File(System.getProperty("user.dir"), "../frontend");
            if (!frontendDir.exists()) frontendDir.mkdirs();
            
            File dataFile = new File(frontendDir, "data.js");
            try (PrintWriter out = new PrintWriter(dataFile)) {
                out.println(dataJs);
            }
            
            // Abre o navegador padrão diretamente
            File htmlFile = new File(frontendDir, "index.html");
            if (htmlFile.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(htmlFile.toURI());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(this, "Erro ao processar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE)
            );
        }
    }

    private String tourToStringToJson(String tourStr) {
        String[] lines = tourStr.split("\n");
        StringBuilder sb = new StringBuilder("[");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            // O Tour imprime como (x, y). Modificamos para [x, y] do javascript.
            trimmed = trimmed.replace("(", "[").replace(")", "]");
            sb.append(trimmed).append(", ");
        }
        if(sb.length() > 2) sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }

    private String pointsToJson(List<Point> points) {
        StringBuilder sb = new StringBuilder("[");
        for (Point p : points) {
            sb.append("[").append(p.x()).append(", ").append(p.y()).append("], ");
        }
        if(sb.length() > 2) sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }

    public static void main(String[] args) {
        // Usa LookAndFeel moderno (Nimbus) se disponível
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Ignora e usa padrao se Nimbus não existir
        }

        SwingUtilities.invokeLater(() -> {
            AppGUI gui = new AppGUI();
            gui.setVisible(true);
        });
    }
}
