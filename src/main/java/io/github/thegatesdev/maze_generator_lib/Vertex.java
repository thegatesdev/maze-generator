package io.github.thegatesdev.maze_generator_lib;

public class Vertex {

    public int x, y;


    public Vertex(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public Vertex add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }


    public Vertex copy() {
        return new Vertex(x, y);
    }
}
