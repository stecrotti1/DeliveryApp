# DeliveryApp

The application was made only for **academic purposes** and it's not meant to use in real life circumstances.

The application uses the [Coroutine Image Loader (Coil)](https://coil-kt.github.io/coil/) to download/upload images from [Firebase](https://firebase.google.com/) server.

Buttons and icons were developed using [Material Design](https://material.io/design).

The Android application is still under development, but feel free to add pull requests if you find any bugs or have suggestions.

## What does it do
The application domain of this project is a delivery system made available by a small market/grocery store (with a static position). The application is supposed to have 3 types of users:
* client/customer;
* rider;
* manager;

### Client
The customer, if not already registered, can create a profile with their data. Once the
profile has been created, the customer can log in. Once logged in, the customer can report their address (which is mustn't be more than 10 km away from the market) automatically through GPS or can correct it manually.
Then, the customer, will receive a list of products with photo, title
and price. By clicking on the product you can view the detail and add it to the shopping cart,along with the quantity. Once the order is completed, it will be possible to proceed to checkout. The payment will be made exclusively by cash or by credit card with the rider. 
Once the rider has left the mini market, the user will receive a notification and from that moment it will be possible to chat with the rider to communicate delivery details or to report delays.

### Rider
The rider, if not already registered, can create a profile with his own data. Once the
profile has been created, the rider can log in. Once logged in, rider can set its status (available for deliveries, not available). If available, it can be selected by the manager.
The rider will receive a notification for delivery and will be able to accept the order or refuse it. In a notification there will be the place of delivery and the distance from the market to the place of delivery. The rider
will be able to see the place of delivery on the map (the place of delivery will be identified by
a marker).
Once the products have been accepted and taken from the market, it will be possible to chat with
the customer for the duration of the journey, as well as chatting with the owner
in a separate chat.
Once the customer has been reached, the rider will report the payment the end of the
delivery. In the event of payment problems or an unreachable customer, delivery failure will be reported and the undelivered products will become available again.

### Manager
The manager, if not already registered, can create a profile with his own data. Once the
profile has been created, the manager can log in. Once logged in, the manager will be able to load the available articles.
Manager can upload a photo (from gallery or camera), a title, a description, the price
and the quantity available. He can also change products that are already available by updating
photos, quantity description etc.
If a product reaches quantity 0 it will not be viewed by customers.
When an order arrives, the manager receives a notification and can select a rider. If the river accepts, the task is entrusted,
otherwise the list of drivers reappears and the manager can select another one. Once entrusted
the assignment, manager can contact the rider via chat.

## Requirements
* at least Android API 28;
* Android Studio version 4.1.+;
* Kotlin version: 1.4.21;
* you have to create your own [Google API key](https://developers.google.com/maps/documentation/android-sdk/start#get-key);
* you may want to connect the Android app to your own firebase project;
* you may want to set the market position in your own firestore cloud (path: **marketPos/randomID/position**) as a Geopoint

## Screenshots
![SelectUserType](https://firebasestorage.googleapis.com/v0/b/deliveryapp-7c8fe.appspot.com/o/screenshots%2Fapp_screen_1.png?alt=media&token=e4b6cfca-a5c1-4b00-b8da-7b20ff6e2596)

![ProductDialog](https://firebasestorage.googleapis.com/v0/b/deliveryapp-7c8fe.appspot.com/o/screenshots%2FScreenshot%20from%202021-03-26%2012-09-28.png?alt=media&token=6235c6c6-e897-49c2-b3ec-e48fcf9d2072)

![HomePage](https://firebasestorage.googleapis.com/v0/b/deliveryapp-7c8fe.appspot.com/o/screenshots%2FScreenshot%20from%202021-03-26%2012-09-16.png?alt=media&token=5ac3d2cd-56a7-48be-95d6-19e6bea6d750)
