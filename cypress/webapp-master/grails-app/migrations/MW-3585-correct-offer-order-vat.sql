update `order` o
join facility f on f.id = o.facility_id
set o.vat = o.price-round(o.price/(1+(f.vat/100)), 2)
where o.vat > 0
and o.article in ('COUPON')