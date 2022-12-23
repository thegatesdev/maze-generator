package io.github.thegatesdev.maze_generator_lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Maze {

    private final int width, depth, corridorWidth, wallThickness, off;

    private Item[][] contents;

    public Maze(int width, int depth, int corridorWidth, int wallThickness) {
        // Size of 1 gap with 1 wall.
        off = corridorWidth + wallThickness;
        // Add 1 wall at the end to close.
        this.width = width;
        this.depth = depth;
        this.corridorWidth = corridorWidth;
        this.wallThickness = wallThickness;
    }

    public Maze(Vertex size, int corridorWidth, int wallThickness) {
        this(size.x, size.y, corridorWidth, wallThickness);
    }


    static <T> void fill2dArray(T[][] array, T item) {
        for (int i = 0; i < array.length; i++) {
            final T[] ts = array[i];
            for (int y = 0; y < ts.length; y++) {
                array[i][y] = item;
            }
        }
    }

    static <T> void fill2dArrayAt(T[][] array, Vertex from, Vertex to, T item) {
        final int len = array.length;
        if (from.x > len || to.x > len || from.x < 0 || to.x < 0)
            throw new IllegalArgumentException("Vector size not within bounds");
        for (int x = from.x; x < to.x; x++) {
            for (int y = from.y; y < to.y; y++) {
                array[x][y] = item;
            }
        }
    }


    public void generate(Random random) {
        contents = new Item[width][depth];
        fill2dArray(contents, Item.FILLED);

        // Start generation at bottom corner of bottom square.
        randomizedDfs(random, new Vertex(wallThickness, wallThickness));
    }

    public Item[][] getContents() {
        return contents;
    }

    void randomizedDfs(Random random, Vertex pos) {
        final int yMax = Math.min(pos.y + corridorWidth, depth);
        final int xMax = Math.min(pos.x + corridorWidth, width);

        // Clear this square.
        fill2dArrayAt(contents, pos, new Vertex(xMax, yMax), Item.EMPTY);

        Vertex nextVertex = randomNeighbor(random, pos);
        // Iterate all neighbors (that aren't filled) in random order, and make them do the same.
        while (nextVertex != null) {
            connect(pos, nextVertex);
            randomizedDfs(random, nextVertex);
            nextVertex = randomNeighbor(random, pos);
        }
    }

    Vertex randomNeighbor(Random random, Vertex pos) {
        final List<Vertex> nbrs = new ArrayList<>(Arrays.asList(pos.copy().add(0, off), pos.copy().add(0, -off), pos.copy().add(off, 0), pos.copy().add(-off, 0)));

        nbrs.removeIf(i -> i.x < 0 || i.y < 0 || i.x >= width || i.y >= depth || contents[i.x][i.y] == Item.EMPTY);

        if (nbrs.isEmpty()) {
            return null;
        }
        final int size = nbrs.size();
        if (size == 1) return nbrs.get(0);

        return nbrs.get(random.nextInt(size));
    }

    void connect(Vertex a, Vertex b) {
        if (b.x > a.x)
            removeWall(new Vertex(a.x + corridorWidth, a.y), false);
        if (a.x > b.x)
            removeWall(new Vertex(b.x + corridorWidth, b.y), false);
        if (b.y > a.y)
            removeWall(new Vertex(a.x, a.y + corridorWidth), true);
        if (a.y > b.y)
            removeWall(new Vertex(b.x, b.y + corridorWidth), true);
    }

    void removeWall(Vertex pos, boolean horizontal) {
        final int yMax = Math.min(pos.y + (horizontal ? wallThickness : corridorWidth), depth);
        final int xMax = Math.min(pos.x + (horizontal ? corridorWidth : wallThickness), width);
        fill2dArrayAt(contents, pos, new Vertex(xMax, yMax), Item.EMPTY);
    }


    public enum Item {
        FILLED,
        EMPTY
    }
}
