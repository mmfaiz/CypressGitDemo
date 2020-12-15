package com.matchi.invoice

class InvoiceArticle implements Serializable {
    Long id
    Long facilityId
    Long organizationId
    Long articleNumber
    String name
    String description
    Float firstPrice
    Float salesPrice
    Integer vat

    static constraints = {
        facilityId nullable: false
        organizationId nullable: true
        articleNumber nullable: false
        name nullable: false, blank: false
        description nullable: true
        firstPrice nullable: true
        salesPrice nullable: true
        vat nullable: true
    }


    @Override
    public String toString() {
        return "InvoiceArticle{" +
                "version=" + version +
                ", id=" + id +
                ", facilityId=" + facilityId +
                ", organizationId=" + organizationId +
                ", articleNumber=" + articleNumber +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", firstPrice=" + firstPrice +
                ", salesPrice=" + salesPrice +
                ", vat=" + vat +
                '}';
    }
}
