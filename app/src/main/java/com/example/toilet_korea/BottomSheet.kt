package com.example.toilet_korea

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.content.Intent.ACTION_SENDTO
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


var toiletNm: String? = null
var rdnmadr: String? = null
var lnmadr: String? = null
var unisexToiletYn: String? = null
var menToiletBowlNumber: String? = null
var menUrineNumber: String? = null
var ladiesToiletBowlNumber: String? = null
var menHandicap: String? = null
var menChildren: String? = null
var ladiesHandicap: String? = null
var ladiesChildren: String? = null
var phoneNumber: String? = null
var openTime: String? = null
var position: String? = null

class BottomSheet : BottomSheetDialogFragment() {

    val bottomSheetBehavior = view?.let {
        BottomSheetBehavior.from(
            it.findViewById<LinearLayout>(R.id.bottomSheet)
        )
    }

    private var inputPhoneNum: String? = null

    private val adapter: ContactAdapter by lazy {
        ContactAdapter({ contact ->
            inputPhoneNum = contact.number
            if (inputPhoneNum != null) {
                val myUri = Uri.parse("smsto:${inputPhoneNum}") //데이터베이스 연결 전 임의로
                val myIntent = Intent(ACTION_SENDTO, myUri)
                // 문자 전송 화면 이동 시 미리 문구를 적어서 보내기
                // myIntent를 가지고 갈 때 -> putExtra로 데이터를 담아서 보내자
                myIntent.putExtra("sms_body", "[Toilette] 공중화장실에서 위급 상황이 발생했습니다. 주소:${rdnmadr}")
                startActivity(myIntent)
            }
        }, { contact ->
            inputPhoneNum = contact.number
            if(inputPhoneNum != null){
                val myUri = Uri.parse("tel:${inputPhoneNum}")
                val myIntent = Intent(ACTION_DIAL, myUri)
                startActivity(myIntent)
            }
        })
    }

    //리뷰 관련
    val database: DatabaseReference = Firebase.database.reference
    private val currentUser = Firebase.auth.currentUser
    var curUID = currentUser?.uid.toString()
    var nickname: String? = null

    private lateinit var reviews: ReviewData

    private lateinit var reviewAdapter: ReviewAdapter
    private var newsList: ArrayList<ReviewData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toiletNm = arguments?.getString("toiletNm")
        rdnmadr = arguments?.getString("rdnmadr")
        lnmadr = arguments?.getString("lnmadr")
        unisexToiletYn = arguments?.getString("unisexToiletYn")
        menToiletBowlNumber = arguments?.getString("menToiletBowlNumber")
        menUrineNumber = arguments?.getString("menUrineNumber")
        ladiesToiletBowlNumber = arguments?.getString("ladiesToiletBowlNumber")
        menHandicap = arguments?.getString("menHandicap")
        menChildren = arguments?.getString("menChildren")
        ladiesHandicap = arguments?.getString("ladiesHandicap")
        ladiesChildren = arguments?.getString("ladiesChildren")
        phoneNumber = arguments?.getString("phoneNumber")
        openTime = arguments?.getString("openTime")
        position = arguments?.getString("position")

