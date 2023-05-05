package com.lucas.xbus.Event;

public class ViewEvent {
    private String text;

    public ViewEvent(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
