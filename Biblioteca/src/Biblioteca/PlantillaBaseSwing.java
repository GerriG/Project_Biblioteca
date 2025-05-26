package Biblioteca;

import javax.swing.*;
import java.awt.*;

public class PlantillaBaseSwing extends JFrame {

    public PlantillaBaseSwing(String tituloVentana) {
        setTitle(tituloVentana);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        // Fondo personalizado (puedes reemplazar la imagen)
        JLabel fondo = new JLabel(new ImageIcon(getClass().getResource("/Biblioteca/Wallpaper/Fondo.jpg")));
        fondo.setBounds(0, 0, 800, 600);
        setContentPane(fondo);
        fondo.setLayout(null);

        // Panel superior (barra azul celeste)
        JPanel barraSuperior = new JPanel();
        barraSuperior.setBackground(new Color(135, 206, 235, 200)); // translúcido
        barraSuperior.setBounds(0, 0, 800, 60);
        barraSuperior.setLayout(null);
        fondo.add(barraSuperior);

        JLabel titulo = new JLabel("Nombre del Módulo", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(Color.WHITE);
        titulo.setBounds(0, 10, 800, 40);
        barraSuperior.add(titulo);

        // Panel central blanco redondeado
        JPanel panelCentral = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        panelCentral.setBounds(100, 100, 600, 350);
        panelCentral.setOpaque(false);
        panelCentral.setLayout(null);
        fondo.add(panelCentral);

        // Aquí puedes agregar tus componentes en el panel central
        JLabel ejemplo = new JLabel("Contenido aquí");
        ejemplo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        ejemplo.setBounds(30, 30, 200, 30);
        panelCentral.add(ejemplo);

        // Panel inferior (botón estilizado)
        JPanel barraInferior = new JPanel();
        barraInferior.setBackground(new Color(135, 206, 235));
        barraInferior.setBounds(0, 540, 800, 60);
        barraInferior.setLayout(null);
        fondo.add(barraInferior);

        JButton btnAccion = new JButton("Ejecutar");
        btnAccion.setBounds(620, 10, 150, 40);
        btnAccion.setFocusPainted(false);
        btnAccion.setBackground(new Color(70, 130, 180));
        btnAccion.setForeground(Color.WHITE);
        btnAccion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAccion.setBorder(BorderFactory.createEmptyBorder());
        barraInferior.add(btnAccion);
    }

    // Método main de prueba
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            new PlantillaBaseSwing("Plantilla - Biblioteca").setVisible(true);
//        });
//    }
}