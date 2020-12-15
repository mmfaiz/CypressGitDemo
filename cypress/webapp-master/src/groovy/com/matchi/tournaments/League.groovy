package com.matchi.tournaments

class League {
    Long id
    String name
    String description
    List<Division> divisions
    String linkToRules

    // Indicates private access and name of group that has access.
    Boolean isPrivate
    String groupName
}