        val contactViewModel: ContactViewModel =
            ViewModelProvider(this, ContactViewModel.Factory(requireActivity().application)).get(ContactViewModel::class.java)
        contactViewModel.getAll().observe(this) { contacts ->
            adapter.setContacts(contacts!!)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?):Dialog{
        val bottomSheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        //setting layout with bottom sheet
        bottomSheet.setContentView(R.layout.bottom_sheet)

        bottomSheet.behavior.peekHeight = 800

        bottomSheet.behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(view: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED,
                    -> {
                        showView(view.findViewById<AppBarLayout>(R.id.appBarLayout), getActionBarSize())
                    }
                    BottomSheetBehavior.STATE_COLLAPSED,
                    -> {
                        hideAppBar()
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> dismiss()
                    else -> {}
                }
            }

            override fun onSlide(view: View, slideOffset: Float) {

            }
        })

        return bottomSheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.bottom_sheet, container, false)
    }


    //button clicked
    @SuppressLint("CutPasteId")
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        database.child("User").child(curUID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                nickname = snapshot.child("userNm").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        val reviewRef = database.child("Review").child(toiletNm.toString())

        //리뷰 바텀시트 하단에 불러오기(아래 Unresolved Ref들은 레이아웃에 아이디 추가해야 함)
        reviewRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("CutPasteId")
            override fun onDataChange(snapshot: DataSnapshot) {

                newsList.clear()

                if (snapshot.exists()) {
                    // looping through to values
                    for (i in snapshot.children) {

                        reviews = i.getValue(ReviewData::class.java)!!
                        newsList.add(reviews)
                    }
                }

                reviewAdapter = ReviewAdapter(context, newsList)
                view?.findViewById<RecyclerView>(R.id.recyclerView)?.layoutManager = LinearLayoutManager(context)
                view?.findViewById<RecyclerView>(R.id.recyclerView)?.adapter = reviewAdapter
                view?.findViewById<RecyclerView>(R.id.recyclerView)?.setHasFixedSize(true)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        // x를 누르면 dialog 닫힘
        view?.findViewById<ImageButton>(R.id.cancelBtn)?.setOnClickListener {
            dismiss()
        }

        //비상연락 버튼클릭이벤트 - DangerCall
        view?.findViewById<FloatingActionButton>(R.id.SOSbtn)?.setOnClickListener {
            showDialog()
        }

        //리뷰 쓰기
        view?.findViewById<Button>(R.id.confirmButton)?.setOnClickListener {

            if (curUID != "null"){
                val contentText = view?.findViewById<EditText>(R.id.contentText)?.text
                val rate: Double = view?.findViewById<RatingBar>(R.id.ratingBar)?.rating!!.toDouble()

                if (contentText.toString() != "" && rate > 0){

                    reviewRef.child(curUID).child("rate").setValue(rate)
                    reviewRef.child(curUID).child("content").setValue(contentText.toString())
                    reviewRef.child(curUID).child("userNm").setValue(nickname)
                    reviewRef.child(curUID).child("image").setValue(R.drawable.ic_personal)

                    Toast.makeText(context, "리뷰가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    contentText?.clear()
                    view?.findViewById<RatingBar>(R.id.ratingBar)?.rating = 0.0F
                } else {
                    Toast.makeText(context, "별점이나 리뷰를 작성해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "게스트 이용자에게는 제한된 접근입니다.", Toast.LENGTH_LONG).show()
            }

        }
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        hideAppBar()
        bottomSheetBehavior?.state ?: BottomSheetBehavior.STATE_COLLAPSED

        val address = if(lnmadr != "null"){
            lnmadr
        }else rdnmadr

        view?.findViewById<TextView>(R.id.name)?.text = toiletNm
        view?.findViewById<TextView>(R.id.address)?.text = address
        view?.findViewById<TextView>(R.id.phone_number)?.text = phoneNumber
        view?.findViewById<TextView>(R.id.open_time)?.text = openTime

        view?.findViewById<TextView>(R.id.unisexToiletYn)?.text = "$unisexToiletYn"
        view?.findViewById<TextView>(R.id.menHandicap)?.text = "남성 장애인용 화장실: $menHandicap"
        view?.findViewById<TextView>(R.id.menChildren)?.text = "남성 어린이용 화장실: $menChildren"
        view?.findViewById<TextView>(R.id.ladiesHandicap)?.text = "여성 장애인용 화장실: $ladiesHandicap"
        view?.findViewById<TextView>(R.id.ladiesChildren)?.text = "여성 어린이용 화장실: $ladiesChildren"
    }

    private fun hideAppBar() {
        val appbar: AppBarLayout? = view?.findViewById(R.id.appBarLayout)
        val params = appbar?.layoutParams
        params?.height = 0
        appbar?.layoutParams = params
    }

    private fun showView(view: View, size: Int) {
        val params = view.layoutParams
        params.height = size
        view.layoutParams = params
    }

    private fun getActionBarSize(): Int {
        val array =
            requireContext().theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        return array.getDimension(0, 0f).toInt()
    }

    fun showDialog(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("짧게 누르면 문자, 길게 누르면 전화")
        val customLayout: View = layoutInflater.inflate(R.layout.alert_dialog, null)
        builder.setView(customLayout)
        builder.setNegativeButton("닫기") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        val recyclerView: RecyclerView =
            customLayout.findViewById(R.id.recyclerView)
        val llm = LinearLayoutManager(context)
        recyclerView.layoutManager = llm
        recyclerView.adapter = adapter
        alertDialog.show()
    }
}