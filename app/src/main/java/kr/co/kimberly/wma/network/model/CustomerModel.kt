package kr.co.kimberly.wma.network.model

data class CustomerModel (
    val custCd: String, // 거래처 코드
    val custNm: String, // 거래처 명
    val remainAmt: Int? = null, // 채권잔액
    val slipNo: String? = null, // 전표 번호
    val totalAmount: Int? = null // 금액
)