package com.matchi.admin

import com.matchi.Facility
import com.matchi.FacilityContract
import com.matchi.FacilityContractItem
import com.matchi.GenericController
import org.apache.commons.lang3.Range
import org.springframework.dao.DataIntegrityViolationException

import javax.servlet.http.HttpServletResponse

/**
 * @author Sergei Shushkevich
 */
class AdminFacilityContractsController extends GenericController {
    def fortnoxFacadeService
    // Article numbers that we want to use
    def articleRanges = [Range.between(1101, 1140), Range.between(1201, 1307), Range.between(1401, 1550)]

    def index() {
        def facility = Facility.get(params.id)

        if (facility) {
            return [facility: facility, contracts: FacilityContract.findAllByFacility(facility, [sort: "dateValidFrom", order: "desc"])]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def create() {
        [contract: new FacilityContract(params)]
    }

    def save() {
        def contract = new FacilityContract(params)
        if (contract.save(flush: true)) {
            flash.message = message(code: "adminFacilityContracts.save.success")
            redirect(action: "index", id: contract.facility.id)
        } else {
            render(view: "create", model: [contract: contract])
        }
    }

    def edit(Long id) {
        def contract = FacilityContract.get(id)
        if (contract) {
            return [contract: contract]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def update(Long id, Long version) {
        def contract = FacilityContract.get(id)
        if (contract) {
            if (version != null) {
                if (contract.version > version) {
                    contract.errors.rejectValue("version", "facilityContract.optimistic.locking.failure")
                    render(view: "edit", model: [contract: contract])
                    return
                }
            }

            contract.properties = params
            if (contract.save(flush: true)) {
                flash.message = message(code: "adminFacilityContracts.update.success")
                redirect(action: "index", id: contract.facility.id)
            } else {
                render(view: "edit", model: [contract: contract])
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def copy(Long id) {
        def contract = FacilityContract.get(id)
        if (contract) {
            def newContract = new FacilityContract(params)
            newContract.facility = contract.facility
            newContract.dateValidFrom = new Date()
            newContract.save()

            contract.items?.each {
                def newContractItem = new FacilityContractItem()
                newContractItem.properties = it.properties
                newContractItem.contract = newContract

                if (it.chargeMonths) {
                    newContractItem.chargeMonths = new HashSet<>(it.chargeMonths)
                } else {
                    newContractItem.chargeMonths = new HashSet<>()
                }

                newContractItem.save()
            }

            if (newContract.save()) {
                flash.message = message(code: "adminFacilityContracts.update.success")
                redirect(action: "index", id: newContract.facility.id)
            } else {
                render(view: "edit", model: [contract: contract])
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def delete(Long id) {
        def contract = FacilityContract.get(id)
        if (contract) {
            def facilityId = contract.facility.id
            try {
                contract.delete(flush: true)
                flash.message = message(code: "adminFacilityContracts.delete.success")
            } catch (DataIntegrityViolationException e) {
                log.error(e.message, e)
                flash.error = message(code: "adminFacilityContracts.delete.failure")
            }
            redirect(action: "index", id: facilityId)
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def createItem() {
        [articles: getArticles(), item: new FacilityContractItem(params)]
    }

    def saveItem() {
        def item = new FacilityContractItem(params)
        if (item.save(flush: true)) {
            flash.message = message(code: "adminFacilityContracts.saveItem.success")
            redirect(action: "edit", id: item.contract.id)
        } else {
            render(view: "createItem", model: [articles: getArticles(), item: item])
        }
    }

    def editItem(Long id) {
        def item = FacilityContractItem.get(id)
        if (item) {
            return [articles: getArticles(), item: item]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def updateItem(Long id, Long version) {
        def item = FacilityContractItem.get(id)
        if (item) {
            if (version != null) {
                if (item.version > version) {
                    item.errors.rejectValue("version", "facilityContractItem.optimistic.locking.failure")
                    render(view: "editItem", model: [item: item])
                    return
                }
            }

            item.properties = params
            item.account = item.articleNumber ? null : item.account
            if (item.save(flush: true)) {
                flash.message = message(code: "adminFacilityContracts.updateItem.success")
                redirect(action: "edit", id: item.contract.id)
            } else {
                render(view: "editItem", model: [articles: getArticles(), item: item])
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def deleteItem(Long id) {
        def item = FacilityContractItem.get(id)
        if (item) {
            def contractId = item.contract.id
            try {
                item.delete(flush: true)
                flash.message = message(code: "adminFacilityContracts.deleteItem.success")
            } catch (DataIntegrityViolationException e) {
                log.error(e.message, e)
                flash.error = message(code: "adminFacilityContracts.deleteItem.failure")
            }
            redirect(action: "edit", id: contractId)
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    /**
     * Will only return Fortnox articles within a given article number series
     * in order to not clutter the list in contract item with irrelevant articles.
     * @return
     */
    List getArticles() {
        return fortnoxFacadeService.listMatchiArticles().findAll {
            try {
                def articleNumber = Integer.parseInt(it.getArticleNumber())
                articleRanges.any { range -> range.contains(articleNumber) }
            } catch (NumberFormatException e) {
                log.error("Unable to parse Fortnox article number '${it.getArticleNumber()}' to a number: ${e.getMessage()}")
                false
            }
        }
    }

}
