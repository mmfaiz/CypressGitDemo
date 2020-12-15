package com.matchi

import com.matchi.coupon.CustomerCoupon
import com.matchi.statistic.FacilityCouponFeeEntry
import com.matchi.statistic.FacilityMembershipDetailEntry
import com.matchi.statistic.FacilityPaymentEntry
import com.matchi.statistic.FacilityPaymentSummary
import com.matchi.statistic.FacilityPromoDiscountEntry
import groovy.sql.GroovyRowResult
import org.joda.time.DateTime
import org.joda.time.Interval

class FacilityStatisticsService {

    def groovySql
    static transactional = false

    def getPaymentStatisticsFor(Facility facility, Interval interval) {

        FacilityContract contract = FacilityContract.activeContract(facility, interval.start.toDate()).list()[0]

        def query = """            
select 
cid as cid, 
fid as fid, 
fname as fname, 
article,
sport,
indoor,
type,
price,
date,
sum(num) as num,
sum(num_credited) as num_credited,
revenue,
sum(total_credited) as total_credited,
sum(total_cancel_fees) as total_credit_fees,
sum(num_api) as num_api,
sum(num_web) as num_web,
coupon_facility as coupon_facility,
grouped_sub_facility as grouped_sub_facility
from

(
select
c.id as cid,
f.id as fid,
f.name as fname,
o.article,
spo.name as sport, 
cou.indoor as indoor,
if(op.type = 'netaxept' or op.type = 'adyen', 'netaxept/adyen', op.type) as type,
o.price,
o.date_delivery as date,
coupon_c.facility_id as coupon_facility,
mt.grouped_sub_facility_id as grouped_sub_facility,

sum(CASE WHEN (op.status = 'CAPTURED') THEN 1 ELSE 0 END) as num,
sum(CASE WHEN (op.status = 'CREDITED') THEN 1 ELSE 0 END) as num_credited,

-- Netaxept and Adyen
CASE WHEN (op.type = 'netaxept' or op.type = 'adyen') THEN (amount) ELSE 0 END as revenue,
sum(CASE WHEN ((op.type = 'netaxept' or op.type = 'adyen') and op.status = 'CREDITED') THEN (amount-credited) ELSE 0 END) as total_cancel_fees,
sum(CASE WHEN ((op.type = 'netaxept' or op.type = 'adyen') and op.status = 'CREDITED') THEN (credited) ELSE 0 END) as total_credited,

-- Origin statistics
sum(CASE WHEN (o.origin = 'api') THEN 1 ELSE 0 END) as num_api,
sum(CASE WHEN (o.origin = 'web') THEN 1 ELSE 0 END) as num_web

FROM
order_payment op
LEFT JOIN
order_order_payments oop on op.id = oop.payment_id
LEFT JOIN
`order` o on oop.order_id = o.id
LEFT JOIN
 
customer_coupon_ticket cct on op.ticket_id = cct.id
LEFT JOIN 
customer_coupon cc on cct.customer_coupon_id = cc.id
LEFT JOIN 
customer coupon_c on cc.customer_id = coupon_c.id
LEFT JOIN

customer c on o.customer_id = c.id
LEFT JOIN 
facility f on c.facility_id = f.id
LEFT JOIN
facility_contract fc on fc.facility_id = f.id
LEFT JOIN 
booking b on b.order_id = o.id
LEFT JOIN 
slot s on b.slot_id = s.id
LEFT JOIN 
court cou on s.court_id = cou.id
LEFT JOIN 
sport spo on cou.sport_id = spo.id
LEFT JOIN 
membership m on m.order_id = o.id
LEFT JOIN 
membership_type mt on mt.id = m.type_id

WHERE
(op.type = 'netaxept' or op.type = 'adyen' or op.type = 'coupon') and
(o.status in ("CONFIRMED", "COMPLETED") or (o.status = "ANNULLED" and op.status = "CAPTURED")) and 
(op.status in ( "CAPTURED" , "CREDITED", "AUTHED" )) and  
o.date_delivery > :start and o.date_delivery < :end
    and c.facility_id = :facilityId
    and (fc.id = :contractId or fc.id is null)
GROUP BY fid, price, article, type, sport, indoor, coupon_facility, mt.grouped_sub_facility_id
) t 

group by 
fid, price, article, type, sport, indoor, coupon_facility, grouped_sub_facility
order by fid, type desc, article, sport, indoor, price;            
            """

        def rows = groovySql.rows(query, [
                start: interval.start.toString("yyyy-MM-dd HH:mm"),
                end: interval.end.toString("yyyy-MM-dd HH:mm"),
                facilityId: facility.id,
                contractId: contract?.id
        ])

        def result = []

        rows.each {
            def entry = new FacilityPaymentEntry()
            entry.facilityId = it.fid
            entry.cid     = it.cid
            entry.article = it.article
            entry.type    = it.type
            entry.price   = it.price
            entry.revenue = it.revenue
            entry.num     = it.num
            entry.numCredited = it.num_credited
            entry.numOriginApi = it.num_api
            entry.numOriginWeb = it.num_web
            entry.numCredited = it.total_credited
            entry.totalCreditFees = it.total_credit_fees
            entry.sport = it.sport
            entry.indoor = it.indoor
            entry.couponFacilityId = it.coupon_facility
            entry.groupedSubFacilityId = it.grouped_sub_facility

            result << entry
        }



        def couponEntries = []
        def giftCardEntries = []
        List<FacilityContractItem> contractItems = []
        List<FacilityPromoDiscountEntry> promoDiscountEntries = getPromoDiscountEntries(facility, interval)
        List<FacilityCouponFeeEntry> detailedOfferEntries = []
        List<FacilityMembershipDetailEntry> membershipEntries = []

        if (contract) {
            List couponCount
            Long total

            if (contract.couponContractType == FacilityContract.CouponContractType.PER_TICKET) {
                couponCount = getCouponBookings(facility, interval, false)
                total = couponCount.sum {
                    Long.parseLong(it[1].toString())
                }
                couponCount.each {
                    detailedOfferEntries << new FacilityCouponFeeEntry(facility: Facility.get(it[0]), type: "variableCouponMediationFee", count: Long.parseLong(it[1].toString()),
                        fee: contract.variableCouponMediationFee, feeType: contract.couponContractType.name())
                }
            } else {
                total = CustomerCoupon.facilityStats(facility, interval.start.toDate(),
                        interval.end.toDate(), false).count()
            }
            if (couponCount) {
                couponEntries << new FacilityCouponFeeEntry(type: "variableCouponMediationFee", count: total,
                        fee: contract.variableCouponMediationFee, feeType: contract.couponContractType.name())
            }

            if (contract.unlimitedCouponContractType == FacilityContract.CouponContractType.PER_TICKET) {
                couponCount = getCouponBookings(facility, interval, true)
                total = couponCount.sum {
                    Long.parseLong(it[1].toString())
                }
                couponCount.each {
                    detailedOfferEntries << new FacilityCouponFeeEntry(facility: Facility.get(it[0]), type: "variableUnlimitedCouponMediationFee", count: Long.parseLong(it[1].toString()),
                        fee: contract.variableUnlimitedCouponMediationFee, feeType: contract.unlimitedCouponContractType.name())
                }
            } else {
                total = CustomerCoupon.facilityStats(facility, interval.start.toDate(),
                        interval.end.toDate(), true).count()
            }
            if (couponCount) {
                couponEntries << new FacilityCouponFeeEntry(type: "variableUnlimitedCouponMediationFee", count: total,
                        fee: contract.variableUnlimitedCouponMediationFee, feeType: contract.unlimitedCouponContractType.name())
            }

            if (contract.giftCardContractType == FacilityContract.GiftCardContractType.PER_USE) {
                couponCount = getCouponBookings(facility, interval, false, "gift_card")
                total = couponCount.sum {
                    Long.parseLong(it[1].toString())
                }
                couponCount.each {
                    detailedOfferEntries << new FacilityCouponFeeEntry(facility: Facility.get(it[0]), type: "variableGiftCardMediationFee", count: Long.parseLong(it[1].toString()),
                        fee: contract.variableGiftCardMediationFee, feeType: contract.giftCardContractType.name())
                }
            } else {
                total = CustomerCoupon.facilityStats(facility, interval.start.toDate(),
                        interval.end.toDate(), false, "gift_card").count()
            }
            if (couponCount) {
                giftCardEntries << new FacilityCouponFeeEntry(type: "variableGiftCardMediationFee", count: total,
                        fee: contract.variableGiftCardMediationFee, feeType: contract.giftCardContractType.name())
            }

            contractItems = getOneTimeContractItems(contract, interval) +
                    getMonthlyContractItems(contract, interval) +
                    getYearlyContractItems(contract, interval)
        }

        if (facility.isMasterFacility()) {
            result.findAll { it.article == "MEMBERSHIP" }.groupBy {
                Facility.get(it.groupedSubFacilityId)
            }.each {
                membershipEntries << new FacilityMembershipDetailEntry(
                    facility: it.key, count: it.value.sum { it.num } as BigDecimal, totalFee: it.value.sum { it.price * it.num } as BigDecimal
                )
            }
        }

        groovySql.close()

        return new FacilityPaymentSummary(facility, result, interval, couponEntries, detailedOfferEntries,
                contractItems, giftCardEntries, promoDiscountEntries, membershipEntries)
    }

