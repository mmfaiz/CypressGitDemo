package com.matchi.statistic

import grails.test.GrailsUnitTestCase

class GraphTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testToString() {
		Line line1=new Line("topic",[0,-1,2,3,4,5])
		Line line2=new Line("topic",[5,5,4,5,5,5])
		Line line3=new Line("topic",[5,2,2,4,1,5])
		Graph graph=new Graph("topic","startTime","endTime",["q","w","e"] ,[line1,line2,line3])
		def test=graph.getLineText();
		System.out.println(test)
		assertEquals(test,"{ name: 'topic',data: [0, -1, 2, 3, 4, 5]}{ name: 'topic',data: [5, 5, 4, 5, 5, 5]}{ name: 'topic',data: [5, 2, 2, 4, 1, 5]}")
    }
	
	
}
