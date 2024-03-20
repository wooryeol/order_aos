package kr.co.kimberly.wma.model

import java.io.Serializable

data class ReceiptModel(
    var receiptNumber: String,
    var account: String,
    var totalAmount: String
): Serializable