    def getMonthlyContractItems(FacilityContract contract, Interval interval) {
        return FacilityContractItem.withCriteria {
            eq("contract", contract)
            eq("type", FacilityContractItem.RecurringType.MONTHLY)
        }.findAll {
            (interval.start.toDate()[Calendar.MONTH]..interval.end.toDate()[Calendar.MONTH]).any { m ->
                it.chargeMonths?.contains(m + 1)
            }
        }
    }
    def getYearlyContractItems(FacilityContract contract, Interval interval) {
        return FacilityContractItem.withCriteria {
            eq("contract", contract)
            eq("type", FacilityContractItem.RecurringType.YEARLY)
            ge("chargeMonth", interval.start.getMonthOfYear())
            le("chargeMonth", interval.end.getMonthOfYear())
        }
    }
    def getOneTimeContractItems(FacilityContract contract, Interval interval) {
        return FacilityContractItem.withCriteria {
            eq("contract", contract)
            eq("type", FacilityContractItem.RecurringType.ONE_TIME_CHARGE)
            ge("chargeDate", interval.start.toDate())
            le("chargeDate", interval.end.toDate())
        }
    }

    private List<FacilityPromoDiscountEntry> getPromoDiscountEntries(Facility facility, Interval interval) {
        def query = """
            SELECT o_r.promo_code, sum(o_r.amount) as total_amount, count(o_r.id) as num
            FROM order_refund o_r
            JOIN `order` o on o_r.order_id = o.id
            WHERE o.facility_id=:facilityId AND o_r.promo_code is not null AND o.status in ("CONFIRMED", "COMPLETED") AND o.date_delivery >= :start  and o.date_delivery <= :end
            GROUP BY o_r.promo_code;          
        """

        def rows = groovySql.rows(query, [
                start: interval.start.toString("yyyy-MM-dd HH:mm"),
                end: interval.end.toString("yyyy-MM-dd HH:mm"),
                facilityId: facility.id,
        ])

        def result = []

        rows.each {
            result << new FacilityPromoDiscountEntry(type: it.promo_code, total: it.total_amount, count: it.num)
        }

        return result
    }

