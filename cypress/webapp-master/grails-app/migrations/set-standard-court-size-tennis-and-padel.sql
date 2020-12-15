Insert into court_type_attribute select id as court_id, "TENNIS_SIZE" as court_type_enum, "Standard" as value from court where sport_id = 1;

Insert into court_type_attribute select id as court_id, "PADEL_SIZE" as court_type_enum, "Double" as value from court where sport_id = 5;
