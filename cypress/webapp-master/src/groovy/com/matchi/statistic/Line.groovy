package com.matchi.statistic

class Line {
	String topic = "temp topic"
	ArrayList xArr = null

	
	Line(String tTopic,ArrayList txArr) {
		xArr = txArr
		topic = tTopic
	}
	public String getTopic() {
		return topic
	}
	public String getArrayOnForm() {
		return xArr.toString()
	}
	public String getLeftScaleName() {
		return "antal bokningar"
	}
}
