package com.example.adira;

public class EmailQueue {
	int rowid;
	String subject;
	String body;

	public EmailQueue() {
		// Empty Constructor
	}

	public EmailQueue(int rowid, String subject, String body) {
		this.rowid = rowid;
		this.subject = subject;
		this.body = body;
	}

	public int getRowid() {
		return rowid;
	}

	public void setRowid(int rowid) {
		this.rowid = rowid;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
