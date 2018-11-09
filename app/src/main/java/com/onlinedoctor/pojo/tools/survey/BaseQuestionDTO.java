package com.onlinedoctor.pojo.tools.survey;

import java.io.Serializable;
import java.util.List;

public class BaseQuestionDTO implements Serializable {
    private static final long serialVersionUID = -416839965953557548L;
    private int id;
    int type;
    private String title;
    private List<String> choicesList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    protected void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getChoicesList() {
        return choicesList;
    }

    public void setChoicesList(List<String> choicesList) {
        this.choicesList = choicesList;
    }
}
