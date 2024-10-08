package com.application;

import lombok.Getter;

@Getter
public enum TYPE {
    XLSX("xlsx"),
    XLS("xls");

    private final String title;
    TYPE(String title) {
        this.title = title;
    }
}
