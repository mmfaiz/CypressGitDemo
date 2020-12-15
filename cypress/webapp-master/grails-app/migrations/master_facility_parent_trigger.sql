create trigger check_master_facility_parent before insert on facility_hierarchy_group_facilities
    for each row
    begin
        if (select count(*) from facility_hierarchy_group where master_facility_id = new.facility_id > 0)
        then
        SIGNAL SQLSTATE VALUE '99999' SET MESSAGE_TEXT = 'A master facility cannot be a child facility';
        end if;
    end;
