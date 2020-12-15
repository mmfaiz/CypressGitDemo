
delete from region where country='HR';
insert into region (version, name, lat, lng, zoomlv, country) values (0, "Croatia", 45.1000, 15.2000, 4, "HR");

insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Bjelovar-Bilogora', 45.5410, 16.5051, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Brod-Posavina', 45.0927, 18.0113, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Dubrovnik-Neretva', 42.3913 ,18.0541, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Istria', 45.1421, 13.5619, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Karlovac', 45.2935, 15.3321, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Koprivnica-Križevci', 46.1012, 16.5433, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Krapina-Zagorje', 46.730, 15.4825, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Lika-Senj', 44.4225 , 15.1027, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Međimurje', 46.2758, 16.2450, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Osijek-Baranja', 45.3813, 18.375, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Požega-Slavonia',	45.1840, 17.4424, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Primorje-Gorski', 45.2714, 14.3538, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Šibenik-Knin', 43.5544, 16.343, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Sisak-Moslavina', 45.1315, 16.155, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Split-Dalmatia', 43.100, 16.300, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Varaždin', 46.1916, 16.1352, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Virovitica-Podravina', 45.5223, 17.3018, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Vukovar-Srijem', 45.1343, 18.550, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Zadar', 44.15, 15.5342, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Zagreb County', 45.4456, 15.3416, (select id from region where name = 'Croatia'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'City of Zagreb', 45.490, 15.590, (select id from region where name = 'Croatia'), 10);