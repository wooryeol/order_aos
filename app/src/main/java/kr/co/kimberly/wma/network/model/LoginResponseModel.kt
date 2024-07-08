package kr.co.kimberly.wma.network.model

import java.io.Serializable


data class LoginResponseModel (
    val agencyCd: String? = null, // 대리점 코드
    val agencyNm: String? = null, // 대리점 명
    val empCd: String? = null, // 사원 코드
    val empNm: String? = null, // 사원 명
    val userId: String? = null, // 사용자 아이디
    val bizNo: String? = null, // 대리점 사업자등록번호
    val telNo: String? = null, // 대리점 전화번호
    val representNm: String? = null, // 대리점 대표자 명
    val address: String? = null, // 대리점 주소
    val bizType: String? = null, // 대리점 업태
    val bizSector: String? = null, // 대리점 업종
    val empMobile: String? = null, // 사원 핸드폰 번호
    val authorityBuy: String? = null, // 구매메뉴 권한여부
    val authorityModifyPrice: String? = null, // 가격수정 권한여부
    val appVersion: String? = null, // APP 버전 정보
    val downloadUrl: String? = null, // APP 다운로드 URL
    val notice: String? = null, // 공지사항
) : Serializable