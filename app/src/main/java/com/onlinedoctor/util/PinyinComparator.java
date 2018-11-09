package com.onlinedoctor.util;

import java.util.Comparator;

import com.onlinedoctor.pojo.patient.Patient;
/*
public class PinyinComparator implements Comparator<PatientListModel> {

	public int compare(PatientListModel o1, PatientListModel o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}
}*/

public class PinyinComparator implements Comparator<Patient> {

	public int compare(Patient o1, Patient o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}
}