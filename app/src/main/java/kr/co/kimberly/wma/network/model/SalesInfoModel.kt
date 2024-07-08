package kr.co.kimberly.wma.network.model

import java.io.Serializable

data class SalesInfoModel (
    val itemNm: String? = null, // 품목명
    val itemCd: String? = null, // 품목 코드
    val netPrice: Int? = null, // 단가
    val getBox: String? = null, // 1박스당 낱개 수
    val boxQty: Int? = null, // 박스 수량
    val unitQty: Int? = null, // 낱개 수량
    val saleQty: Int? = null, // 판매 수량
    val supplyPrice: Int? = null, // 공급가
    val vat: Int? = null, // 부가세
    val amount: Int? = null // 합계
) :
    Serializable