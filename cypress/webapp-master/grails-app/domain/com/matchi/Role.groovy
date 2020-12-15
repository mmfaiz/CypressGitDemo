package com.matchi

class Role implements Serializable {

	private static final long serialVersionUID = 12L

	String authority

	static mapping = {
		cache true
	}

	static constraints = {
		authority blank: false, unique: true
	}

	String toString() { "$authority" }
}
