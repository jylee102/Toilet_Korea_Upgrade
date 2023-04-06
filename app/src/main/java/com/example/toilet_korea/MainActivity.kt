package com.example.toilet_korea

import android.annotation.SuppressLint
import android.media.SoundPool
import android.os.*
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private val mainFragment = MapFragment()

    val database: DatabaseReference = Firebase.database.reference

    private val currentUser = Firebase.auth.currentUser
    var curUID = currentUser?.uid.toString()

    var toilets = ArrayList<Toilet>()
    var toilet: Toilet? = null

    //뒤로가기 Listener역할을 할 Interface 생성
    interface onBackPressedListener {
        fun onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        QueryActivity().finish()

        val toolbar: Toolbar = findViewById(R.id.toolbar) // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.navi_menu) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        setNavigationDrawer() // call method

        val bundle = intent.getBundleExtra("bundle")

        if (bundle != null && bundle.containsKey("toilet")) {
            val toilet = bundle.getSerializable("toilet") as Toilet
            val clicked = bundle.getBoolean("clicked", false)
            val send = Bundle()
            send.putSerializable("toilet", toilet)
            send.putBoolean("searching", clicked)

            if (clicked){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame, mainFragment)
                    .commit()

                mainFragment.arguments = send
            }
        }
    }

    private fun setNavigationDrawer() {
        val dLayout: DrawerLayout = findViewById(R.id.drawer_layout) // initiate a DrawerLayout
        val navView: NavigationView = findViewById(R.id.nav_view) // initiate a Navigation View

        // implement setNavigationItemSelectedListener event on NavigationView
        navView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { menuItem ->
            var frag: Fragment? = null // create a Fragment Object
            val itemId = menuItem.itemId // get selected menu item's id
            // check selected menu item's id and replace a Fragment Accordingly
            when (itemId) {
                R.id.first -> {
                    if (curUID != "null"){
                        frag = FirstFragment()
                    } else {
                        Toast.makeText(applicationContext, "게스트 이용자에게는 제한된 접근입니다.", Toast.LENGTH_LONG).show()
                    }
                }
                R.id.second -> {
                    frag = SecondFragment()
                }
                R.id.third -> {
                    frag = ThirdFragment()
                }
                R.id.go_to_main -> {
                    frag = MapFragment()
                }
            }
            if (curUID != "null" || itemId != R.id.first){
                // display a toast message with menu item's title
                Toast.makeText(applicationContext, menuItem.title, Toast.LENGTH_SHORT).show()
            }

            if (frag != null) {
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame, frag) // replace a Fragment with Frame Layout
                transaction.commit() // commit the changes
                dLayout.closeDrawers() // close the all open Drawer Views
                return@OnNavigationItemSelectedListener true
            }
            false
        })
    }

    // 툴바 메뉴 버튼이 클릭 됐을 때 실행하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val dLayout: DrawerLayout = findViewById(R.id.drawer_layout) // initiate a DrawerLayout

        //왼쪽 바 상단 닉네임 불러오기
        val database: DatabaseReference = Firebase.database.reference
        val currentUser = Firebase.auth.currentUser
        val userRef = database.child("User").child(currentUser?.uid.toString()).child("userNm")

        val userListener = object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userName = dataSnapshot.value
                if (userName != null) {
                    findViewById<TextView>(R.id.text).text = userName.toString() + "님"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        userRef.addValueEventListener(userListener)

        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        when(item.itemId){
            android.R.id.home->{
                // 햄버거 버튼 클릭시 네비게이션 드로어 열기
                dLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //fragment에서 상속 - 뒤로가기
    //프레그먼트에서 뒤로가기 한번 누르면 메인액티비티(지도화면)으로
    private var backPressedTime : Long = 0
    override fun onBackPressed() {
        val fragmentList = supportFragmentManager.fragments
        for (fragment in fragmentList) {
            if (fragment is onBackPressedListener) {
                (fragment as onBackPressedListener).onBackPressed()
                return
            }
        }
        //두 번 클릭시 어플 종료
        // 2초내 다시 클릭하면 앱 종료
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            finish()
            return
        }
        // 처음 클릭 메시지
        Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
        backPressedTime = System.currentTimeMillis()
    }

    //DB의 toilet reference
    val toiletRef = database.child("toilet")

    inner class ToiletThread : Thread() {
        override fun run() {
            handler.sendEmptyMessage(0)
        }
    }

    // 스레드가 다운로드 받아서 파싱한 결과를 가지고 맵 뷰에 마커를 출력해달라고 요청
    val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {

            toiletRef.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("PotentialBehaviorOverride", "SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (dataSnapshot.exists()) {

                        // looping through to values
                        for (i in dataSnapshot.children) {

                            toilet = i.getValue(Toilet::class.java)!!

                            if (toilet?.latitude != null && toilet?.longitude != null){
                                toilets.add(toilet!!)
                            }
                        }

                        DataHolder.instance.data = ArrayList<Toilet>(toilets)

                        findViewById<LottieAnimationView>(R.id.pBar).visibility = View.GONE

                        supportFragmentManager.beginTransaction()
                            .replace(R.id.frame, mainFragment)
                            .commit()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    var toiletThread: ToiletThread? = null

    // 앱이 활성화될때 화장실 데이터를 읽어옴
    @SuppressLint("PotentialBehaviorOverride")
    override fun onStart() {
        super.onStart()

        val toilets = DataHolder.instance.data

        if (toilets == null){
            findViewById<LottieAnimationView>(R.id.pBar).visibility = View.VISIBLE
            if (toiletThread == null) {
                toiletThread = ToiletThread()
                toiletThread!!.start()
            }
        }else{
            DataHolder.instance.data = ArrayList<Toilet>(toilets)
        }
    }

    // 앱이 비활성화 될때 백그라운드 작업 취소
    override fun onStop() {
        super.onStop()
        toiletThread?.isInterrupted
        toiletThread = null
    }
}