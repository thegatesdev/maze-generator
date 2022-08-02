package com.thegates.maze_generator_lib;

public class Vector2 {

    public int x, y;


    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public Vector2 add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }


    public Vector2 copy() {
        return new Vector2(x, y);
    }
}
