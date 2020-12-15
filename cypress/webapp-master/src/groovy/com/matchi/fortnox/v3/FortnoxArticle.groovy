package com.matchi.fortnox.v3

/**
 *  Fields description http://developer.fortnox.se/documentation/common/articles/#Fields
 *
 * @author Michael Astreiko
 */
class FortnoxArticle implements Serializable {
    private static final long serialVersionUID = 1L;

    /** identifier */
    String ArticleNumber

    Boolean Bulky
    Integer ConstructionAccount
    Integer Depth
    String Description
    String DisposableQuantity
    String EAN
    Integer EUAccount
    Integer EUVATAccount
    Integer ExportAccount
    Integer Height
    Boolean Housework
    String Manufacturer
    String ManufacturerArticleNumber
    String Note
    Integer PurchaseAccount
    String PurchasePrice
    String SalesPrice
    String QuantityInStock
    String ReservedQuantity
    Integer SalesAccount
    Boolean StockGoods
    String StockPlace
    String StockValue
    Float StockWarning
    String SupplierName
    String SupplierNumber
    //STOCK /SERVICE
    String Type
    String Unit
    Float VAT
    Boolean WebshopArticle
    Integer Weight
    Integer Width
    Boolean Expired

    String getId() {
        ArticleNumber
    }

    String getDescr() {
        Description
    }

    String getFirstPrice() {
        SalesPrice
    }

    @Override
    public String toString() {
        return "FortnoxArticle{" +
                "ArticleNumber='" + ArticleNumber + '\'' +
                ", Bulky=" + Bulky +
                ", ConstructionAccount=" + ConstructionAccount +
                ", Depth=" + Depth +
                ", Description='" + Description + '\'' +
                ", DisposableQuantity='" + DisposableQuantity + '\'' +
                ", EAN='" + EAN + '\'' +
                ", EUAccount=" + EUAccount +
                ", EUVATAccount=" + EUVATAccount +
                ", ExportAccount=" + ExportAccount +
                ", Height=" + Height +
                ", Housework=" + Housework +
                ", Manufacturer='" + Manufacturer + '\'' +
                ", ManufacturerArticleNumber='" + ManufacturerArticleNumber + '\'' +
                ", Note='" + Note + '\'' +
                ", PurchaseAccount=" + PurchaseAccount +
                ", PurchasePrice='" + PurchasePrice + '\'' +
                ", SalesPrice='" + SalesPrice + '\'' +
                ", QuantityInStock='" + QuantityInStock + '\'' +
                ", ReservedQuantity='" + ReservedQuantity + '\'' +
                ", SalesAccount=" + SalesAccount +
                ", StockGoods=" + StockGoods +
                ", StockPlace='" + StockPlace + '\'' +
                ", StockValue='" + StockValue + '\'' +
                ", StockWarning=" + StockWarning +
                ", SupplierName='" + SupplierName + '\'' +
                ", SupplierNumber='" + SupplierNumber + '\'' +
                ", Type='" + Type + '\'' +
                ", Unit='" + Unit + '\'' +
                ", VAT=" + VAT +
                ", WebshopArticle=" + WebshopArticle +
                ", Weight=" + Weight +
                ", Width=" + Width +
                ", Expired=" + Expired +
                '}';
    }
}
