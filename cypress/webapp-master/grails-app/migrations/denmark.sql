insert into region (version, name, lat, lng, zoomlv, country) values (0, "Hovedstaden",55.83333,12.33333, 7, "DK");
insert into region (version, name, lat, lng, zoomlv, country) values (0, "Midtjylland",56.16667,9.5, 7, "DK");
insert into region (version, name, lat, lng, zoomlv, country) values (0, "Nordjylland",57,9.66667, 7, "DK");
insert into region (version, name, lat, lng, zoomlv, country) values (0, "Sjælland",55.41667,11.83333, 7, "DK");
insert into region (version, name, lat, lng, zoomlv, country) values (0, "Syddanmark",55.37911,10.345, 7, "DK");

insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Vallensbæk',55.63128,12.37369, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Tårnby',55.60391,12.59599, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Rødovre',55.68852,12.44834, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Lyngby-Taarbæk',55.78456,12.50508, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'København',55.6711,12.56529, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Ishøj',55.62299,12.30567, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Hvidovre',55.63166,12.46615, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Hørsholm',55.88759,12.48566, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Hillerød',55.92319,12.23794, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Herlev',55.73317,12.43106, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Helsingør',56.05,12.5, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Glostrup',55.68188,12.41018, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Gladsaxe',55.76667,12.43333, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Gentofte',55.75,12.55, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Frederikssund',55.80957,12.04038, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Frederiksberg',55.67856,12.52216, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Dragør',55.58233,12.62756, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Brøndby',55.6429,12.41101, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Bornholm',55.12386,14.91115, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Ballerup',55.73248,12.35793, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Allerød',55.85856,12.32558, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Albertslund',55.68022,12.34797, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Furesø',55.78333,12.34167, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Høje-Taastrup',55.65643,12.24854, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Fredensborg',55.95,12.45, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Halsnæs',55.96765,11.94214, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Rudersdal',55.825,12.49167, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Gribskov',56.05833,12.24167, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Egedal',55.75556,12.22778, (select id from region where name = 'Hovedstaden'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Viborg',56.45,9.36667, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Struer',56.46667,8.58333, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Skive',56.64478,8.9766, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Skanderborg',56.07956,9.89868, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Silkeborg',56.17495,9.54666, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Samsø',55.85245,10.60045, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Randers',56.51561,10.06901, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Odder',55.92967,10.15304, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Lemvig',56.49136,8.29927, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Horsens',55.9274,9.77377, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Holstebro',56.35916,8.59631, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Herning',56.14997,8.89712, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Hedensted',55.7968,9.744, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Aarhus',56.16317,10.16897, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Ringkøbing-Skjern',56.01,8.39667, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Norddjurs',56.44124,10.7666, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Ikast-Brande',55.98333,9.21667, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Favrskov',56.31667,9.94, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Syddjurs',56.3125,10.52083, (select id from region where name = 'Midtjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Thisted',57.00397,8.61834, (select id from region where name = 'Nordjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Morsø',56.79622,8.73272, (select id from region where name = 'Nordjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Læsø',57.26774,11.02265, (select id from region where name = 'Nordjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Hjørring',57.45682,10.05859, (select id from region where name = 'Nordjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Frederikshavn',57.43347,10.42507, (select id from region where name = 'Nordjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Brønderslev',57.23506,10.10061, (select id from region where name = 'Nordjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Jammerbugt',57.14583,9.5625, (select id from region where name = 'Nordjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Mariagerfjord',56.69722,9.84722, (select id from region where name = 'Nordjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Aalborg',57,9.95, (select id from region where name = 'Nordjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Rebild',56.80556,9.77778, (select id from region where name = 'Nordjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Vesthimmerland',56.8,9.37083, (select id from region where name = 'Nordjylland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Vordingborg',55.01383,12.10008, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Stevns',55.33373,12.30692, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Sorø',55.48268,11.55693, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Solrød',55.53553,12.17337, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Slagelse',55.34546,11.3339, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Roskilde',55.65,12.1, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Ringsted',55.44721,11.8172, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Næstved',55.25855,11.74615, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Lejre',55.63375,11.92234, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Køge',55.45668,12.07332, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Kalundborg',55.63545,11.19993, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Holbæk',55.65919,11.62049, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Greve',55.5877,12.2506, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Odsherred',55.88333,11.59444, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Faxe',55.29444,12.06111, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Guldborgsund',54.76944,11.83611, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Lolland',54.80238,11.29524, (select id from region where name = 'Sjælland'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Vejle',55.74874,9.40421, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Vejen',55.46312,9.05004, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Tønder',55.07304,8.87214, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Svendborg',55.0841,10.61391, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Sønderborg',54.91667,9.8, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Nyborg',55.29473,10.7031, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Middelfart',55.45782,9.881, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Kolding',55.45006,9.45807, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Kerteminde',55.45498,10.6222, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Haderslev',55.24441,9.32261, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Fredericia',55.57271,9.69489, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Fanø',55.41667,8.41667, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Varde',55.61667,8.5, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Billund',55.73079,8.96844, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Assens',55.29958,10.07952, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Aabenraa',54.9898,9.31282, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Esbjerg',55.46893,8.46222, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Nordfyns',55.52222,10.22222, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Ærø',54.85833,10.43333, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Faaborg-Midtfyn',55.22667,10.40667, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Langeland',54.9,10.77222, (select id from region where name = 'Syddanmark'), 10);
insert into municipality (version, name, lat, lng, region_id, zoomlv) values (0, 'Odense',55.3957,10.37761, (select id from region where name = 'Syddanmark'), 10);