package com.android.deliveryapp.util

import android.widget.ImageView
import android.widget.ProgressBar

class ProgressBarImage {
    var img: ImageView? = null
    var pb: ProgressBar? = null
}

data class Model (var url: String, var name: String, var price: String)