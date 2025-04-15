package com.application;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wire {
    final int number;
    String color;
    String wireArea;
    String length;

    public Wire(int number, String color, String wireArea, String length) {
        this.number = number;
        this.color = color;
        this.wireArea = wireArea;
        this.length = length;
    }

    @Override
    public String toString() {
        return String.format("Color=%-4s WireArea=%-5s Length=%4s", color, wireArea, length);
    }

//    @Override
//    public int hashCode() {
//        return Objects.hash(number, color, wireArea, length);
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Wire wire = (Wire) o;
//
//        return this.number == (wire.number) &&
//                this.color.equals(wire.color) &&
//                this.wireArea.equals(wire.wireArea) &&
//                this.length.equals(wire.length);
//    }
}