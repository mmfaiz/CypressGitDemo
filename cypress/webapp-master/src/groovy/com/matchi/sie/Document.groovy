package com.matchi.sie

import jline.internal.Log
import net.fortuna.ical4j.model.DateRange
import com.matchi.sie.Document.*
import org.apache.commons.lang.StringUtils

class Document
{
    // Because some accounting software have limits
    // - Fortnox should handle 200
    //  - Visma etc -> 100
    public static final DESCRIPTION_LENGTH_MAX = 100
    public static final String FLAGGA = "FLAGGA"
    public static final String PROGRAM = "PROGRAM"
    public static final String FORMAT = "FORMAT"
    public static final String GEN = "GEN"
    public static final String SIETYP = "SIETYP"
    public static final String FNAMN = "FNAMN"
    public static final String PC8 = "PC8"
    public static final String RAR = "RAR"
    public static final String KONTO = "KONTO"
    public static final String IB = "IB"
    public static final String UB = "UB"
    public static final String RES = "RES"
    public static final String DIM = "DIM"
    public static final String VER = "VER"
    public static final String OBJEKT = "OBJEKT"
    public static final String TRANS = "TRANS"
    public static final String SIE_TYPE4_DATE_FORMAT = "yyyyMMdd"

    /** @var DataSource */
    public DataSource dataSource

    Document() {
        super()
    }

    Document(DataSource dataSource)
    {
        this.dataSource = dataSource
    }

    String render()
    {
        this.renderer = new Renderer()
        this.addHeader()
        this.addFinancialYears()
        this.addAccounts()
        this.addDimensions()
        this.addBalances()
        this.addVouchers()

        return this.renderer.render()
    }
    private addHeader()
    {
        this.renderer.addLine(FLAGGA, [0])
        this.renderer.addLine(PROGRAM, [this.dataSource.program, this.dataSource.programVersion])
        this.renderer.addLine(FORMAT, [PC8])
        this.renderer.addLine(GEN, [this.dataSource.generatedOn])
        this.renderer.addLine(SIETYP, [4])
        this.renderer.addLine(FNAMN, [this.dataSource.companyName])
    }
    private addFinancialYears()
    {
        this.financialYears().eachWithIndex { DateRange dateRange, index ->
            this.renderer.addLine(RAR, [(-index).toString(), dateRange.getRangeStart().format(SIE_TYPE4_DATE_FORMAT).toString(), dateRange.getRangeEnd().format(SIE_TYPE4_DATE_FORMAT).toString()])

        }
    }
    private addAccounts()
    {

        this.dataSource.accounts.each{ account ->
            String number = account["number"]
            String description = account["description"]
            description = StringUtils.left(description, DESCRIPTION_LENGTH_MAX)
            this.renderer.addLine(KONTO, [number,description])
        }
    }
    private addBalances()
    {
        this.financialYears().eachWithIndex { DateRange dateRange, index ->
            this.addBalanceRows(IB, -index, this.dataSource.balanceAccountNumbers, dateRange.getRangeStart())
            this.addBalanceRows(UB, -index, this.dataSource.balanceAccountNumbers, dateRange.getRangeEnd())
            this.addBalanceRows(RES, -index, this.dataSource.closingAccountNumbers, dateRange.getRangeEnd())
        }
    }
    private addBalanceRows(String label, Integer yearIndex, List<Integer> accountNumbers, Date date)
    {
        accountNumbers.each { accountNumber ->
            String balance = this.dataSource.balanceBefore(accountNumber, date)

            // Accounts with no balance should not be in the SIE-file.
            // See paragraph 5.17 in the SIE file format guide (Rev. 4B).
            if (!balance) {
                return
            }
            this.renderer.addLine(label, [yearIndex.toString(), accountNumber, balance])
        }
    }
    private addDimensions()
    {
        this.dataSource.dimensions.each { dimension ->
            String dimensionNumber = dimension["number"]
            String dimensionDescription = dimension["description"]
            dimensionDescription = StringUtils.left(dimensionDescription, DESCRIPTION_LENGTH_MAX)
            this.renderer.addLine(DIM, [dimensionNumber, dimensionDescription])
            dimension["objects"].each { object ->
                String objectNumber = object["number"]
                String objectDescription = object["description"]
                this.renderer.addLine(OBJEKT, [dimensionNumber, objectNumber, objectDescription])
            }
        }
    }
    private addVouchers()
    {
        this.dataSource.vouchers.each { Map voucher ->
            this.addVoucher(voucher)
        }
    }
    private addVoucher(Map opts)
    {
        String number = opts.number
        String bookedOn = opts.bookedOn
        String description = StringUtils.left(opts.description, DESCRIPTION_LENGTH_MAX)
        Map voucherLines = [:]
        String voucherSeries = ""

        if (opts!= null && opts.containsKey("series")) {
            voucherSeries = opts.series
        }
        else {
            Boolean creditor = opts.creditor
            String type = opts.type
            voucherSeries = (new VoucherSeries()).selfFor(creditor,type)
        }
        this.renderer.addLine(VER, [voucherSeries,number,bookedOn,description])
        this.renderer.addBeginningOfArray()

        opts.voucherLines.each {
            voucher ->
                String accountNumber = voucher.accountNumber
                String amount = voucher.amount
                bookedOn = voucher.bookedOn
                Map dimensions = [:]

                if(voucher.dimensions) {
                    dimensions = voucher.dimensions
                }
                description = StringUtils.left(voucher.description, DESCRIPTION_LENGTH_MAX)
                this.renderer.addLine(TRANS, [accountNumber, dimensions, amount, bookedOn, description])
                // Some consumers of SIE cannot handle single voucher lines (fortnox), so add another empty one to make
                // it balance. The spec just requires the sum of lines to be 0, so single lines with zero amount would conform,
                // but break for these implementations.
                if (voucherLines.size() < 2 && amount == 0) {
                    this.renderer.addLine(TRANS, [accountNumber, dimensions, amount, bookedOn, description])
                }
        }
        this.renderer.addEndOfArray()
    }
    /** @var Renderer */
    private Renderer renderer
    private renderer()
    {
        if (!this.renderer) {
            this.renderer = new Renderer()
        }
        return this.renderer
    }
    private financialYears()
    {
        List<DateRange> financialYears = this.dataSource.financialYears
        return financialYears//.sort(financialYears, DateTimeRange)//need fix reverse
    }
}
