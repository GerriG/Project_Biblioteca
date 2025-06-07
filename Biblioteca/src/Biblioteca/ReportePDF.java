package Biblioteca;

//Dependencias de iPDF
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReportePDF {

    //Generar el reporte PDF mediante datos de la BD
    public static void generarDesdeTabla(JTable tabla) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar reporte como...");
        fileChooser.setSelectedFile(new File("ReportePrestamos.pdf"));
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return; // Usuario canceló
        }

        File archivoDestino = fileChooser.getSelectedFile();
        if (!archivoDestino.getName().toLowerCase().endsWith(".pdf")) {
            archivoDestino = new File(archivoDestino.getAbsolutePath() + ".pdf");
        }

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(archivoDestino));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4.rotate());

            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            Paragraph titulo = new Paragraph("📊 Reporte de Préstamos")
                    .setFont(fontBold)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(titulo);
            document.add(new Paragraph(" "));

            int columnas = tabla.getColumnCount();
            Table table = new Table(columnas).useAllAvailableWidth();

            // Encabezados
            for (int i = 0; i < columnas; i++) {
                String encabezado = tabla.getColumnName(i);
                Cell celdaEncabezado = new Cell().add(new Paragraph(encabezado).setFont(fontBold));
                table.addHeaderCell(celdaEncabezado);
            }

            // Datos
            TableModel model = tabla.getModel();
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < columnas; col++) {
                    Object valor = model.getValueAt(row, col);
                    String texto = valor != null ? valor.toString() : "";
                    Cell celdaDato = new Cell().add(new Paragraph(texto).setFont(fontNormal));
                    table.addCell(celdaDato);
                }
            }

            document.add(table);
            document.close();

            JOptionPane.showMessageDialog(null, "✅ Reporte generado correctamente:\n" + archivoDestino.getAbsolutePath());

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "❌ Error al generar PDF:\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
