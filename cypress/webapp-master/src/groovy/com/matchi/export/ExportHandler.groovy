package com.matchi.export

import com.matchi.MFile
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

abstract class ExportHandler {
    private static final Log log = LogFactory.getLog(ExportHandler)

    def excelExportManager

    abstract def getExportTitles()
    abstract def getExportData()

    MFile exportToExcel(Map exportMap) {
        return excelExportManager.export( exportMap )
    }

    Map compileExportData(def objects) {
        def data =  []
        def exportData = getExportData()

        exportData.each { def edata ->
            def added = false

            objects.each { def obj ->
                obj.properties.each { def prop ->
                    if (prop.key == edata && prop.value.toString() != "null") {
                        data << prop.value.toString()
                        added = true
                    }
                }
            }

            if (!added) {
                data << ""
            }
        }

        return [ titles: getExportTitles(), data: data ]
    }


}
