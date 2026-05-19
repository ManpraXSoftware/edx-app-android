package org.edx.mobile.model;

import java.util.List;

public class Message {
    private long id;
    private String text;
    private boolean isUser;
    private boolean isSimmerActive;
    private boolean isResponse;
    private boolean isScrollingEnable;
    private List<GridItem> gridItems;

    public Message(long id, String text, boolean isUser, boolean isSimmerActive, boolean isResponse, boolean isScrollingEnable) {
        this(id, text, isUser, isSimmerActive, isResponse, isScrollingEnable, null);
    }

    public Message(long id, String text, boolean isUser, boolean isSimmerActive, boolean isResponse, boolean isScrollingEnable, List<GridItem> gridItems) {
        this.id = id;
        this.text = text;
        this.isUser = isUser;
        this.isSimmerActive = isSimmerActive;
        this.isResponse = isResponse;
        this.isScrollingEnable = isScrollingEnable;
        this.gridItems = gridItems;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public boolean isSimmerActive() {
        return isSimmerActive;
    }

    public void setSimmerActive(boolean simmerActive) {
        isSimmerActive = simmerActive;
    }

    public boolean isResponse() {
        return isResponse;
    }

    public void setResponse(boolean response) {
        isResponse = response;
    }

    public boolean isScrollingEnable() {
        return isScrollingEnable;
    }

    public void setScrollingEnable(boolean scrollingEnable) {
        isScrollingEnable = scrollingEnable;
    }

    public List<GridItem> getGridItems() {
        return gridItems;
    }

    public void setGridItems(List<GridItem> gridItems) {
        this.gridItems = gridItems;
    }

    public boolean hasGridItems() {
        return gridItems != null && !gridItems.isEmpty();
    }
}