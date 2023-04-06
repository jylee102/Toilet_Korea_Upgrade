package com.example.toilet_korea

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.R
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.thread

class RealtimeDB : AppCompatActivity() {

    val database: DatabaseReference = Firebase.database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        thread {
            val API_KEY =
                "g5i7qJn8Mi5NKv%2FXkSxItQQmoXGzQfgjtj0UdKXYURG4OfE%2BOS%2BxD17cMRFYH22ISNcxiTJw68PboMhNllrnWA%3D%3D"

            //데이터의 시작과 종료 인덱스
            var pageNo = 1
            //과천만
            val numOfRows = 100
            //데이터의 전체 개수를 저장하기 위한 프로퍼티
            var count = 350

            database.child("toilet").removeValue()
            do {
                //파싱할 URL 생성
                val url =
                    URL(
                        "http://api.data.go.kr/openapi/tn_pubr_public_toilet_api?serviceKey=" + API_KEY + "&pageNo=" + pageNo + "&numOfRows=" + numOfRows + "&type=json"
                    )
                //연결해서 문자열 가져오기
                val connection = url.openConnection()
                val data = connection.getInputStream()
                val isr = InputStreamReader(data)
                // br: 라인 단위로 데이터를 읽어오기 위해서 만듦
                val br = BufferedReader(isr)

                // Json 문서는 일단 문자열로 데이터를 모두 읽어온 후, Json에 관련된 객체를 만들어서 데이터를 가져옴
                var str: String?
                val buf = StringBuffer()

                do {
                    str = br.readLine()

                    if (str != null) {
                        buf.append(str)
                    }
                } while (str != null)


                //JSON 파싱
                // 전체가 객체로 묶여있기 때문에 객체형태로 가져옴
                val root = JSONObject(buf.toString())
                val response = root.getJSONObject("response")
                val body = response.getJSONObject("body")
                val items = body.getJSONArray("items") // 객체 안에 있는 item이라는 이름의 리스트를 가져옴


                for (i in 0 until items.length()) {

                    val obj = items.getJSONObject(i)

                    val tN = obj.getString("toiletNm")
                    val toiletNm = replaceSpecialChar(tN)

                    val mcbn = obj.getString("menChildrenToiletBowlNumber")
                    val menChildrenToiletBowlNumber = handleInputError(mcbn)
                    val mcun = obj.getString("menChildrenUrinalNumber")
                    val menChildrenUrinalNumber = handleInputError(mcun)

                    val toilet = Toilet(obj.getString("toiletNm"),
                        obj.getString("rdnmadr"),
                        obj.getString("lnmadr"),
                        obj.getString("unisexToiletYn"),
                        obj.getInt("menToiletBowlNumber"),
                        obj.getInt("menUrineNumber"),
                        obj.getInt("menHandicapToiletBowlNumber"),
                        obj.getInt("menHandicapUrinalNumber"),
                        menChildrenToiletBowlNumber,
                        menChildrenUrinalNumber,
                        obj.getInt("ladiesToiletBowlNumber"),
                        obj.getInt("ladiesHandicapToiletBowlNumber"),
                        obj.getInt("ladiesChildrenToiletBowlNumber"),
                        obj.getString("phoneNumber"),
                        obj.getString("openTime"),
                        obj.getString("latitude").toDoubleOrNull(),
                        obj.getString("longitude").toDoubleOrNull(),
                        obj.getString("emgBellYn"),
                        obj.getString("enterentCctvYn"),
                        obj.getString("dipersExchgPosi"))

                    database.child("toilet").child(toiletNm).setValue(toilet)

                }
                //인덱스를 변경해서 데이터 계속 가져오기
                pageNo = pageNo + 1
            } while (pageNo <=count) //일단 과천만 표시하려고


            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    fun replaceSpecialChar(toiletNm: String): String{
        val p: Pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE)
        val m: Matcher = p.matcher(toiletNm)
        val b: Boolean = m.find()

        var result: String? = null

        return if(b){
            result = toiletNm.replace(".", ",")
            result = result.replace("#", ",")
            result = result.replace("$", ",")
            result = result.replace("[", ",")
            result = result.replace("]", ",")

            result

        }else toiletNm
    }

    fun handleInputError(e: String): Int?{
        return if (e == ".")
            null
        else e.toInt()
    }

}