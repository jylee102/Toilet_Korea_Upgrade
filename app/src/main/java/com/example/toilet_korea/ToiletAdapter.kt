package com.example.toilet_korea

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.toilet_korea.databinding.ToiletRowBinding
import com.google.android.gms.maps.model.LatLng

class ToiletAdapter(private val toiletList: List<Toilet>) : RecyclerView.Adapter<ToiletAdapter.ToiletHolder>() {

    private val DiffAsync = AsyncListDiffer(this, DiffCallbackAsync())

    inner class DiffCallbackAsync : DiffUtil.ItemCallback<Toilet>() {
        override fun areItemsTheSame(oldItem: Toilet, newItem: Toilet) =
            oldItem.toiletNm == newItem.toiletNm

        override fun areContentsTheSame(oldItem: Toilet, newItem: Toilet) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToiletHolder {
        val itemBinding = ToiletRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToiletHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ToiletHolder, position: Int) {
        val toilet: Toilet = toiletList[position]
        holder.bind(toilet)
    }

    override fun getItemCount(): Int = toiletList.size

    fun submitList(newList : ArrayList<Toilet>){
        DiffAsync.submitList(newList)
    }

    class ToiletHolder(private val itemBinding: ToiletRowBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        @SuppressLint("MissingPermission")
        fun getMyLocation(): LatLng {
            // 위치를 측정하는 프로바이더를 GPS 센서로 지정
            val locationProvider: String = LocationManager.GPS_PROVIDER
            // 위치 서비스 객체를 불러옴
            val locationManager = itemView.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            // 마지막으로 업데이트된 위치를 가져옴
            val lastKnownLocation: Location? = locationManager?.getLastKnownLocation(locationProvider)
            // 위도 경도 객체로 반환
            return if (lastKnownLocation != null) {
                // 위도 경도 객체로 반환
                LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
            } else {
                // 위치를 구하지 못한경우 기본값 반환
                MapFragment().CITY_HALL
            }
        }

        fun getDistance(latitude: Double, longitude: Double): Int {
            val currentLatLng = getMyLocation()

            val locationA = Location("A")
            val locationB = Location("B")

            locationA.latitude = currentLatLng.latitude
            locationA.longitude = currentLatLng.longitude
            locationB.latitude = latitude
            locationB.longitude = longitude

            return if (currentLatLng != MapFragment().CITY_HALL)
                locationA.distanceTo(locationB).toInt()
            else 0
        }

        @SuppressLint("SetTextI18n")
        fun bind(toilet: Toilet) {


            val distance = getDistance(toilet.latitude!!, toilet.longitude!!)

            itemBinding.tvName.text = toilet.toiletNm
            itemBinding.tvAddress.text = toilet.lnmadr
            itemBinding.tvUnisex.text = "공용화장실여부: ${toilet.unisexToiletYn}"

            if (distance != 0)
                itemBinding.tvDistance.text = "거리: ${distance}m"

            itemBinding.viewOnMap.setOnClickListener {
                val intent = Intent(it.context, MainActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable("toilet", toilet)
                bundle.putBoolean("clicked", true)
                intent.putExtra("bundle", bundle)
                it.context.startActivity(intent)
            }
        }
    }
}