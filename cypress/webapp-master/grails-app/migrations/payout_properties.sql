insert into facility_property (version, date_created, facility_id, key_name, last_updated, value)
select 0, now(), id, 'PAYOUT_BANKGIRO', now(), bankgiro from facility;

insert into facility_property (version, date_created, facility_id, key_name, last_updated, value)
select 0, now(), id, 'PAYOUT_PLUSGIRO', now(), plusgiro from facility;