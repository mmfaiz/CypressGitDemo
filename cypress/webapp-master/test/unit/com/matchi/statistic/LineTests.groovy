package com.matchi.statistic

import grails.test.GrailsUnitTestCase

class LineTests extends GrailsUnitTestCase {
    
	protected void setUp() {
        super.setUp()

    }
    protected void tearDown() {
        super.tearDown()
    }

    void testTopic() {
		Line line;
		String topic="test topic"
		line=new Line(topic,[1,2,3,4,3,2,1])
		assertEquals(topic,line.getTopic())
    }
	void testData() {
		Line line;
		def arr=[-1,2,3,4,3,2,1]
		line=new Line("",arr)
		assertEquals(arr.toString(),line.getArrayOnForm())
	}
	
}
