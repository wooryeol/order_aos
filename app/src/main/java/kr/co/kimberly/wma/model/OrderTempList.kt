package kr.co.kimberly.wma.model

import java.io.Serializable

class OrderTempList(
    var idx: Int,
    var itemName: String,
    var itemCode: String,
    var qtyBox: Int,
    var qtyEA: Int,
    var totalPrice: Int,
    var date: String,
    var updatedDate: String,
    var deletedDate: String
): Serializable {
    constructor(): this(0,"", "", 0, 0, 0, "", "", "")
}