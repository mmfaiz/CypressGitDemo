package com.matchi

import com.matchi.enums.BookingGroupType
import groovyx.gpars.GParsPool
import java.util.concurrent.TimeUnit

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class CourtRelationsBookingServiceIntegrationTests extends GroovyTestCase {

    static transactional = false

    def courtRelationsBookingService

    void testBlockSlot() {
        def facility = createFacility()
        facility.relatedBookingsCustomer = createCustomer(facility)
        facility.save(failOnError: true, flush: true)
        def originalSlot = createSlot(createCourt(facility))
        def slotToBlock = createSlot(createCourt(facility))

        courtRelationsBookingService.blockSlot(originalSlot, slotToBlock)

        def booking = Booking.findBySlot(slotToBlock)
        assert booking
        assert booking.customer == facility.relatedBookingsCustomer
        assert booking.comments == "Blockerad"
        assert booking.showComment == true
        assert booking.group
        assert booking.group.type == BookingGroupType.BLOCKED
    }

    void testBlockSlotByConcurrentTransactions() {
        def facility = createFacility()
        facility.relatedBookingsCustomer = createCustomer(facility)
        facility.save(failOnError: true, flush: true)
        def slots = []
        5.times {
            slots << createSlot(createCourt(facility))
        }
        def slotToBlock = createSlot(createCourt(facility))

        def pool = GParsPool.createPool(5)
        GParsPool.withExistingPool(pool) {
            slots.eachParallel { originalSlot ->
                Slot.withNewSession {
                    Slot.withTransaction {
                        def slot = Slot.get(slotToBlock.id)
                        sleep(100)
                        courtRelationsBookingService.blockSlot(originalSlot, slot)
                    }
                }
            }
        }
        pool.awaitTermination(10, TimeUnit.SECONDS)

        assert Booking.countBySlot(slotToBlock) == 1
        def booking = Booking.findBySlot(slotToBlock)
        assert booking.customer == facility.relatedBookingsCustomer
        assert booking.comments == "Blockerad"
        assert booking.showComment == true
        assert booking.group
        assert booking.group.type == BookingGroupType.BLOCKED
    }
}