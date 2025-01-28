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
        contentStream.drawImage(logo, 50, 700, 100, 100);

        // Insertar texto
        contentStream.beginText();
        contentStream.setFont(PDType0Font.load(document, new File("asets\\fuentes\\Roboto-Black.ttf")), 12);
        contentStream.newLineAtOffset(50, 650);
        contentStream.showText("Este informe presenta un resumen de los movimientos registrados.");
        contentStream.endText();

        // Insertar gráfico
        PDImageXObject pdImage = PDImageXObject.createFromFile(tempImage.getAbsolutePath(), document);
        contentStream.drawImage(pdImage, 50, 300, 500, 300);

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
