package io.github.thegatesdev.maze_generator_lib;

import java.awt.*;
import java.util.BitSet;
import java.util.Random;

public class Maze {

    private final int width, depth, corridorWidth, wallThickness, off;

    private BitSet[] contents;
    private boolean generated = false;

    public Maze(int width, int depth, int corridorWidth, int wallThickness) {
        // Size of 1 gap with 1 wall.
        off = corridorWidth + wallThickness;
        // Add 1 wall at the end to close.
        this.width = width;
        this.depth = depth;
        this.corridorWidth = corridorWidth;
        this.wallThickness = wallThickness;
    }

    public static Point translateNew(Point point, int x, int y) {
        return new Point(point.x + x, point.y + y);
    }

    public void generate(Random random) {
        fill();
        // Start generation at bottom corner of bottom square.
        try {
            randomizedDfs(random, new Point(wallThickness, wallThickness));
        } catch (Throwable e) {
            throw new RuntimeException("Something went wrong trying to generate the maze.", e);
        }
        generated = true;
    }

    public BitSet[] getContents() {
        return contents;
    }

    public boolean isGenerated() {
        return generated;
    }

    private void fill() {
        contents = new BitSet[width];
        for (int i = 0; i < width; i++) {
            contents[i] = new BitSet(depth);
        }
    }

    private Point randomNeighbor(Random random, Point pos) {
        int active = 3;
        final Point[] nbrs = new Point[]{
                translateNew(pos, 0, -off), translateNew(pos, 0, off), translateNew(pos, off, 0), translateNew(pos, -off, 0)
        };
        for (int i = 0; i < nbrs.length; i++) {
            final Point nbr = nbrs[i];
            if (!inMaze(nbr.x, nbr.y) || !contents[nbr.x].get(nbr.y)) {
                // Not in maze or already cleared...
                nbrs[i] = nbrs[active--]; // Make last element redundant, move last to this index ( clearing / removing the current neighbor ).
            }
        }
        if (active == 0) return null;// No neighbors.
        if (active == 1) return nbrs[0];// 1 neighbor, no random call needed.
        return nbrs[random.nextInt(active)];
    }

    private void connect(Point a, Point b) {
        if (b.x > a.x)
            removeWall(new Point(a.x + corridorWidth, a.y), false);
        if (a.x > b.x)
            removeWall(new Point(b.x + corridorWidth, b.y), false);
        if (b.y > a.y)
            removeWall(new Point(a.x, a.y + corridorWidth), true);
        if (a.y > b.y)
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
            nextVertex = randomNeighbor(random, pos);
        }
    }

    private boolean inMaze(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < depth;
    }
}
