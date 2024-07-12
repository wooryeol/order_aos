package kr.co.kimberly.wma.network.model

import java.io.Serializable

data class SearchItemModel (
    var itemCd: String? = null, // 품목 코드
    val itemNm: String? = null, // 품목 명
    val whStock: String? = null, // 재고 수량
    val getBox: Int? = null, // 박스 입수량
    val vatYn: String? = null, // 부가제 적용 여부
    val netPrice: Int? = null, // 공급가
    val slipNo: String? = null, // 전표 번호
    val customer: String? = null, // 거래처
    val totalAmount: Int? = null, // 금액
    val slipSeq: Int? = null, // 전표내 품목 순번
    val boxQty: Int? = null, // 박스 수량
    val unitQty: Int? = null, // 낱개 수량
    val saleQty: Int? = null, // 판매 수량
    val amount: Int? = null, // 합계
    var enableOrderYn: String? = null, // 본사 발주 가능여부
    var orderPrice: Int? = null, // 발주 단가
    var supplyPrice: Int? = null, // 공급가
    var vat: Int? = null, // 부가세
):Serializable