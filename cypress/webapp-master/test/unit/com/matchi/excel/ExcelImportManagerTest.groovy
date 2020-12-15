package com.matchi.excel

import grails.plugin.jxl.builder.ExcelBuilder
import org.junit.Before
import org.junit.Test
/**
 * Created by koloritnij on 31.03.15.
 */
@Mixin(ExcelBuilder)
class ExcelImportManagerTest extends GroovyTestCase {

    def excelImportManager

    @Before
    void setUp() {
        excelImportManager = new ExcelImportManager()
    }

    @Test
    void testParseFileTitles(){
        Character nonPrintable = 0x03
        File file = File.createTempFile('test', '.xls')
        workbook(file.path) { //Create a writable workbook at the location
            sheet("My worksheet") { //Create a worksheet with the provided name
                cell(0, 0, "123"+nonPrintable)
            }
        }
       assert "123".equals(excelImportManager.retrieveColumnTitles(file)?.getAt(0))
    }
}
