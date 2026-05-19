package org.edx.mobile.model;

public class ChatBotRequestBody {
        private String text;
        private String lang;

    public ChatBotRequestBody(String field,String lang) {
        this.text = field;
        this.lang=lang;
    }

    public void setField(String field,String lang) {
        this.text= field;
        this.lang=lang;
    }
}
