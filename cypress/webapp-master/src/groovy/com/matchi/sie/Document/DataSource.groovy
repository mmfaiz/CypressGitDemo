package com.matchi.sie.Document

import net.fortuna.ical4j.model.DateRange
import org.joda.time.DateTime

abstract class DataSource
{

    public String program = ""
    public String programVersion = ""
    public String generatedOn = ""
    public String companyName = ""
    public List accounts = []
    public List<Integer> balanceAccountNumbers = []
    public List<Integer> closingAccountNumbers = []
    public List vouchers = []
    public List<DateRange> financialYears = []
    public List dimensions = []

    DataSource() {
    }
    DataSource(Map hash) {
        hash.each{ k, v ->
            this.k = v
        }
    }


    /**
     * Used to calculate balance before (and on) the given date for an account.
     * @param accountNumber
     * @param \DateTime date
     * @return mixed
     */
    abstract balanceBefore(Integer accountNumber, Date date)

}
