# Master facility

## Intention of logic.
Because it seems unclear how things work in background when handling stuff regarding master facilities I try to do some explanation here.

If an object is created by an administrator at a master level that is where the purchase for that object is stored and also where the customer is created. It's then fetched from that master customer when utilizing it.

That means when purchasing a global membership from a local facility's page it should be created at a customer on master level (it didn't do before because of a bug). The same goes for offers. 

## Price logic
Price lists exists at a local level because that's where courts and slots exists (just as before). The customer categories for slots also exists at local facility level because that's where the slots exists. Other customer categories have to be created at master facility level (which is currently not possible due to ui change) to be able to set different prices for offers.

The customer categories have a MemberTypeCondition and those can be related to membershipTypes from a master facility.

### Example
So when a user asks for a price for e.g. a booking the system will look for customer category conditions to meet. It will find a membershipType condition which requires membershipType = "global membership" (purchased at, and created for a customer, at the global facility). The system will find that the user has a customer at the local facility (which is not a global member) BUT it will also find a customer at the global facility and because that customer has the correct membership it can be matched.

When trying to use a global punch card (and gift card) the same procedure will follow. It will find a customer at master facility and find a purchased punch card there and return to the user that it's usable. When paying there will be a punch registered at the punch card and the booking is made. Since the punch card is related to the global customer that's where the punch is registered and also where statistics will apply. The punch do store on which object (booking, activity etc) it was used for which make it possible to find a relation to the local facility for invoicing.

# Running locally in Kubernetes

Change requests, limits and replicas to "reasonable" values:

    ...
    replicas: 1
    ...
    resources:
      requests:
        memory: 4Gi
        cpu: 1500m
      limits:
        memory: 4Gi
        cpu: 1500m


# How to:

1. [Configure emails to sent them locally](docs/emails_locally.md)

1. [Add new country/currency/locale](docs/add_new_country.md)
