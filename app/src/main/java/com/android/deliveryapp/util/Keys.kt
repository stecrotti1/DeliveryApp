package com.android.deliveryapp.util

class Keys {
    companion object {
        // user types
        const val CLIENT: String = "USER"
        const val RIDER: String = "RIDER"
        const val MANAGER: String = "MANAGER"

        // preferences
        const val userInfo: String = "userInfo"
        const val userType: String = "userType"
        const val isLogged: String = "isLogged"
        const val isRegistered: String = "isRegistered"
        const val hasLocation: String = "hasLocation"

        // firebasefirestore
        const val users: String = "users"

        const val clientAddress: String = "address"

        const val marketPosFirestore: String = "marketPos"
        const val marketDocument: String = "MARKET"
        const val fieldPosition: String = "position"

        const val productListFirebase: String = "productList"

        const val image: String = "image"
        const val name: String = "name"
        const val price: String = "price"
    }
}