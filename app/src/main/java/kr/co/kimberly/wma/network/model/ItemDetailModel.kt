package kr.co.kimberly.wma.network.model

import java.io.Serializable

data class ItemDetailModel (
    val resultType: String? = null, // 결과 유형
    val makerNm: String? = null, // 제조사
    val itemCd: String? = null, // 품목코드
    val itemNm: String? = null, // 품목명
    val kanCode: String? = null, // 상품바코드
    val dimension: String? = null, // 치수
    val vatType: String? = null, // 부가세구분
    val registerImgYn: String? = null, // 이미지등록여부
    val imgUrl: String? = null, // 이미지URL
): Serializable