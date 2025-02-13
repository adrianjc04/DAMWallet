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
import java.time.LocalDate;
import javax.imageio.ImageIO;
import model.Movimiento;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;

/**
 * La clase PDF es responsable de generar y guardar un informe en formato PDF que incluye 
 * una lista de movimientos financieros y un gráfico de barras que representa los movimientos 
 * en función de su cantidad.
 * 
 * El informe generado incluye el logo de la empresa, la fecha del informe, el título "DAMWallet", 
 * una lista de movimientos con fecha, concepto y cantidad, y un gráfico de barras con los 
 * movimientos financieros.
 * 
 * Esta clase utiliza la biblioteca Apache PDFBox para crear el documento PDF y JFreeChart 
 * para generar el gráfico.
 * 
 * @author DAM2
 */
public class PDF {

    private List<Movimiento> movimientos;
    private String logoPath;

    /**
     * Constructor que inicializa la lista de movimientos y la ruta del logo que se 
     * incluirá en el informe.
     * 
     * @param movimientos la lista de movimientos financieros a incluir en el informe.
     * @param logoPath la ruta del archivo de imagen del logo a incluir en el informe.
     */
    public PDF(List<Movimiento> movimientos, String logoPath) {
        this.movimientos = movimientos;
        this.logoPath = logoPath;
    }

    /**
     * Guarda un archivo PDF con el gráfico de los movimientos correspondientes al último mes.
     * Llama al método `guardarArchivo` para generar el informe y guardarlo en el archivo proporcionado.
     * 
     * @param file el archivo donde se guardará el informe PDF.
     * @throws IOException si ocurre un error al escribir el archivo PDF.
     */
    public void guardarGraficoMensual(File file) throws IOException {
        guardarArchivo(file, "Movimientos Totales");
    }

    /**
     * Guarda un archivo PDF con el gráfico de los movimientos correspondientes al último año.
     * Llama al método `guardarArchivo` para generar el informe y guardarlo en el archivo proporcionado.
     * 
     * @param file el archivo donde se guardará el informe PDF.
     * @throws IOException si ocurre un error al escribir el archivo PDF.
     */
    public void guardarGraficoAnual(File file) throws IOException {
        guardarArchivo(file, "Movimientos Anuales");
    }

    /**
     * Guarda un archivo PDF que incluye un gráfico de barras y una lista de movimientos, 
     * con un título específico. Los movimientos que se incluyen en el gráfico y el informe 
     * dependen del título (mensual o anual).
     * 
     * @param file el archivo donde se guardará el informe PDF.
     * @param titulo el título que se utilizará en el gráfico (por ejemplo, "Movimientos Totales").
     * @throws IOException si ocurre un error al escribir el archivo PDF.
     */
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

        // Insertar fecha actual
        contentStream.beginText();
        contentStream.setFont(PDType0Font.load(document, new File("asets\\fuentes\\Roboto-Light.ttf")), 12);
        contentStream.newLineAtOffset(70, 650);
        contentStream.showText("Fecha del informe: " + LocalDate.now().toString());
        contentStream.endText();

        // Insertar título
        contentStream.beginText();
        contentStream.setFont(PDType0Font.load(document, new File("asets\\fuentes\\Roboto-Black.ttf")), 16);
        contentStream.newLineAtOffset(250, 680);
        contentStream.showText("DAMWallet");
        contentStream.endText();

        // Insertar lista de movimientos
        contentStream.beginText();
        contentStream.setFont(PDType0Font.load(document, new File("asets\\fuentes\\Roboto-Light.ttf")), 10);
        contentStream.newLineAtOffset(70, 580);
        contentStream.showText("Lista de movimientos:");
        contentStream.newLineAtOffset(0, -15);

        int yPosition = 565;
        for (Movimiento mov : movimientos) {
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText(mov.getFecha().toString() + " - " + mov.getConcepto() + " - " + String.format("%.2f EUR", mov.getCantidad()));
            yPosition -= 15;
        }
        contentStream.endText();

        // Si no hay suficiente espacio para el gráfico, agregar una nueva página
        if (yPosition - 350 < 50) {
            contentStream.close();
            PDPage newPage = new PDPage();
            document.addPage(newPage);
            contentStream = new PDPageContentStream(document, newPage);
            yPosition = 700;
        }

        // Insertar gráfico después de la lista de movimientos
        PDImageXObject pdImage = PDImageXObject.createFromFile(tempImage.getAbsolutePath(), document);
        contentStream.drawImage(pdImage, 50, yPosition - 320, 500, 300);
        contentStream.close();

        document.save(file);
        document.close();
        tempImage.delete();
    }

    /**
     * Crea un gráfico de barras utilizando los movimientos financieros.
     * El gráfico utiliza la fecha como el eje de categorías y la cantidad como el eje de valores.
     * 
     * @param titulo el título que se utilizará para el gráfico.
     * @return un objeto JFreeChart que representa el gráfico generado.
     */
    private JFreeChart crearGrafico(String titulo) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Movimiento mov : movimientos) {
            dataset.addValue(mov.getCantidad(), "Cantidad", mov.getFecha().toString());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                titulo,
                "Fecha",
                "Cantidad",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        CategoryAxis categoryAxis = plot.getDomainAxis();
        categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        return chart;
    }
}