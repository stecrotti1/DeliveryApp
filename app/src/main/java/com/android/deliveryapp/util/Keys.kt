package com.android.deliveryapp.util

class Keys {
    companion object {
        // extras for intents
        const val CLIENT: String = "USER"
        const val RIDER: String = "RIDER"
        const val MANAGER: String = "MANAGER"

        // preferences
        const val userInfo: String = "userInfo"
        const val userType: String = "userType"
        const val isLogged: String = "isLogged"
        const val isRegistered: String = "isRegistered"
        const val hasLocation: String = "hasLocation"
        const val managerID: String = "managerID"
        const val clientLocation: String = "clientLocation"
        const val marketLocation: String = "marketLocation"

        // firebasefirestore
        const val marketPosFirebase: String = "MarketPos"
        const val Lat: String = "Lat"
        const val Lng: String = "Lng"
        const val productListFirebase: String = "productList"
        const val image: String = "image"
        const val name: String = "name"
        const val price: String = "price"
    }
}