    private List<GroovyRowResult> getCouponBookings(Facility facility, Interval interval,
                                                    Boolean unlimitedCoupons, String discriminator = "coupon") {
        def couponCount = groovySql.rows("""select o.facility_id, count(*)
                from order_payment op
                join order_order_payments oop on op.id = oop.payment_id
                join `order` o on oop.order_id = o.id
                join customer_coupon_ticket cct on op.ticket_id = cct.id
                join customer_coupon cc on cct.customer_coupon_id = cc.id
                join customer c on cc.customer_id = c.id
                join coupon cpn on cc.coupon_id = cpn.id
                where op.type = 'coupon' and op.status = 'CAPTURED'
                    and (o.status = 'CONFIRMED' OR o.status = 'COMPLETED')
                    and o.date_delivery > :start and o.date_delivery < :end
                    and c.facility_id = :facilityId
                    and cpn.unlimited = :unlim
                    and cpn.class = :discr group by o.facility_id""",
            [start: interval.start.toDate(), end: interval.end.toDate(),
             facilityId: facility.id, unlim: unlimitedCoupons, discr: discriminator])

        groovySql.close()
        couponCount
    }

    int getOnlineCouponBookings(long id, DateTime from, DateTime to) {
        String query = """
            select count(*) from `order` o 
                left join order_order_payments oop on o.id = oop.order_id 
                left join order_payment op on oop.payment_id = op.id 
            where 
                facility_id = :id 
                and date_delivery >= :from 
                and date_delivery < :to 
                and o.status in ( 'COMPLETED', 'ANNULLED') 
                and op.type = 'coupon' and o.origin != 'facility'"""

        String fromStr = from.toString("yyyy-MM-dd HH:mm")
        String toStr = to.toString("yyyy-MM-dd HH:mm")

        groovySql.firstRow(query, [id: id, from: fromStr, to: toStr])[0] as int
    }

    def getCreditedBookings(long id, DateTime from, DateTime to) {
        String query = """
            select count(*) as count, op.amount from `order` o 
                left join order_refund ord on ord.order_id = o.id 
                LEFT JOIN order_order_payments oop on oop.order_id = o.id 
                LEFT JOIN order_payment op on oop.payment_id = op.id 
            where o.facility_id = :id
                and o.date_delivery >= :from
                and o.date_delivery < :to
                and o.status in ('ANNULLED') 
                and op.status = 'CREDITED' 
                and (op.amount - op.credited) > 12.5 
            group by op.amount"""

        String fromStr = from.toString("yyyy-MM-dd HH:mm")
        String toStr = to.toString("yyyy-MM-dd HH:mm")

        def rows = groovySql.rows(query, [id: id, from: fromStr, to: toStr])

        groovySql.close()
        return rows.collect {
            [count: it.count, amount: it.amount]
        }
    }

