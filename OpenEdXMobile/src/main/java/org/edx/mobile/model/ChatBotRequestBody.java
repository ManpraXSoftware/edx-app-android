package org.edx.mobile.model;

public class ChatBotRequestBody {
    private String text;

    public ChatBotRequestBody(String field) {
        this.text = field;
    }

    public void setField(String field) {
        this.text= field;
    }
}
