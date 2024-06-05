package com.ashfaque.demoweatherkotlin.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ashfaque.demoweatherkotlin.KEY
import com.ashfaque.demoweatherkotlin.api.RetrofitHelper.apiService
import com.ashfaque.demoweatherkotlin.databinding.ActivityMainBinding
import com.ashfaque.demoweatherkotlin.model.WeatherResponse
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices



class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.cardViewBtn.setOnClickListener {

            val tSearch = mBinding.edSearch.text.toString()
            if(tSearch.isNotEmpty())
            {
                getWeather(tSearch)
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()

    }


    private fun requestLocationPermission() {
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        when {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                getLastLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // In an educational UI, explain to the user why your app requires this permission.
                Toast.makeText(this, "Location permission is required to get your current location", Toast.LENGTH_LONG).show()
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                // Directly ask for the permission
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                  //  Toast.makeText(this, "Lat: $latitude, Long: $longitude", Toast.LENGTH_LONG).show()
                    getWeather("$latitude,$longitude")
                } else {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_LONG).show()
                }
            }

    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun getWeather(cityName:String) {
       mBinding.progress.visibility= View.VISIBLE
       mBinding.nestedScrollView.visibility= View.GONE
       mBinding.tvDataNotFound.visibility= View.GONE
        GlobalScope.launch {
            //val apiService=RetrofitHelper.apiService
            val result= apiService.getWeather(KEY,cityName)
            if(result.isSuccessful)
            {

              val response=result.body()
                Log.d("Ashu",response.toString())
                if(response!=null)
                {
                    withContext(Dispatchers.Main)
                    {
                        setUI(response)
                    }
                }
            }
            else
            {

                withContext(Dispatchers.Main)
                {
                    mBinding.progress.visibility= View.INVISIBLE
                    mBinding.tvDataNotFound.visibility= View.VISIBLE
                }

            }


        }
    }

    @SuppressLint("CheckResult")
    private fun setUI(mResponse: WeatherResponse) {
        mBinding.progress.visibility= View.GONE
        mBinding.nestedScrollView.visibility= View.VISIBLE
        mBinding.tvTemp.text="${mResponse.current.temp_c} °C"
        mBinding.tvHumidity.text="${mResponse.current.humidity} %"
        mBinding.tvVisibility.text="${mResponse.current.vis_km} km/h"
        mBinding.tvWind.text="${mResponse.current.wind_kph} km/h"

        mBinding.tvLocationName.text="${mResponse.location.name} - ${mResponse.location.region} - ${mResponse.location.country}"
        mBinding.tvLocationDateTime.text=mResponse.location.localtime
        mBinding.tvTimeZone.text=mResponse.location.tz_id
        mBinding.tvLat.text=mResponse.location.lat
        mBinding.tvLong.text=mResponse.location.lon

        mBinding.tvCo.text=mResponse.current.air_quality.co+"\n(μg/m3)"
        mBinding.tvNO2.text=mResponse.current.air_quality.no2+"\n(μg/m3)"
        mBinding.tvO3.text=mResponse.current.air_quality.o3+"\n(μg/m3)"
        mBinding.tvSO2.text=mResponse.current.air_quality.so2+"\n(μg/m3)"

        Glide
            .with(this)
            .load("https:"+mResponse.current.condition.icon)
            .apply(RequestOptions().override(100, 100))
            .into(mBinding.imgTempCondition)

    }


}