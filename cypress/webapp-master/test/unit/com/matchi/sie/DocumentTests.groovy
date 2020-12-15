package com.matchi.sie

import com.matchi.sie.Document.DataSource
import com.matchi.sie.Parser.Entry
import com.matchi.sie.Parser.SieFile
import net.fortuna.ical4j.model.DateRange
import org.apache.commons.lang.StringUtils
import org.junit.Before
import org.junit.After
import org.junit.Test

import java.text.DateFormat
import java.text.SimpleDateFormat

class DocumentTests extends GroovyTestCase {

    public SieFile sieFile = new SieFile()
    protected TimeZone originalTimeZone

    class TestDataSource extends DataSource
    {
        @Override
        balanceBefore(Integer accountNumber, Date date) {
            if (accountNumber == 9999) {
                // So we can test empty balances.
                return null
            } else {
                // Faking a fetch based on date and number.
                Float number = (float) Integer.parseInt(date.format("d"))
                return accountNumber + (number*100)
            }
        }
    }
    @Before
    void setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone('UTC'))
        this.sieFile = new SieFile()
    }

    @After
    void tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    protected buildVoucher(Map attributes)
    {
        Map defaults = [
                "creditor" : true,
                "type" : "payment",
                "number" : 1,
                "bookedOn" : new Date(),
                "description" : "A voucher",
                "voucherLines" : [
                        buildVoucherLine(),
                        buildVoucherLine(),
                ],
        ]
        Map map3 = [:]
        map3.put(attributes,defaults)
        return map3
    }
    static buildVoucherLine(Map attributes)
    {
        Map defaults = [
                "accountNumber" : 1234,
                "amount" : 1,
                "bookedOn" : new SimpleDateFormat("yMd").format(new Date()),
                "description" : "A voucher line"
        ]
        Map map3 = [:]

        map3.put(attributes,defaults)
        return map3
    }
    protected entryAttribute(String label, String attribute)
    {
        return this.indexedEntryAttribute(label, 0, attribute)
    }
    protected indexedEntryAttribute(String label, Integer index, String attribute)
    {
        Map attributes = this.indexedEntryAttributes(label, index)
        if (attributes == null) {
            throw new Exception("Unknown attribute " +label+ " in indexedEntryAttribute" )
        }
        return attributes[attribute]
    }
    protected indexedEntryAttributes(String label, Integer index)
    {
        Map indexed = this.indexedEntry(label, index)?.attributes
        return indexed
    }
    protected indexedVoucherEntries(Integer index)
    {
        List<Entry> entry = this.indexedEntry("ver", index)?.entries
        return entry
    }
    protected indexedEntry(String label, Integer index)
    {
        List<Entry> entriesWithLabel = this.sieFile.entriesWithLabel(label)
        return entriesWithLabel[index]
    }

    protected financialYears()
    {
        DateFormat df = new SimpleDateFormat("yyyyMMdd")
        return [



                new DateRange(df.parse("20130101"),df.parse("20131231")),
                new DateRange(df.parse("20120101"),df.parse("20121231")),
                new DateRange(df.parse("20110101"),df.parse("20111231")),
        ]
    }
    static generatedOn()
    {
        return (new Date().minus(1).format("YMd")) // Date.yesterday
    }
    static accounts()
    {
        return [
                [
                        "number": 1500,
                        "description": "Customer ledger"
                ]
        ]
    }
    static vouchers()
    {
        return [
                [
                        "creditor": false,
                        "type": "invoice",
                        "number": 1,
                        "bookedOn" : "20110903",
                        "description" : "Invoice 1",
                        "voucherLines" : [
                                [
                                        "accountNumber" : 1500,
                                        "amount" : 512.0,
                                        "bookedOn" : "20110903",
                                        "description" : "Item 1",
                                        "dimensions" : [6 : 1]
                                ],
                                [
                                        "accountNumber" : 3100,
                                        "amount" : -512.0,
                                        "bookedOn" : "20110903",
                                        "description" : "Item 1",
                                        "dimensions" : [6 : 1]
                                ],
                        ]
                ],
                [
                        "creditor" : true,
                        "type" : "payment",
                        "number" : 2,
                        "bookedOn" : "20120831",
                        "description" : "Payout 1",
                        "voucherLines" : [
                                [
                                        "accountNumber" : 2400,
                                        "amount" : 256.0,
                                        "bookedOn" : "20120831",
                                        "description" : "Payout line 1"
                                ],
                                [
                                        "accountNumber" : 1970,
                                        "amount" : -256.0,
                                        "bookedOn" : "20120831",
                                        "description" : "Payout line 2"
                                ],
                        ]
                ]
        ]
    }
    static dimensions()
    {
        return [
                [
                        "number" : 6,
                        "description" : "Project",
                        "objects" : [
                                ["number" : 1, "description" : "Education"]
                        ]
                ]
        ]
    }
    protected doc()
    {
        DataSource dataSource = new TestDataSource(
                [
                        "accounts" : accounts(),
                        "vouchers" : vouchers(),
                        "program" : "Foonomic",
                        "programVersion" : "3.11",
                        "generatedOn" : generatedOn(),
                        "companyName" : "Foocorp",
                        "financialYears" : this.financialYears(),
                        "balanceAccountNumbers" : [1500, 2400, 9999],
                        "closingAccountNumbers" : [3100, 9999],
                        "dimensions" : dimensions(),
                ]
        )
        return (new Document(dataSource))
    }
    protected doc1() {
        DataSource dataSource = new TestDataSource(
                [
                        "accounts" : [["number": 1500, "description": StringUtils.repeat("k", 101)]],
                        "vouchers" : [[
                                              "creditor" : true,
                                              "type" : "payment",
                                              "number" : 1,
                                              "bookedOn" : new Date(),
                                              "description" : StringUtils.repeat("d", 101),
                                              "voucherLines" : [
                                                      [
                                                              "accountNumber" : 1234,
                                                              "amount" : 1,
                                                              "bookedOn" : new SimpleDateFormat("yMd").format(new Date()),
                                                              "description" : StringUtils.repeat("v", 101)
                                                      ],
                                                      [
                                                              "accountNumber" : 1234,
                                                              "amount" : 1,
                                                              "bookedOn" : new SimpleDateFormat("yMd").format(new Date()),
                                                              "description" : "Payout line 2"
                                                      ],
                                              ]
                                      ]
                        ]
                ]
        )

        return (new Document(dataSource))
    }
    protected doc2() {
        DataSource dataSource = new TestDataSource(
                [
                        "vouchers" : [[
                                              "creditor" : true,
                                              "type" : "payment",
                                              "number" : 1,
                                              "bookedOn" : new Date(),
                                              "description" : "A voucher",
                                              "voucherLines" : [
                                                      [
                                                              "accountNumber" : 1234,
                                                              "amount" : 0,
                                                              "bookedOn" : new SimpleDateFormat("yMd").format(new Date()),
                                                              "description" : "A voucher line"
                                                      ],
                                                      [
                                                              "accountNumber" : 1234,
                                                              "amount" : 0,
                                                              "bookedOn" : new SimpleDateFormat("yMd").format(new Date()),
                                                              "description" : "A voucher line"
                                                      ]
                                              ],
                                      ]]
                ]
        )
        return (new Document(dataSource))
    }
    protected doc3() {
        DataSource dataSource = new TestDataSource(
                [
                        "vouchers" : [[
                                              "creditor" : true,
                                              "type" : "payment",
                                              "number" : 1,
                                              "bookedOn" : new Date(),
                                              "description" : "A voucher",
                                              "series" : "X",
                                              "voucherLines" : [
                                                      [
                                                              "accountNumber" : 1234,
                                                              "amount" : 0,
                                                              "bookedOn" : new SimpleDateFormat("yMd").format(new Date()),
                                                              "description" : "A voucher line"
                                                      ],
                                                      [
                                                              "accountNumber" : 1234,
                                                              "amount" : 0,
                                                              "bookedOn" : new SimpleDateFormat("yMd").format(new Date()),
                                                              "description" : "A voucher line"
                                                      ]
                                              ],
                                      ]]
                ]
        )
        return (new Document(dataSource))
    }
    protected setTestSieFile()
    {
        Document doc = new Document()
        doc = this.doc()
        String render = doc.render()

        this.sieFile =  (new Parser()).parse(render)
    }
    protected setTestSieFile1()
    {
        Document doc = new Document()
        doc = this.doc1()
        String render = doc.render()

        this.sieFile =  (new Parser()).parse(render)
    }
    protected setTestSieFile2() {
        Document doc = new Document()
        doc = this.doc2()
        String render = doc.render()

        this.sieFile = (new Parser()).parse(render)
    }
    protected setTestSieFile3() {
        Document doc = new Document()
        doc = this.doc3()
        String render = doc.render()

        this.sieFile = (new Parser()).parse(render)
    }
    @Test
    void testAddsAHeader()
    {

        this.setTestSieFile()

        assert this.entryAttribute("flagga", "x").equals("0")
        assert this.entryAttribute("program", "programnamn").equals("Foonomic")
        assert this.entryAttribute("program", "version").equals("3.11")
        assert this.entryAttribute("format", "PC8").equals("PC8")
        assert this.entryAttribute("gen", "datum").equals(generatedOn())
        assert this.entryAttribute("sietyp", "typnr").equals("4")
        assert this.entryAttribute("fnamn", "foretagsnamn").equals("Foocorp")
    }

    void testHasAccountingYears()
    {
        this.setTestSieFile()

        assertEquals("0", this.indexedEntryAttribute("rar", 0, "arsnr"))
        assertEquals("20130101", this.indexedEntryAttribute("rar", 0, "start"))
        assertEquals("20131231", this.indexedEntryAttribute("rar", 0, "slut"))
        assertEquals("-1", this.indexedEntryAttribute("rar", 1, "arsnr"))
        assertEquals("20120101", this.indexedEntryAttribute("rar", 1, "start"))
        assertEquals("20121231", this.indexedEntryAttribute("rar", 1, "slut"))
        assertEquals("-2", this.indexedEntryAttribute("rar", 2, "arsnr"))
        assertEquals("20110101", this.indexedEntryAttribute("rar", 2, "start"))
        assertEquals("20111231", this.indexedEntryAttribute("rar", 2, "slut"))
    }

    void testHasAccounts()
    {
        this.setTestSieFile()
        assert this.indexedEntryAttributes("konto", 0).equals(["kontonr" : "1500", "kontonamn" : "Customer ledger"])

    }

    void testHasDimensions()
    {
        this.setTestSieFile()

        assert this.indexedEntryAttributes("dim", 0).equals(["dimensionsnr" : "6", "namn" : "Project"])
    }

    void testHasObjects()
    {
        this.setTestSieFile()

        assert this.indexedEntryAttributes("objekt", 0).equals(["dimensionsnr" : "6", "objektnr" : "1", "objektnamn" : "Education"])
    }

    // ingående balans
    void testHasBalancesBroughtForward()
    {
        this.setTestSieFile()
        assert !this.indexedEntryAttributes("ib", 0).equals(["arsnr" : "0", "konto" : "9999", "saldo" : ""])
        assert this.indexedEntryAttributes("ib", 0).equals(["arsnr" : "0", "konto" : "1500", "saldo" : "1600.0"])
        assert this.indexedEntryAttributes("ib", 1).equals(["arsnr" : "0", "konto" : "2400", "saldo" : "2500.0"])
        assert this.indexedEntryAttributes("ib", 2).equals(["arsnr" : "-1", "konto" : "1500", "saldo" : "1600.0"])
        assert this.indexedEntryAttributes("ib", 3).equals(["arsnr" : "-1", "konto" : "2400", "saldo" : "2500.0"])
        assert this.indexedEntryAttributes("ib", 4).equals(["arsnr" : "-2", "konto" : "1500", "saldo" : "1600.0"])
        assert this.indexedEntryAttributes("ib", 5).equals(["arsnr" : "-2", "konto" : "2400", "saldo" : "2500.0"])
    }

    // utgående balans
    void testHasBalancesCarriedForward()
    {
        this.setTestSieFile()
        assert !this.indexedEntryAttributes("ub", 0).equals(["arsnr" : "0", "konto" : "9999", "saldo" : ""])
        assert this.indexedEntryAttributes("ub", 0).equals(["arsnr" : "0", "konto" : "1500", "saldo" : "4600.0"])
        assert this.indexedEntryAttributes("ub", 1).equals(["arsnr" : "0", "konto" : "2400", "saldo" : "5500.0"])
        assert this.indexedEntryAttributes("ub", 2).equals(["arsnr" : "-1", "konto" : "1500", "saldo" : "4600.0"])
        assert this.indexedEntryAttributes("ub", 3).equals(["arsnr" : "-1", "konto" : "2400", "saldo" : "5500.0"])
        assert this.indexedEntryAttributes("ub", 4).equals(["arsnr" : "-2", "konto" : "1500", "saldo" : "4600.0"])
        assert this.indexedEntryAttributes("ub", 5).equals(["arsnr" : "-2", "konto" : "2400", "saldo" : "5500.0"])
    }
    // saldo för resultatkonto
    void testHasClosingAccountBalances()
    {
        this.setTestSieFile()
        assert !this.indexedEntryAttributes("res", 0).equals(["ars" : "0", "konto" : "9999", "saldo" : ""])
        assert this.indexedEntryAttributes("res", 0).equals(["ars" : "0", "konto" : "3100", "saldo" : "6200.0"])
        assert this.indexedEntryAttributes("res", 1).equals(["ars" : "-1", "konto" : "3100", "saldo" : "6200.0"])
        assert this.indexedEntryAttributes("res", 2).equals(["ars" : "-2", "konto" : "3100", "saldo" : "6200.0"])
    }


    void testHasVouchers()
    {
        this.setTestSieFile()
        assert this.indexedEntry("ver", 0).attributes.equals(["serie" : "KF", "vernr" : "1", "verdatum" : "20110903", "vertext" : "Invoice 1"])
        assert this.indexedVoucherEntries(0)[0].attributes.equals(["kontonr" : "1500", "belopp" : "512.0", "transdat" : "20110903", "transtext" : "Item 1", "objektlista" : [["dimensionsnr" : "6", "objektnr" : "1"]]])
        assert this.indexedVoucherEntries(0)[1].attributes.equals(["kontonr" : "3100", "belopp" : "-512.0", "transdat" : "20110903", "transtext" : "Item 1", "objektlista" : [["dimensionsnr" : "6", "objektnr" : "1"]]])

        assert this.indexedEntry("ver", 1).attributes.equals(["serie" : "KB", "vernr" : "2", "verdatum" : "20120831", "vertext" : "Payout 1"])
        assert this.indexedVoucherEntries(1)[0].attributes.equals(["kontonr" : "2400", "belopp" : "256.0", "transdat" : "20120831", "transtext" : "Payout line 1", "objektlista" :[] ])
        assert this.indexedVoucherEntries(1)[1].attributes.equals(["kontonr" : "1970", "belopp" : "-256.0", "transdat" : "20120831", "transtext" : "Payout line 2", "objektlista" :[] ])

    }
    void testTruncatesReallyLongDescriptions()
    {
        this.setTestSieFile1()

        assert this.indexedEntryAttributes("konto",0).equals(["kontonr" : "1500", "kontonamn" : StringUtils.repeat("k", 100)])
        assert this.indexedEntry("ver",0).attributes.vertext.equals(StringUtils.repeat("d", 100))
        assert this.indexedVoucherEntries(0)[0].attributes.transtext.equals(StringUtils.repeat("v", 100))
    }
    void testEnsuresThereAreAtLeastTwoLinesWithAZeroedSingleVoucherLine()
    {
        this.setTestSieFile2()
        assert this.indexedVoucherEntries(0).size().equals(2)
    }

    void testReadsTheSeriesFromTheVoucherWithASeriesDefined()
    {
        this.setTestSieFile3()
        this.indexedEntry("ver",0).attributes.serie.equals("X")
    }

}