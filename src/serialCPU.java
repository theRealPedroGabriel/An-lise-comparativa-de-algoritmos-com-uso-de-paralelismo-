import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class serialCPU {

    public static void main(String[] args) throws IOException {
        String[] fileNames = {
                "C:\\Users\\pgsmc\\OneDrive\\Documentos\\EstruturadeDados\\carrinho-de-compras\\projeto final paralelo concorrente\\src\\DonQuixote-388208.txt",   //grande
                "C:\\Users\\pgsmc\\OneDrive\\Documentos\\EstruturadeDados\\carrinho-de-compras\\projeto final paralelo concorrente\\src\\MobyDick-217452.txt",  //medio
                "C:\\Users\\pgsmc\\OneDrive\\Documentos\\EstruturadeDados\\carrinho-de-compras\\projeto final paralelo concorrente\\src\\Dracula-165307.txt"    //pequeno
        };
        String wordToFind = "the"; // Mudar conforme necessário
        BufferedWriter writer = new BufferedWriter(new FileWriter("performance_results.csv"));
        writer.write("Filename,Execution Time (ms)\n"); // Cabeçalho do CSV
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (String filePath : fileNames) {
            long totalTime = 0;
            for (int i = 0; i < 3; i++) { // Executar cada teste 3 vezes
                long startTime = System.nanoTime();
                int count = countOccurrences(filePath, wordToFind);
                long endTime = System.nanoTime();
                long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                totalTime += duration;
                System.out.println("Run " + (i+1) + ": " + duration + " ms");
                System.out.println("Word '" + wordToFind + "' occurred " + count + " times.");
            }
            long averageTime = totalTime / 3;// Tempo médio de execução
            File file = new File(filePath);
            dataset.addValue(averageTime, "Execution Time", file.getName());
            writer.write(file.getName() + "," + averageTime + "\n");
            System.out.println("Average time for " + file.getName()+ ": " + averageTime + " ms");
        }
        writer.close();
        JFreeChart chart = createChart(dataset);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        // Criando a janela
        ApplicationFrame frame = new ApplicationFrame("SerialCPU");
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }
    private static int countOccurrences(String filePath, String wordToFind) throws IOException {
        int count = 0;
        wordToFind = wordToFind.toLowerCase(); // Considerando insensibilidade a maiúsculas/minúsculas

        // Ler o arquivo linha por linha
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Processar cada palavra na linha
                String[] words = line.toLowerCase().split("\\s+"); // Dividir por qualquer espaço em branco
                for (String word : words) {
                    if (word.equals(wordToFind)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }
    private static JFreeChart createChart(DefaultCategoryDataset dataset) {
        // Criar um gráfico de barras.
        JFreeChart chart = ChartFactory.createBarChart(
                "Performance Analysis", // Título do gráfico
                "File Size", // Eixo X Label
                "Time (ms)", // Eixo Y Label
                dataset, // Dados
                PlotOrientation.VERTICAL, // Orientação do gráfico
                true, // Incluir legenda
                true, // Tooltips
                false // URLs
        );


        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED); // Cor para a série "x"
        renderer.setSeriesPaint(1, Color.BLUE); // Cor para a série "y"
        renderer.setSeriesPaint(2, Color.GREEN); // Cor para a série "z"

        chart.setBackgroundPaint(Color.WHITE);

        chart.getLegend().setFrame(BlockBorder.NONE);

        chart.setTitle(
                new TextTitle("Tempo de execusao SerialCPU",
                        new Font("Serif", java.awt.Font.BOLD, 18))
        );
        return chart;
    }
}
