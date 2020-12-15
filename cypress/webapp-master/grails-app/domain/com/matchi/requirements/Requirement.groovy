package com.matchi.requirements

import com.matchi.Customer

abstract class Requirement {
    
    Date dateCreated
    Date lastUpdated

    static belongsTo = [ profile: RequirementProfile ]

    static constraints = {

    }

    static mapping = {
        tablePerHierarchy false
        tablePerConcreteClass true
        id generator: 'increment'
        version false
    }

    /**
     * Returns a list of the different Requirement implementations
     * @return
     */
    static List<Class<Requirement>> getSubClasses() {
        return [IsActiveMemberRequirement, GroupRequirement, MemberTypeRequirement]
    }

    /**
     * Checks if a string is a valid subclass of Requirement
     * @param name
     * @return
     */
    static boolean isValidClassName(String name) {
        return getSubClasses().any { Class clazz ->
            return clazz.getSimpleName().equals(name)
        }
    }

    static String getRealClassName(String name) {
        return getSubClasses().find { Class clazz ->
            return clazz.getSimpleName().equals(name)
        }.name
    }

    /**
     * Checks if a Customer fulfills this requirement
     * @param customer
     * @return
     */
    abstract boolean validate(Customer customer)

    abstract Map getRequirementProperties()

    abstract void setValues(def p)
}
