package io.github.thegatesdev.maze_generator_lib;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Random;

public class Maze {

    private static final Deque<Point> reusable = new ArrayDeque<>();
    private final int width, depth, corridorWidth, wallThickness;
    private final int[] neighborOffsets;
    private BitSet[] contents;
    private boolean generated = false;

    public Maze(int width, int depth, int corridorWidth, int wallThickness) {
        // Add 1 wall at the end to close.
        this.width = width;
        this.depth = depth;
        this.corridorWidth = corridorWidth;
        this.wallThickness = wallThickness;

        int off = corridorWidth + wallThickness;
        neighborOffsets = new int[]{
                off, 0,//R
                0, off,//U
                -off, 0,//L
                0, -off//D
        };
    }

    private static Point getPoint(int x, int y) {
        if (reusable.isEmpty()) return new Point(x, y);
        final Point pop = reusable.pop();
        pop.setLocation(x, y);
        return pop;
    }

    private static void savePoint(Point point) {
        reusable.push(point);
    }


    public void generate(Random random) {
        fill();
        // Start generation at bottom corner of bottom square.
        final Point start = getPoint(wallThickness, wallThickness);
        try {
            randomizedDfs(random, start);
        } catch (Throwable e) {
            throw new RuntimeException("Something went wrong trying to generate the maze.", e);
        } finally {
            savePoint(start);
        }
        generated = true;
    }


    public boolean isGenerated() {
        return generated;
    }

    public BitSet[] getGenerated() {
        if (!generated) throw new RuntimeException("Not generated yet");
        return contents;
    }


    private void fill() {
        contents = new BitSet[width];
        for (int i = 0; i < width; i++) {
            contents[i] = new BitSet(depth);
        }
    }

    private Point randomNeighbor(Random random, Point pos) {
        final int rnd = random.nextInt(4) * 2;
        Point last = null;
        for (int i = 0; i < 8; i++) {
            final int x = neighborOffsets[i++] + pos.x;
            final int y = neighborOffsets[i] + pos.y;
            if (inBounds(x, y) && contents[x].get(y)) {
                // Random lower than or exact match.
                if (i >= rnd) return getPoint(x, y);
                if (last == null) last = getPoint(x, y);
                else last.setLocation(x, y);
            }
        }// rnd 4, items 2 and 3
        return last;
    }

    private void connect(Point a, Point b) {
        if (b.x > a.x)
            removeWall(new Point(a.x + corridorWidth, a.y), false);
        else if (a.x > b.x)
            removeWall(new Point(b.x + corridorWidth, b.y), false);
        else if (b.y > a.y)
            removeWall(new Point(a.x, a.y + corridorWidth), true);
        else if (a.y > b.y)
            removeWall(new Point(b.x, b.y + corridorWidth), true);
    }

    private void removeWall(Point pos, boolean horizontal) {
        final int yMax = Math.min(pos.y + (horizontal ? wallThickness : corridorWidth), depth);
        final int xMax = Math.min(pos.x + (horizontal ? corridorWidth : wallThickness), width);
        for (int x = pos.x; x < xMax; x++) {
            contents[x].clear(pos.y, yMax);
        }
    }

    private void randomizedDfs(Random random, Point pos) {
        final int yMax = Math.min(pos.y + corridorWidth, depth);
        final int xMax = Math.min(pos.x + corridorWidth, width);

        // Clear this square.
        for (int x = pos.x; x < xMax; x++) {
            contents[x].clear(pos.y, yMax);
        }

        Point nextVertex = randomNeighbor(random, pos);
        // Iterate all neighbors (that aren't filled) in random order, and make them do the same.
        while (nextVertex != null) {
            connect(pos, nextVertex);
            randomizedDfs(random, nextVertex);
            savePoint(nextVertex);
            nextVertex = randomNeighbor(random, pos);
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < depth;
    }
}
