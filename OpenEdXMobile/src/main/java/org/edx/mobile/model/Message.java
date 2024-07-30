package org.edx.mobile.model;

public class Message {
    private String text;
    private boolean isUser=false;
    private boolean isSimmerActive=false;
    private boolean isResponse=false;
    private boolean isScrollingEnable=false;

    public Message(String text, boolean isUser,boolean isSimmerActive,boolean isResponse,boolean isScrollingEnable) {
        this.text = text;
        this.isUser = isUser;
        this.isSimmerActive=isSimmerActive;
        this.isResponse=isResponse;
        this.isScrollingEnable=isScrollingEnable;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }
    public boolean isSimmerActive() {
        return isSimmerActive;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUser(boolean user) {
        isUser = user;
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
}
