/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import grails.util.GrailsNameUtils
import org.codehaus.groovy.grails.validation.DomainClassPropertyComparator
import org.codehaus.groovy.grails.validation.ConstrainedProperty

includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsCreateArtifacts")

generateForName = null

target(main: "Generates i18n messages for domain classes") {
    depends(checkVersion, parseArguments, packageApp)
    promptForName(type: "Domain Class")
    generateI18nMessages()
}

setDefaultTarget(main)

target(generateI18nMessages: "The implementation target") {
    try {
        def name = argsMap["params"][0]
        if (!name || name == "*") {
            uberGenerate()
        }
        else {
            generateForName = name
            generateForOne()
        }
    }
    catch (Exception e) {
        logError("Error running generate-i18n-messages", e)
        exit(1)
    }
}

target(generateForOne: "Generates i18n messages for only one domain class") {
    depends(loadApp)

    def name = generateForName
    name = name.indexOf('.') > -1 ? name : GrailsNameUtils.getClassNameRepresentation(name)
    def domainClass = grailsApp.getDomainClass(name)

    if (!domainClass) {
        println "Domain class not found in grails-app/domain, trying hibernate mapped classes..."
        bootstrap()
        domainClass = grailsApp.getDomainClass(name)
    }

    if (domainClass) {
        generateForDomainClass(domainClass)
        event("StatusFinal", ["Finished generation for domain class ${domainClass.fullName}. Copy messages to appropriate resource bundle(s)"])
    }
    else {
        event("StatusFinal", ["No domain class found for name ${name}. Please try again and enter a valid domain class name"])
    }
}

target(uberGenerate: "Generates i18n messages for all domain classes") {
    depends(loadApp)

    def domainClasses = grailsApp.domainClasses

    if (!domainClasses) {
        println "No domain classes found in grails-app/domain, trying hibernate mapped classes..."
        bootstrap()
        domainClasses = grailsApp.domainClasses
    }

   if (domainClasses) {
        domainClasses.each { domainClass ->
            generateForDomainClass(domainClass)
        }
        event("StatusFinal", ["Finished generation for domain classes. Copy messages to appropriate resource bundle(s)"])
    }
    else {
        event("StatusFinal", ["No domain classes found"])
    }
}

def generateForDomainClass(domainClass) {
    def file = new File("grails-app/i18n/messages.properties ")
    // print generic messages for this domain class
    file << "# ${domainClass.shortName} messages\n"
    file << "${domainClass.propertyName}.label=${domainClass.shortName}\n"
    file << "${domainClass.propertyName}.create.label=Create ${domainClass.shortName}\n"
    file << "${domainClass.propertyName}.edit.label=Edit ${domainClass.shortName}\n"
    file << "${domainClass.propertyName}.list.label=${domainClass.shortName} List\n"
    file << "${domainClass.propertyName}.new.label=New ${domainClass.shortName}\n"
    file << "${domainClass.propertyName}.show.label=Show ${domainClass.shortName}\n"
    file << "${domainClass.propertyName}.created=${domainClass.shortName} {0} created\n"
    file << "${domainClass.propertyName}.updated=${domainClass.shortName} {0} updated\n"
    file << "${domainClass.propertyName}.deleted=${domainClass.shortName} {0} deleted\n"
    file << "${domainClass.propertyName}.not.found=${domainClass.shortName} not found with id {0}\n"
    file << "${domainClass.propertyName}.not.deleted=${domainClass.shortName} not deleted with id {0}\n"
    file << "${domainClass.propertyName}.optimistic.locking.failure=Another user has updated this ${domainClass.shortName} while you were editing\n"

    // print messages for all properties contained by domain class
    props = domainClass.properties.findAll { it.name != 'version' }
    Collections.sort(props, new org.codehaus.groovy.grails.validation.DomainClassPropertyComparator(domainClass))
    props.each { p ->
        file << "${domainClass.propertyName}.${p.name}.label=${p.naturalName}\n"

        // print messages for inList constaint values
        cp = domainClass.constrainedProperties[p.name]
        if (cp?.inList) {
            cp.inList.each { v ->
                file << "${domainClass.propertyName}.${p.name}.${v}=${v}\n"
            }
        }

        // print error messages for constraints
        cp?.appliedConstraints?.each { c ->
            switch (c.name) {
                case ConstrainedProperty.BLANK_CONSTRAINT:
                    if (!c.parameter)
                        file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] cannot be blank\n"
                    break
                case ConstrainedProperty.CREDIT_CARD_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is not a valid credit card number\n"
                    break
                case ConstrainedProperty.EMAIL_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is not a valid e-mail address\n"
                    break
                case ConstrainedProperty.IN_LIST_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is not contained within the list [{3}]\n"
                    break
                case ConstrainedProperty.MATCHES_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] does not match the required pattern [{3}]\n"
                    break
                case ConstrainedProperty.MAX_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] exceeds maximum value [{3}]\n"
                    break
                case ConstrainedProperty.MAX_SIZE_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] exceeds the maximum size of [{3}]\n"
                    break
                case ConstrainedProperty.MIN_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is less than minimum value [{3}]\n"
                    break
                case ConstrainedProperty.MIN_SIZE_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is less than the minimum size of [{3}]\n"
                    break
                case ConstrainedProperty.NOT_EQUAL_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] cannot equal [{3}]\n"
                    break
                case ConstrainedProperty.NULLABLE_CONSTRAINT:
                    if (!c.nullable)
                        file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] cannot be null\n"
                    break
                case ConstrainedProperty.RANGE_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] does not fall within the valid range from [{3}] to [{4}]\n"
                    break
                case ConstrainedProperty.SIZE_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] does not fall within the valid size range from [{3}] to [{4}]\n"
                    break
                //case ConstrainedProperty.UNIQUE_CONSTRAINT: // unique constraint reference not available
                //    println "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] must be unique"
                //    break
                case ConstrainedProperty.URL_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] is not a valid URL\n"
                    break
                case ConstrainedProperty.VALIDATOR_CONSTRAINT:
                    file << "${domainClass.propertyName}.${p.name}.${c.name}.error=Property [${p.naturalName}] of class [${domainClass.shortName}] with value [{2}] does not pass custom validation\n"
                    break
            }
        }
    }
    file << "\n"
}
