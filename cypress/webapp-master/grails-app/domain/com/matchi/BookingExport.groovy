package com.matchi

class BookingExport {
    public static final String BOOKING_NEW_MARKER = "1"
    public static final String BOOKING_CANCELLED_MARKER = "0"


	//static belongsTo = [ booking: Booking ]

	String bookingNumber
	String timeCreated
    String filename
    Long facilityId
    String data
    boolean cancelled

    static constraints = {
		bookingNumber nullable: false
		timeCreated nullable: true
        filename nullable: true
        data nullable: false, blank: false
    }
	
	static mapping = {
		//sort "id"
	}
	
	String toString() { "$bookingNumber" }

    def toNewBookingFormat() {
        return "${data}" + BOOKING_NEW_MARKER
    }

    def toCancelledBookingFormat() {
        return "${data}" + BOOKING_CANCELLED_MARKER
    }

  /*  int hashCode() {
        int result;
        result = (bookingNumber != null ? bookingNumber.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }*/
}
