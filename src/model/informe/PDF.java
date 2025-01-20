import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
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

public class PDF extends Informe {

    public PDF(List<Movimiento> movimientos) {
        super(movimientos);
    }

    public void guardarGraficoMensual(File file) throws IOException {
        guardarArchivo(file, "Movimientos Mensuales");
    }

    public void guardarGraficoAnual(File file) throws IOException {
        guardarArchivo(file, "Movimientos Anuales");
    }

    @Override
    public void guardarArchivo(File file, String titulo) throws IOException {
        JFreeChart chart = crearGrafico(titulo);

        BufferedImage chartImage = chart.createBufferedImage(600, 400);
        File tempImage = File.createTempFile("chart", ".png");
        ImageIO.write(chartImage, "png", tempImage);

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

    private JFreeChart crearGrafico(String titulo) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Movimiento mov : movimientos) {
            dataset.addValue(mov.getCantidad(), "Cantidad", mov.getFecha().toString());
        }

        return ChartFactory.createBarChart(titulo, "Fecha", "Cantidad", dataset);
    }
}
