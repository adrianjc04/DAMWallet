package model.informe;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import model.Movimiento;

/**
 *
 * @author DAM2
 */
class PDF extends Informe {

    public PDF(List<Movimiento> movimientos) {
        super(movimientos);
    }

    public void guardarGraficoMensual(File file) throws IOException {
        guardarArchivo(file, LocalDate.now().minusMonths(1), "Movimientos Mensuales");
    }

    public void guardarGraficoAnual(File file) throws IOException {
        guardarArchivo(file, LocalDate.now().minusYears(1), "Movimientos Anuales");
    }

    @Override
    public void guardarArchivo(File file, LocalDate limite, String titulo) throws IOException {
        BarChart<String, Number> barChart = crearGrafico(titulo, obtenerDatosFiltrados(limite));
        WritableImage image = barChart.snapshot(null, null);
        File tempImage = File.createTempFile("chart", ".png");
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", tempImage);

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDImageXObject pdImage = PDImageXObject.createFromFile(tempImage.getAbsolutePath(), document);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.drawImage(pdImage, 50, 500, 500, 300);
        contentStream.close();
        document.save(file);
        document.close();
        tempImage.delete();
    }

    private BarChart<String, Number> crearGrafico(String titulo, XYChart.Series<String, Number> dataSeries) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Mes/Año");
        yAxis.setLabel("Cantidad");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(titulo);
        barChart.getData().add(dataSeries);
        barChart.setStyle("-fx-bar-fill: #123EAF;"); // Azul oscuro

        return barChart;
    }

    private XYChart.Series<String, Number> obtenerDatosFiltrados(LocalDate limite) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        movimientos.stream()
                .filter(mov -> mov.getFecha().isAfter(limite))
                .forEach(mov -> series.getData().add(new XYChart.Data<>(mov.getFecha().toString(), mov.getCantidad())));

        return series;
    }
}
