package com.matchi.tournaments

import com.matchi.Customer
import com.matchi.Group

class ExcelLeague {
    Long id
    List<League> leagues

    void removePrivateLeagues(Customer customer) {
        leagues.removeAll {
            // NOTE! Boolean isPrivate is not working correct in closure therefor string comparison.
            it.isPrivate.toString() == "true" &&
            (
                customer == null ||
                !customer.groups.any { Group group -> group.name.equalsIgnoreCase(it.groupName) }
            )
        }
    }
}
