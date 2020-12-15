<select name="${selectName ?: 'discountType'}" class="span1">
    <option value="AMOUNT" ${rowObj?.discountType == com.matchi.invoice.InvoiceRow.DiscountType.AMOUNT ? 'selected' : ''}><g:currentFacilityCurrency/></option>
    <option value="PERCENT" ${rowObj?.discountType == com.matchi.invoice.InvoiceRow.DiscountType.PERCENT ? 'selected' : ''}>%</option>
</select>