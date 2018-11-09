package com.onlinedoctor.pojo.tools.survey;

import com.onlinedoctor.pojo.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Questionnaire {
    private int id;
    private String name;
    private String doctorId;
    private String questionJson;
    private long timestamp;
    public int localId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getQuestionJson() {
        return questionJson;
    }

    public void setQuestionJson(String questionJson) {
        this.questionJson = questionJson;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static Questionnaire fromJO(JSONObject jo) throws JSONException {
        Questionnaire q = new Questionnaire();
        q.setId(jo.getInt("id"));
        q.setName(jo.getString("name"));
        q.setDoctorId(jo.getString("doctorId"));
        q.setQuestionJson(jo.getString("questionJson"));
        q.setTimestamp(jo.getLong("time"));
        return q;
    }

    public ArrayList<BaseQuestionDTO> toQuesionDTOList() {
        ArrayList<BaseQuestionDTO> bqList = new ArrayList();
        //解析Json
        try {
            JSONObject jo = new JSONObject(questionJson);
            JSONArray ja = jo.getJSONArray("qList");
            for (int i = 0; i < ja.length(); i++) {
                JSONObject question = ja.getJSONObject(i);
                switch (question.getInt("type")) {
                    case Common.TYPE_SINGLE_CHOICE:
                        bqList.add(SingleChoiceDTO.fromJO(question));
                        break;
                    case Common.TYPE_MUTIL_CHOICES:
                        bqList.add(MultiChoicesDTO.fromJO(question));
                        break;
                    case Common.TYPE_SUBJECTIVE:
                        bqList.add(SubjectiveDTO.fromJO(question));
                        break;
                    case Common.TYPE_IMAGE:
                        bqList.add(IMG.fromJO(question));
                        break;
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bqList;
    }
}
