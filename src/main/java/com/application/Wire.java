package com.application;

import java.util.Objects;

public class Wire {
    private final int number;
    private String color;
    private String wireArea;
    private String length;

    public Wire(int number, String color, String wireArea, String length) {
        this.number = number;
        this.color = color;
        this.wireArea = wireArea;
        this.length = length;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getWireArea() {
        return wireArea;
    }

    public void setWireArea(String wireArea) {
        this.wireArea = wireArea;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return String.format("Color=%-4s WireArea=%-5s Length=%4s", color, wireArea, length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, color, wireArea, length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wire wire = (Wire) o;

        return this.number == (wire.number) &&
                this.color.equals(wire.color) &&
                this.wireArea.equals(wire.wireArea) &&
                this.length.equals(wire.length);
    }
}