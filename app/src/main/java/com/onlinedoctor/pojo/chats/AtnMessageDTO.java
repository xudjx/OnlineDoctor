package com.onlinedoctor.pojo.chats;

import com.onlinedoctor.pojo.patient.PatientDTO;

import java.io.Serializable;

/** 
 * @author Song
 */

public class AtnMessageDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private PatientDTO patientinfo;
	private int status;
	private long timestamp;
	
	public PatientDTO getPackPatientMessage() {
		return patientinfo;
	}

	public void setPackPatientMessage(PatientDTO patientDTO) {
		this.patientinfo = patientDTO;
	}

	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}


	public long getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public AtnMessageDTO() {

	}
}
