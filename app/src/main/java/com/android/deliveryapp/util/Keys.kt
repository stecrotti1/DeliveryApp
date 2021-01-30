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
        const val isRemembered: String = "isRemembered"
        const val isRegistered: String = "isRegistered"
        const val managerID: String = "managerID"
        const val clientLocation: String = "clientLocation"

        // firebasefirestore
        const val productList: String = "productList"
        const val image: String = "image"
        const val name: String = "name"
        const val price: String = "price"
    }
}