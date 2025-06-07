package Biblioteca;

//Importar todas las dependencias de iPDF
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PDFMora {

    //Crear archivo PDF con los datos de la mora
    public static void generarPDF(String usuario, int diasRetraso, double monto, String fecha) {
        try {
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            String nombreArchivo = "Comprobante_Multa_" + usuario.replace(" ", "_") + "_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar comprobante como...");
            fileChooser.setSelectedFile(new File(nombreArchivo));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos PDF", "pdf"));

            int seleccion = fileChooser.showSaveDialog(null);
            if (seleccion != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(null, "Operación cancelada.");
                return;
            }

            File archivoSeleccionado = fileChooser.getSelectedFile();
            String ruta = archivoSeleccionado.getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".pdf")) {
                ruta += ".pdf";
            }

            PdfWriter writer = new PdfWriter(new FileOutputStream(ruta));
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A5);
            doc.setMargins(20, 20, 20, 20);

            doc.add(new Paragraph("📄 Comprobante de Pago de Mora")
                    .setFontSize(16)
                    .setFont(boldFont)
                    .setMarginBottom(20));

            Table tabla = new Table(2);
            tabla.setWidth(UnitValue.createPercentValue(100));
            tabla.addCell(new Cell().add(new Paragraph("Usuario")).setFont(boldFont));
            tabla.addCell(usuario);
            tabla.addCell(new Cell().add(new Paragraph("Días de Retraso")).setFont(boldFont));
            tabla.addCell(String.valueOf(diasRetraso));
            tabla.addCell(new Cell().add(new Paragraph("Monto Pagado")).setFont(boldFont));
            tabla.addCell(String.format("$ %.2f", monto));
            tabla.addCell(new Cell().add(new Paragraph("Fecha de Multa")).setFont(boldFont));
            tabla.addCell(fecha);
            tabla.addCell(new Cell().add(new Paragraph("Fecha de Pago")).setFont(boldFont));
            tabla.addCell(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            doc.add(tabla);
            doc.close();

            JOptionPane.showMessageDialog(null, "Comprobante generado en:\n" + ruta);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al generar PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Centrar el contenido de la tabla
    public static void centrarContenidoTabla(JTable tablaMultas) {
        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        TableModel modelo = tablaMultas.getModel();
        for (int i = 0; i < modelo.getColumnCount(); i++) {
            tablaMultas.getColumnModel().getColumn(i).setCellRenderer(centrado);
        }
    }

    //Crear titulo redondeado
    public static JPanel crearPanelRedondeado(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }

    //Formatear botones
    public static void estiloBoton(JButton boton) {
        boton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        boton.setFocusPainted(false);
        boton.setBackground(Color.WHITE);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(6, 12, 6, 12)
        ));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
