package com.example.toilet_korea

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.IgnoreExtraProperties
import com.google.maps.android.clustering.ClusterItem
import java.io.Serializable


@IgnoreExtraProperties
data class Toilet(val toiletNm: String? = null, val rdnmadr: String? = null, val lnmadr: String? = null, val unisexToiletYn: String? = null,
                  val menToiletBowlNumber: Int? = null, val menUrineNumber: Int? = null, val menHandicapToiletBowlNumber: Int? = null,
                  val menHandicapUrinalNumber: Int? = null, val menChildrenToiletBowlNumber: Int? = null, val menChildrenUrinalNumber: Int? = null,
                  val ladiesToiletBowlNumber: Int? = null, val ladiesHandicapToiletBowlNumber: Int? = null, val ladiesChildrenToiletBowlNumber: Int? = null,
                  val phoneNumber: String? = null, val openTime: String? = null, val latitude: Double? = null, val longitude: Double? = null,
                  val emgBellYn: String? = null, val enterentCctvYn: String? = null, val dipersExchgPosi: String? = null): Serializable, Parcelable, ClusterItem {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(toiletNm)
        parcel.writeString(rdnmadr)
        parcel.writeString(lnmadr)
        parcel.writeString(unisexToiletYn)
        parcel.writeValue(menToiletBowlNumber)
        parcel.writeValue(menUrineNumber)
        parcel.writeValue(menHandicapToiletBowlNumber)
        parcel.writeValue(menHandicapUrinalNumber)
        parcel.writeValue(menChildrenToiletBowlNumber)
        parcel.writeValue(menChildrenUrinalNumber)
        parcel.writeValue(ladiesToiletBowlNumber)
        parcel.writeValue(ladiesHandicapToiletBowlNumber)
        parcel.writeValue(ladiesChildrenToiletBowlNumber)
        parcel.writeString(phoneNumber)
        parcel.writeString(openTime)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeString(emgBellYn)
        parcel.writeString(enterentCctvYn)
        parcel.writeString(dipersExchgPosi)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Toilet> {
        override fun createFromParcel(parcel: Parcel): Toilet {
            return Toilet(parcel)
        }

        override fun newArray(size: Int): Array<Toilet?> {
            return arrayOfNulls(size)
        }
    }

    override fun getPosition(): LatLng {
        return LatLng(latitude!!, longitude!!)
    }

    override fun getTitle(): String? {
        return toiletNm
    }

    override fun getSnippet(): String? {
        return null
    }

    fun getTag(): String {
        return rdnmadr + "/" +
                unisexToiletYn + "/" +
                phoneNumber + "/" +
                openTime + "/" +
                emgBellYn + "/" +
                enterentCctvYn + "/" +
                dipersExchgPosi + "/" +

                menToiletBowlNumber.toString() + "/" +
                menUrineNumber.toString() + "/" +
                menHandicapToiletBowlNumber.toString() + "/" +
                menHandicapUrinalNumber.toString() + "/" +
                menChildrenToiletBowlNumber.toString() + "/" +
                menChildrenUrinalNumber.toString() + "/" +
                ladiesToiletBowlNumber.toString() + "/" +
                ladiesHandicapToiletBowlNumber.toString() + "/" +
                ladiesChildrenToiletBowlNumber.toString()
    }

}