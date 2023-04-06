package com.example.toilet_korea

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat.getDrawable
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


class MarkerClusterRenderer(
    context: Context?,
    map: GoogleMap,
    clusterManager: ClusterManager<Toilet?>?
) :
    DefaultClusterRenderer<Toilet>(context, map, clusterManager) {

    val c = context

    override fun onBeforeClusterItemRendered(
        item: Toilet,
        markerOptions: MarkerOptions
    ) {

        // 화장실 이미지로 사용할 Bitmap
        // Lazy 는 바로 생성하지 않고 처음 사용 될 때 생성하는 문법
        /*val bitmap by lazy {
            val drawable = getDrawable(c!!, R.drawable.restroom_sign) as BitmapDrawable
            Bitmap.createScaledBitmap(drawable.bitmap, 64, 64, false)
        }

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))*/
        markerOptions.title(item.title)
        markerOptions.draggable(true)
        super.onBeforeClusterItemRendered(item, markerOptions)
    }

    override fun onClusterItemRendered(item: Toilet, marker: Marker) {
        marker.tag = item.getTag()
        super.onClusterItemRendered(item, marker)
    }
}