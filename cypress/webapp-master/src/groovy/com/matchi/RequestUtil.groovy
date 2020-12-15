package com.matchi

class RequestUtil {

	static toLongList(def collection) {
		def result = []

		collection.each {
			result << Long.parseLong(it.trim())
		}
		return result
	}

    static toListFromString(def strList) {
        if (!strList) {
            return []
        }

        strList = strList.replaceAll(/(\[)/, '')
        strList = strList.replaceAll(/(\])/, '')

        return strList.split(",")
    }

}