    List getActivitiesGroups(FacilityPaymentSummary summary, BigDecimal price) {
        def query = """select ifnull(a.name, o.description) as activity_name, count(*) as num
                from `order` o
                join order_order_payments oop on oop.order_id = o.id
                join order_payment op on op.id = oop.payment_id
                left join order_metadata om on om.metadata = o.id and om.metadata_idx = 'activityOccasionId'
                left join activity_occasion ao on om.metadata_elt = ao.id
                left join activity a on ao.activity_id = a.id
                left join customer c on o.customer_id = c.id
                left join facility f on c.facility_id = f.id
                left join facility_contract fc on fc.facility_id = f.id
                where
                o.article = 'ACTIVITY' and o.price = :price and
                (o.status in ('CONFIRMED', 'COMPLETED') or (o.status = 'ANNULLED' and op.status = 'CAPTURED')) and
                o.date_delivery > :start and o.date_delivery < :end and
                (op.type = 'netaxept' or op.type = 'adyen') and op.status = 'CAPTURED' and
                c.facility_id = :facilityId and (fc.id = :contractId or fc.id is null)
                group by activity_name"""

        groovySql.rows(query, [
                price: price,
                start: summary.interval.start.toString("yyyy-MM-dd HH:mm"),
                end: summary.interval.end.toString("yyyy-MM-dd HH:mm"),
                facilityId: summary.facility.id,
                contractId: summary.contract?.id])
    }

    List getIncorrectActivitiesGroups(FacilityPaymentSummary summary, BigDecimal price) {
        def query = """select ifnull(a.name, o.description) as activity_name, count(*) as num
                from `order` o
                join order_order_payments oop on oop.order_id = o.id
                join order_payment op on op.id = oop.payment_id
                left join order_metadata om on om.metadata = o.id and om.metadata_idx = 'activityOccasionId'
                left join activity_occasion ao on om.metadata_elt = ao.id
                left join activity a on ao.activity_id = a.id
                left join customer c on o.customer_id = c.id
                left join facility f on c.facility_id = f.id
                left join facility_contract fc on fc.facility_id = f.id
                where
                o.article = 'ACTIVITY' and o.price = :price and
                (op.type = 'netaxept' or op.type = 'adyen') and (op.amount - op.credited) = 12.5 and
                ((o.status = 'COMPLETED' and op.status = 'CREDITED') or (o.status = 'COMPLETED' and op.status = 'CAPTURED') or (o.status = 'ANNULLED' and op.status = 'CAPTURED')) and
                o.date_delivery > :start and o.date_delivery < :end and
                c.facility_id = :facilityId and (fc.id = :contractId or fc.id is null)
                group by activity_name"""

        groovySql.rows(query, [
                price: price,
                start: summary.interval.start.toString("yyyy-MM-dd HH:mm"),
                end: summary.interval.end.toString("yyyy-MM-dd HH:mm"),
                facilityId: summary.facility.id,
                contractId: summary.contract?.id])
    }

    List getSubmissionGroups(FacilityPaymentSummary summary, BigDecimal price) {
        def query = """select coalesce(a.name, o.description) as submission_name, count(*) as num
                from `order` o
                join order_order_payments oop on oop.order_id = o.id
                join order_payment op on op.id = oop.payment_id
                left join customer c on o.customer_id = c.id
                left join submission s on o.id = s.order_id
                left join form frm on s.form_id = frm.id
                left join activity a on frm.id = a.form_id
                left join facility f on c.facility_id = f.id
                left join facility_contract fc on fc.facility_id = f.id
                where
                o.article = 'FORM_SUBMISSION' and o.price = :price and
                (o.status in ('CONFIRMED', 'COMPLETED') or (o.status = 'ANNULLED' and op.status = 'CAPTURED')) and
                o.date_delivery > :start and o.date_delivery < :end and
                (op.type = 'netaxept' or op.type = 'adyen') and op.status = 'CAPTURED' and
                c.facility_id = :facilityId and (fc.id = :contractId or fc.id is null)
                group by submission_name"""

        groovySql.rows(query, [
                price: price,
                start: summary.interval.start.toString("yyyy-MM-dd HH:mm"),
                end: summary.interval.end.toString("yyyy-MM-dd HH:mm"),
                facilityId: summary.facility.id,
                contractId: summary.contract?.id])
    }
}
