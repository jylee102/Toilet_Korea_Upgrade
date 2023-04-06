package com.example.toilet_korea

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.android.clustering.ClusterManager

class MapFragment : Fragment(), OnMapReadyCallback {

    private var clusterManager: ClusterManager<Toilet?>? = null

    private var toilets: Collection<Toilet>? = null

    val database: DatabaseReference = Firebase.database.reference

    private val soundPool = SoundPool.Builder().build()
    private var emgSoundId: Int? = null

    var rootView: View? = null
    var mapView: MapView? = null

    // 런타임에서 권한이 필요한 퍼미션 목록
    val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // 퍼미션 승인 요청시 사용하는 요청 코드
    val REQUEST_PERMISSION_CODE = 1

    // 기본 맵 줌 레벨
    val DEFAULT_ZOOM_LEVEL = 17f

    // 현재위치를 가져올수 없는 경우 서울 시청의 위치로 지도를 보여주기 위해 서울시청의 위치를 변수로 선언
    // LatLng 클래스는 위도와 경도를 가지는 클래스
    val CITY_HALL = LatLng(37.50203121152806, 127.03054633381461)

    // 구글 맵 객체를 참조할 멤버 변수
    var googleMap: GoogleMap? = null

    val bottomSheet = BottomSheet()

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        rootView = inflater.inflate(R.layout.map_view, container, false)
        mapView = rootView!!.findViewById<View>(R.id.mapView) as MapView
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
        return rootView
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<FloatingActionButton>(R.id.myLocationButton)?.setOnClickListener {
            when {
                hasPermissions() -> googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(getMyLocation(), DEFAULT_ZOOM_LEVEL)
                )
                else -> Toast.makeText(requireContext().applicationContext, "위치사용권한 설정에 동의해주세요", Toast.LENGTH_LONG)
                    .show()
            }
        }

        //비상벨
        emgSoundId = soundPool.load(requireContext(), R.raw.alarm, 1)
        var emgOnOff = 0

        //버튼이 눌리면 사운드 ON
        view.findViewById<FloatingActionButton>(R.id.sirenButton)?.setOnClickListener {

            if(emgOnOff == 0) {
                Log.i("emgsiren", "Clicked")
                emgSoundId?.let { soundId ->
                    soundPool.play(soundId, 2F, 2F, 0, 0 - 1, 1F)
                }
                emgOnOff = 1
            } else{
                soundPool.autoPause()
                emgOnOff = 0
            }

        }
    }

    override fun onResume() {
        mapView!!.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        MapsInitializer.initialize(this.requireActivity())

        // 앱이 실행될때 런타임에서 위치 서비스 관련 권한체크
        if (hasPermissions()) {

            // 클러스터 매니저 세팅
            clusterManager = ClusterManager(context, googleMap)
            googleMap.setOnCameraIdleListener(clusterManager)
            googleMap.setOnMarkerClickListener(clusterManager)

            val clusterRenderer = MarkerClusterRenderer(context, googleMap, clusterManager)
            clusterManager!!.renderer = clusterRenderer

            // 권한이 있는 경우 맵 초기화
            initMap()

            addMarkers()
        } else {
            // 권한 요청
            requestPermissions(PERMISSIONS, REQUEST_PERMISSION_CODE)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 맵 초기화
        initMap()
    }

    // 앱에서 사용하는 권한이 있는지 체크하는 함수
    fun hasPermissions(): Boolean {
        // 퍼미션목록중 하나라도 권한이 없으면 false 반환
        for (permission in PERMISSIONS) {
            if (context?.let {
                    ActivityCompat.checkSelfPermission(
                        it.applicationContext,
                        permission
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }


    // 맵 초기화하는 함수
    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    fun initMap() {
        // 맵뷰에서 구글 맵을 불러오는 함수. 컬백함수에서 구글 맵 객체가 전달됨
        mapView?.getMapAsync {
            // 구글맵 멤버 변수에 구글맵 객체 저장
            googleMap = it

            // 현재위치로 이동 버튼 비활성화
            it.uiSettings.isMyLocationButtonEnabled = false
            // 위치 사용 권한이 있는 경우
            when {
                hasPermissions() -> {
                    // 현재위치 표시 활성화
                    it.isMyLocationEnabled = true
                    // 현재위치로 카메라 이동
                    it.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            getMyLocation(),
                            DEFAULT_ZOOM_LEVEL
                        )
                    )
                }
                else -> {
                    // 권한이 없으면 서울시청의 위치로 이동
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(CITY_HALL, DEFAULT_ZOOM_LEVEL))
                }
            }

            searching()

            googleMap?.setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener { marker ->

                val arr = marker.tag.toString().split("/") //마커에 붙인 태그
                val args = Bundle()
                args.putString("toiletNm", marker.title.toString())
                args.putString("rdnmadr", arr[0])
                args.putString("lnmadr", marker.snippet.toString())
                args.putString("unisexToiletYn", arr[1])
                args.putString("menToiletBowlNumber", arr[7])
                args.putString("menUrineNumber", arr[8])
                args.putString("ladiesToiletBowlNumber", arr[13])

                if (arr[1] == "Y")
                    args.putString("unisexToiletYn", "남녀공용")
                else
                    args.putString("unisexToiletYn", "남녀분리")

                if (arr[9] != "0" || arr[10] != "0")
                    args.putString("menHandicap", "O")
                else
                    args.putString("menHandicap", "X")

                if (arr[11] != "0" || arr[12] != "0")
                    args.putString("menChildren", "O")
                else
                    args.putString("menChildren", "X")

                if (arr[14] != "0")
                    args.putString("ladiesHandicap", "O")
                else
                    args.putString("ladiesHandicap", "X")

                if (arr[15] != "0")
                    args.putString("ladiesChildren", "O")
                else
                    args.putString("ladiesChildren", "X")

                args.putString("phoneNumber", arr[2])
                args.putString("openTime", arr[3])
                args.putString("position", marker.position.toString())

                args.putParcelable("latlng", marker.position)

                bottomSheet.arguments = args
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)

                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(marker.position, DEFAULT_ZOOM_LEVEL))

                return@OnInfoWindowClickListener
            })
        }
    }

    @SuppressLint("MissingPermission")
    fun getMyLocation(): LatLng {
        // 위치를 측정하는 프로바이더를 GPS 센서로 지정
        val locationProvider: String = LocationManager.GPS_PROVIDER
        // 위치 서비스 객체를 불러옴
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        // 마지막으로 업데이트된 위치를 가져옴
        val lastKnownLocation: Location? = locationManager?.getLastKnownLocation(locationProvider)
        // 위도 경도 객체로 반환
        return if (lastKnownLocation != null) {
            // 위도 경도 객체로 반환
            LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
        } else {
            // 위치를 구하지 못한경우 기본값 반환
            CITY_HALL
        }
    }

    fun addMarkers(){

        if (arguments == null || !requireArguments().containsKey("toilet")) {

            toilets = DataHolder.instance.data

            if (toilets != null){
                clusterManager?.addItems(toilets)
                googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
            }
        }
    }

    fun searching(){
        if (arguments != null && requireArguments().containsKey("toilet")) {

            val toilet = arguments?.getSerializable("toilet") as Toilet
            val searching = arguments?.getBoolean("searching")
            val chosenPosition = LatLng(toilet.latitude!!, toilet.longitude!!)

            if (searching == true){
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(chosenPosition, DEFAULT_ZOOM_LEVEL))

                val special = googleMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(toilet.latitude, toilet.longitude))
                        .title(toilet.toiletNm as String)
                        .snippet(toilet.lnmadr as String)
                )

                special?.tag = toilet.rdnmadr + "/" +
                        toilet.unisexToiletYn + "/" +
                        toilet.phoneNumber + "/" +
                        toilet.openTime + "/" +
                        toilet.emgBellYn + "/" +
                        toilet.enterentCctvYn + "/" +
                        toilet.dipersExchgPosi + "/" +

                        toilet.menToiletBowlNumber.toString() + "/" +
                        toilet.menUrineNumber.toString() + "/" +
                        toilet.menHandicapToiletBowlNumber.toString() + "/" +
                        toilet.menHandicapUrinalNumber.toString() + "/" +
                        toilet.menChildrenToiletBowlNumber.toString() + "/" +
                        toilet.menChildrenUrinalNumber.toString() + "/" +
                        toilet.ladiesToiletBowlNumber.toString() + "/" +
                        toilet.ladiesHandicapToiletBowlNumber.toString() + "/" +
                        toilet.ladiesChildrenToiletBowlNumber.toString()

                val args = Bundle()
                args.putString("toiletNm", toilet.toiletNm)
                args.putString("rdnmadr", toilet.rdnmadr)
                args.putString("lnmadr", toilet.lnmadr)
                args.putString("unisexToiletYn", toilet.unisexToiletYn)
                args.putString("menToiletBowlNumber", toilet.menToiletBowlNumber.toString())
                args.putString("menUrineNumber", toilet.menUrineNumber.toString())
                args.putString("ladiesToiletBowlNumber", toilet.ladiesToiletBowlNumber.toString())

                if (toilet.unisexToiletYn == "Y")
                    args.putString("unisexToiletYn", "남녀공용")
                else
                    args.putString("unisexToiletYn", "남녀분리")

                if (toilet.menHandicapToiletBowlNumber != 0 || toilet.menHandicapUrinalNumber != 0)
                    args.putString("menHandicap", "O")
                else
                    args.putString("menHandicap", "X")

                if (toilet.menChildrenToiletBowlNumber != 0 || toilet.menChildrenUrinalNumber != 0)
                    args.putString("menChildren", "O")
                else
                    args.putString("menChildren", "X")

                if (toilet.ladiesHandicapToiletBowlNumber != 0)
                    args.putString("ladiesHandicap", "O")
                else
                    args.putString("ladiesHandicap", "X")

                if (toilet.ladiesChildrenToiletBowlNumber != 0)
                    args.putString("ladiesChildren", "O")
                else
                    args.putString("ladiesChildren", "X")

                args.putString("phoneNumber", toilet.phoneNumber)
                args.putString("openTime", toilet.openTime)
                args.putString("position", chosenPosition.toString())

                bottomSheet.arguments = args
                bottomSheet.show(parentFragmentManager, bottomSheet.tag)
            }
        }
    }

}