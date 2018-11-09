package com.onlinedoctor.sqlite.service;

import java.util.List;

import com.onlinedoctor.pojo.patient.Label;

public interface LabelService {

	public int addLabelOne(Label l);
	
	public List<Label> listLabels();
	
	public boolean deleteLabelByName(String name);
	
	public Label getLabelById(long labelId);
	
	public boolean deleteLabelById(long id);
	
	public int updateLabelNameById(long id, String newName);

	public boolean updateLabel(long id, Label label);
}
