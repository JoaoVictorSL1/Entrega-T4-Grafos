/* *****************************************************************************
 *  Name:    Jeremie Lumbroso
 *  NetID:   lumbroso
 *  Precept: P99
 *
 *  Partner Name:    Donna Gabai
 *  Partner NetID:   dgabai
 *  Partner Precept: P99
 *
 *  Description:  Implements an interactive client that builds a Tour using
 *                either the nearest heuristic (red) or the smallest heuristic
 *                (blue).
 *
 *                Can be called with or without an input file to begin:
 *
 *                  java-introcs TSPVisualizer tsp1000.txt
 *
 *                Keyboard commands:
 *                  - n   toggle nearest heuristic tour
 *                  - s   toggle smallest heuristic tour
 *                  - m   toggle mouse up correction (what does this do... ?)
 *                  - q   quit (no!)
 *
 *  Dependencies: Point, StdOut, StdDraw
 **************************************************************************** */

import java.util.ArrayList;
import java.util.List;

public class TSPVisualizer {
    private static final int BORDER = 70;

    public static void showTours(int width, int height, List<Point> points, Tour nearest, Tour smallest) {
        showTours(width, height, points, nearest, smallest, true, true);
    }

    public static void showTours(int width, int height, List<Point> points, Tour nearest, Tour smallest, boolean showNearest, boolean showSmallest) {
        // Double width for side-by-side view if both are shown, otherwise single width
        int canvasWidth = (showNearest && showSmallest) ? width * 2 : width;
        StdDraw.setCanvasSize(canvasWidth, height + BORDER);
        StdDraw.enableDoubleBuffering();
        render(width, height, points, nearest, smallest, showNearest, showSmallest);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException(
                    "informe o arquivo de entrada. Ex.: java TSPVisualizer ../dados/entrada_oficial.txt"
            );
        }

        In in = new In(args[0]);
        int width = in.readInt();
        int height = in.readInt();
        ArrayList<Point> points = new ArrayList<>();
        Tour nearest = new Tour();
        Tour smallest = new Tour();

        try {
            while (!in.isEmpty()) {
                Point p = new Point(in.readDouble(), in.readDouble());
                points.add(p);
                nearest.insertNearest(p);
                smallest.insertSmallest(p);
            }
        }
        catch (UnsupportedOperationException exception) {
            StdOut.println("Implementacao pendente em Tour.java:");
            StdOut.println(exception.getMessage());
            return;
        }

        showTours(width, height, points, nearest, smallest);
    }

    private static void render(int width, int height, List<Point> points, Tour nearest, Tour smallest, boolean showNearest, boolean showSmallest) {
        StdDraw.clear();
        
        int canvasWidth = (showNearest && showSmallest) ? width * 2 : width;

        if (showNearest && showSmallest) {
            // Render Nearest on the left
            drawSubTour(0, canvasWidth, width, height, "Nearest Insertion", points, nearest, StdDraw.RED);
            // Render Smallest on the right
            drawSubTour(width, canvasWidth, width, height, "Smallest Insertion", points, smallest, StdDraw.BLUE);
        } else if (showNearest) {
            drawSubTour(0, canvasWidth, width, height, "Nearest Insertion", points, nearest, StdDraw.RED);
        } else if (showSmallest) {
            drawSubTour(0, canvasWidth, width, height, "Smallest Insertion", points, smallest, StdDraw.BLUE);
        }
        
        StdDraw.show();
    }

    private static void drawSubTour(int offsetX, int canvasWidth, int width, int height, String title, List<Point> points, Tour tour, java.awt.Color color) {
        // Correct approach to shift [0, width] to [offsetX, offsetX+width] in a [0, canvasWidth] canvas:
        // StdDraw scale [xmin, xmax] maps to screen [0, canvasWidth]
        // We want x=0 to map to screen=offsetX, and x=width to map to screen=offsetX+width
        // This holds if xmin = -offsetX and xmax = canvasWidth - offsetX
        StdDraw.setXscale((double)-offsetX, (double)(canvasWidth - offsetX));
        StdDraw.setYscale((double)-BORDER, (double)height);

        // Draw points (original coordinates)
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.005);
        for (Point p : points) {
            p.draw();
        }

        // Draw tour (original coordinates)
        if (tour != null) {
            StdDraw.setPenColor(color);
            StdDraw.setPenRadius(0.003);
            tour.draw();
            
            // Information - we use absolute scale [0, width] for text in this segment
            // But we must reset the scale to draw text relative to the sub-view
            StdDraw.setXscale(0.0, (double)width);
            // This setXscale call shifts the coordinate system again. 
            // So offsetX becomes the new screen 0 for this call.
            // Wait, StdDraw's setXscale is global. 
            // Better: use the same scale as above and draw text at shifted positions.
            StdDraw.setXscale((double)-offsetX, (double)(canvasWidth - offsetX));
            
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.textLeft(width/2.0 - 50.0, height + 20.0, title);
            StdDraw.textLeft(10.0, -35.0, String.format("Length: %.4f", tour.length()));
            StdDraw.textLeft(10.0, -10.0, "Points: " + points.size());
        }
    }
}
