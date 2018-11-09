package com.onlinedoctor.pojo.tools.survey;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;


public class BaseQuestionDO implements Parcelable{
	private int id = -1;
	private String question;
	private int type; // 选择题为1， 主观题为2
	private int title_id = -1; //对应的disease_survey的id
	public List<String> options = new ArrayList<String>();
	

	public BaseQuestionDO(int id, String question, int type, int title_id) {
		super();
		this.id = id;
		this.question = question;
		this.type = type;
		this.title_id = title_id;
	}

	public BaseQuestionDO(String question, int type) {
		super();
		this.question = question;
		this.type = type;
	}

	public BaseQuestionDO() {
	};

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public int getTitle_id() {
		return title_id;
	}

	public void setTitle_id(int title_id) {
		this.title_id = title_id;
	}

	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(question);
        dest.writeInt(type);
        dest.writeInt(title_id);
        dest.writeList(options);
    }
    
    
    public static final Parcelable.Creator<BaseQuestionDO> CREATOR = new Creator<BaseQuestionDO>() {
    	 
        @Override
        public BaseQuestionDO[] newArray(int size) {
            return new BaseQuestionDO[size];
        }
 
        @Override
        public BaseQuestionDO createFromParcel(Parcel source) {
        	BaseQuestionDO result = new BaseQuestionDO();
            result.id = source.readInt();
            result.question = source.readString();
            result.type = source.readInt();
            result.title_id = source.readInt();
            result.options = source.readArrayList(ArrayList.class.getClassLoader());
            
            return result;
        }
    };
}