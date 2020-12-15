##Background

As a black-admin user I need to be able to select %country_name% as country for a facility and add %currency_name% as the venue currency.

##Implementation details

###1. Add country to Config

```
settings {
        facility {
            countries
```

###2. Add currency and defile service fee

```
settings {
        ...
        currency = [
                SEK: [
```

###3. Add locale to LocaleHelper

```
class LocaleHelper {
    enum Country {
```

###4. Add VAT

```
class Facility {
	...
	static final List<Integer> POSSIBLE_VATS = [
```

###5. Add regions

>Q. Should we prepopulate the database with regions and cities (like we did for Denmark)?

>A. Not needed and will be done manually from black admin. 

##Test instructions

1. Add or edit current facility to support %currency_name% as currency and %country_name% as country as image below in black admin facility settings and select %country_vat% as VAT. 

1. Make a booking and pay for a as end user on the facility with %currency_name%

1. Verify the black admin order

1. Verify the receipt

1. Verify Adyen transaction

1. Verify cancellation