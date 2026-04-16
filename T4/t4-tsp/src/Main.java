import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String filename = "../dados/usa13509.txt"; // Modo Padrão
        if (args.length >= 1) {
            filename = args[0];
        }

        File testFile = new File(filename);
        if (!testFile.exists()) {
            System.err.println("ERRO GRAVE: O arquivo '" + filename + "' não foi encontrado.");
            System.err.println("Por favor verifique se a pasta 'dados' está onde deveria ou passe o caminho absoluto.");
            return;
        }

        In in;
        try {
            in = new In(filename);
        } catch (Exception e) {
            System.err.println("ERRO ao processar o formato do arquivo: " + e.getMessage());
            return;
        }
        
        int width = in.readInt();
        int height = in.readInt();

        List<Point> points = new ArrayList<>();
        while (!in.isEmpty()) {
            points.add(new Point(in.readDouble(), in.readDouble()));
        }

        StdOut.println("Instancia TSP carregada:");
        StdOut.println("- dimensoes: " + width + " x " + height);
        StdOut.println("- numero de pontos: " + points.size());

        Tour nearest = new Tour();
        Tour smallest = new Tour();

        long timeNearest = 0;
        long timeSmallest = 0;

        try {
            long startNearest = System.currentTimeMillis();
            for (Point point : points) {
                nearest.insertNearest(point);
            }
            timeNearest = System.currentTimeMillis() - startNearest;

            long startSmallest = System.currentTimeMillis();
            for (Point point : points) {
                smallest.insertSmallest(point);
            }
            timeSmallest = System.currentTimeMillis() - startSmallest;
        }
        catch (UnsupportedOperationException exception) {
            StdOut.println();
            StdOut.println("Implementacao pendente em Tour.java:");
            StdOut.println(exception.getMessage());
            StdOut.println("Complete insertNearest e insertSmallest e execute novamente.");
            return;
        }

        StdOut.println();
        StdOut.printf("Nearest insertion: tamanho = %d, comprimento = %.4f, tempo = %d ms\n",
                nearest.size(), nearest.length(), timeNearest);
        StdOut.printf("Smallest insertion: tamanho = %d, comprimento = %.4f, tempo = %d ms\n",
                smallest.size(), smallest.length(), timeSmallest);

        // Em vez de invocar o StdDraw nativo, exporta invisívelmente pros navegadores e abre.
        exportAndOpen(width, height, points, nearest, smallest, timeNearest, timeSmallest);
    }

    private static void exportAndOpen(int width, int height, List<Point> points, Tour nearest, Tour smallest, long timeNearest, long timeSmallest) {
        try {
            String nearestStr = tourToStringToJson(nearest.toString());
            String smallestStr = tourToStringToJson(smallest.toString());
            String pointsJson = pointsToJson(points);
            
            String dataJs = "var tspData = {\n" +
              "  width: " + width + ",\n" +
              "  height: " + height + ",\n" +
              "  points: " + pointsJson + ",\n" +
              "  nearestTour: " + nearestStr + ",\n" +
              "  nearestLength: " + nearest.length() + ",\n" +
              "  timeNearest: " + timeNearest + ",\n" +
              "  smallestTour: " + smallestStr + ",\n" +
              "  smallestLength: " + smallest.length() + ",\n" +
              "  timeSmallest: " + timeSmallest + "\n" +
            "};";

            File baseDir = new File(System.getProperty("user.dir"));
            File frontendDir = new File(baseDir.getParentFile(), "frontend");
            
            // Caso o aluno execute diretamente da pasta principal t4-tsp ao invés da t4-tsp/src
            if (!frontendDir.exists() && new File(baseDir, "frontend").exists()) {
                frontendDir = new File(baseDir, "frontend");
            }
            if (!frontendDir.exists()) frontendDir.mkdirs();
            
            File dataFile = new File(frontendDir, "data.js");
            try (PrintWriter out = new PrintWriter(dataFile)) {
                out.println(dataJs);
            }
            
            File htmlFile = new File(frontendDir, "index.html");
            if (htmlFile.exists() && java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(htmlFile.getCanonicalFile().toURI());
                System.out.println("Navegador aberto em: " + htmlFile.getCanonicalFile().getAbsolutePath());
            } else {
                System.out.println("Não foi possível abrir o navegador automaticamente.");
                System.out.println("Abra manualmente este arquivo: " + htmlFile.getCanonicalPath());
            }
        } catch (Exception e) {
            System.err.println("Erro ao exportar HTML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String tourToStringToJson(String tourStr) {
        String[] lines = tourStr.split("\n");
        StringBuilder sb = new StringBuilder("[");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            trimmed = trimmed.replace("(", "[").replace(")", "]");
            sb.append(trimmed).append(", ");
        }
        if(sb.length() > 2) sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }

    private static String pointsToJson(List<Point> points) {
        StringBuilder sb = new StringBuilder("[");
        for (Point p : points) {
            sb.append("[").append(p.x()).append(", ").append(p.y()).append("], ");
        }
        if(sb.length() > 2) sb.setLength(sb.length() - 2);
        sb.append("]");
        return sb.toString();
    }
}
