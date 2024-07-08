package kr.co.kimberly.wma.network.model

import java.io.Serializable

data class ListResultModel<T> (
    val returnCd: String, // 결과 코드
    val returnMsg: String, // 결과 메세지
    val data: List<T>? = null
): Serializable

data class ObjectResultModel<T> (
    val returnCd: String, // 결과 코드
    val returnMsg: String, // 결과 메세지
    val data: T? = null
): Serializable

data class DataModel<T>(
    val maxPage: Int? = null, // 총 페이지 수
    val searchPage: Int? = null, // 결과 페이지 번호
    val slipNo: String? = null, // 전표 번호
    val customerCd: String? = null, // 거래처 코드
    val customerNm: String? = null, // 거래처 명
    val totalAmount: Int? = null, // 합계 금액
    val enableButtonYn: String? = null, // 버튼 활성화 여부
    val itemList: List<T>? = null, // 결과 리스트
    val resultType: String? = null, // 결과 유형
    val customerList: List<T>? = null, // 거래처 목록
) : Serializable