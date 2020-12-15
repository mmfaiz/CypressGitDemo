package com.matchi.statistic

class Graph {

	public String topic
	def endTime, startTime
	ArrayList<String> yAxys// renames
	ArrayList<Line> lines

	Graph(String topic,def startTime,def endTime,ArrayList<String> yAxys ,ArrayList<Line> lines){
		this.topic = topic
		this.endTime = endTime
		this.startTime = startTime
		this.yAxys = yAxys
		this.lines = lines
	}

	String getLineText(){
		String tot=""
		for(line in lines) {
			if( line != null ) {
				tot += "{ "+
					"name: '" + line.topic + "'," +
					"data: " + line.getArrayOnForm() + "}"
			}
		}
		return tot
	}
	Date startDateInDate() {
		return startTime.toDate()
	}
	Date endDateInDate() {
		return endTime.toDate()
	}
}
	

