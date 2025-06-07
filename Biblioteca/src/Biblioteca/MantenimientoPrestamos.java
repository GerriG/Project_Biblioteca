package Biblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MantenimientoPrestamos extends JPanel {

    //Configurar ventana
    public MantenimientoPrestamos() {
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        //Crear botones
        JButton btnNuevo = new JButton("➕ Nuevo Préstamo");
        JButton btnEditar = new JButton("✏️ Editar");
        JButton btnDevolver = new JButton("📥 Devolución");

        estiloBoton(btnNuevo);
        estiloBoton(btnEditar);
        estiloBoton(btnDevolver);

        add(btnNuevo);
        add(btnEditar);
        add(btnDevolver);

        // Asignar acciones a botones
        btnNuevo.addActionListener(e -> JOptionPane.showMessageDialog(this, "Procesar nuevo préstamo"));
        btnEditar.addActionListener(e -> JOptionPane.showMessageDialog(this, "Editar préstamo existente"));
        btnDevolver.addActionListener(e -> JOptionPane.showMessageDialog(this, "Procesar devolución de libro"));
    }

    //Formatear botones
    private void estiloBoton(JButton boton) {
        boton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        boton.setFocusPainted(false);
        boton.setBackground(new Color(230, 230, 230));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(6, 12, 6, 12)
        ));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
