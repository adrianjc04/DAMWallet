package model.informe;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import model.Movimiento;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;

public class PDF {

    private List<Movimiento> movimientos;
    private String logoPath;

    public PDF(List<Movimiento> movimientos, String logoPath) {
        this.movimientos = movimientos;
        this.logoPath = logoPath;
    }

    public void guardarGraficoMensual(File file) throws IOException {
        guardarArchivo(file, "Movimientos Totales");
    }

    public void guardarGraficoAnual(File file) throws IOException {
        guardarArchivo(file, "Movimientos Anuales");
    }

    public void guardarArchivo(File file, String titulo) throws IOException {
        JFreeChart chart = crearGrafico(titulo);
        BufferedImage chartImage = chart.createBufferedImage(600, 400);
        File tempImage = File.createTempFile("chart", ".png");
        ImageIO.write(chartImage, "png", tempImage);

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Insertar logo
        PDImageXObject logo = PDImageXObject.createFromFile(logoPath, document);
        contentStream.drawImage(logo, 250, 700, 100, 100);

        // Insertar texto
        contentStream.beginText();
        contentStream.setFont(PDType0Font.load(document, new File("asets\\fuentes\\Roboto-Black.ttf")), 16);
        contentStream.newLineAtOffset(250, 650);
        contentStream.showText("DAMWallet");
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(PDType0Font.load(document, new File("asets\\fuentes\\Roboto-Light.ttf")), 12);
        contentStream.newLineAtOffset(70, 620);
        contentStream.showText("Este informe presenta un resumen de los movimientos registrados.");
        contentStream.endText();

        // Insertar gráfico
        PDImageXObject pdImage = PDImageXObject.createFromFile(tempImage.getAbsolutePath(), document);
        contentStream.drawImage(pdImage, 50, 300, 500, 300);

        // Determinar el gasto más alto y el ingreso más alto
        Movimiento gastoMayor = movimientos.stream().filter(m -> m.getCantidad() < 0).min((m1, m2) -> Double.compare(m1.getCantidad(), m2.getCantidad())).orElse(null);
        Movimiento ingresoMayor = movimientos.stream().filter(m -> m.getCantidad() > 0).max((m1, m2) -> Double.compare(m1.getCantidad(), m2.getCantidad())).orElse(null);

        // Mostrar los detalles en el PDF
        contentStream.beginText();
        contentStream.setFont(PDType0Font.load(document, new File("asets\\fuentes\\Roboto-Light.ttf")), 12);
        contentStream.newLineAtOffset(70, 250);
        if (gastoMayor != null) {
            contentStream.showText("Gasto más alto: " + gastoMayor.getConcepto() + " -> " + String.format("%.2f", gastoMayor.getCantidad()) + " EUR (" + gastoMayor.getFecha().toString() + ")");
            contentStream.newLineAtOffset(0, -15);
        }
        if (ingresoMayor != null) {
            contentStream.showText("Ingreso más alto: " + ingresoMayor.getConcepto() + " -> " + String.format("%.2f", ingresoMayor.getCantidad()) + " EUR (" + ingresoMayor.getFecha().toString() + ")");
        }
        contentStream.endText();

        contentStream.close();
        document.save(file);
        document.close();
        tempImage.delete();
    }

    private JFreeChart crearGrafico(String titulo) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Movimiento mov : movimientos) {
            dataset.addValue(mov.getCantidad(), "Cantidad", mov.getFecha().toString());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                titulo,         // Título
                "Fecha",        // Etiqueta eje X
                "Cantidad",     // Etiqueta eje Y
                dataset,        // Datos
                PlotOrientation.VERTICAL, // Orientación vertical
                true,           // Leyenda visible
                true,           // Tooltips visibles
                false           // URL de la acción visible
        );

        // Ajustar las etiquetas de las fechas para que se lean bien
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        CategoryAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        return chart;
    }
}