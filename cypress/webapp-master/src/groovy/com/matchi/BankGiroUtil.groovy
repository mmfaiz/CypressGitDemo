package com.matchi


import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.LocalDate
import org.notima.bg.BgMaxFile
import org.notima.bg.bgmax.BgMaxReceipt
import org.notima.bg.bgmax.BgMaxReference
import org.notima.bg.bgmax.BgMaxSet

import java.nio.charset.Charset

class BankGiroUtil {
    private static final Log log = LogFactory.getLog(BankGiroUtil)
    private static final allowedReferenceTypes = [
            BgMaxReference.REFTYPE_OCR,
            BgMaxReference.REFTYPE_OCR_INCORRECT,
            BgMaxReference.REFTYPE_OCR_NOCHECK
    ]

    /**
     * Retrieves payment records from a BGMax file
     * Only fetching records of referenceType = 2, e.g one controlled reference
     * @param BGMax file
     * @return Payment records as Map with key OCR-number (record-reference) with values amount & date
     */
    public static HashMap getBgMaxRecords(File file) {
        BgMaxFile bgMaxFile = new BgMaxFile()
        bgMaxFile.readFromFile(file, Charset.defaultCharset())

        def records = [:]

        bgMaxFile.records.each { BgMaxSet set ->
            set.records.each { BgMaxReceipt receipt ->
                if (receipt.reference && allowedReferenceTypes.contains(receipt.referenceType)) {
                    records.put(receipt.reference, [amount: receipt.amount.toBigDecimal(), date: new LocalDate(receipt.getTransactionDate())])
                }
            }
        }
        return records
    }

    /**
     * Retrieves payment records from a BBS file
     *
     * @param BBS file
     * @return Payment records as Map with key OCR-number (record-reference) with values amount & date
     */
    static HashMap getBBSRecords(File file) {
        def transactions = [:]
        try {
            def linesInFile = file.readLines("UTF-8")

            def skippedLineIndexes = []
            linesInFile[2..-3].eachWithIndex { String line, int index ->
                try {
                    if (!skippedLineIndexes.contains(index)) {
                        def transactionType = line[4..5]
                        if (!["20", "21"].contains(transactionType)) {
                            //KID
                            def kid = line[49..73].replaceAll(' ', '')
                            transactions[kid] = [amount: line[32..48].toBigDecimal() * 0.01,
                                                 date  : new LocalDate(Date.parse("ddMMyy", line[15..20]))]
                            skippedLineIndexes << index + 1
                        } else {
                            skippedLineIndexes << index + 1 << index + 2
                        }
                    }
                } catch (ex) {
                    log.error "Cannot process transaction line ${line} with index: ${index}", ex
                }
            }
        } catch (e) {
            log.error "Failed to process BBS file: ${e.message}", e
        }

        transactions
    }
}
