package kr.co.kimberly.wma.model

import java.io.Serializable

data class OrderRegModel(val orderName: String, val box: String, val each: String, val unitPrice: String, val totalQty: String, val totalAmount: String) :
    Serializable