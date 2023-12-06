package com.htnguyen.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.htnguyen.weatherapp.adater.WeatherDailyAdapter
import com.htnguyen.weatherapp.databinding.ActivityMainBinding
import com.htnguyen.weatherapp.model.WeatherDaily
import com.htnguyen.weatherapp.support.InputMethodManager
import com.htnguyen.weatherapp.support.hideKeyboard
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override val layout: Int = R.layout.activity_main
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    var layoutBottomSheet: LinearLayout? = null
    var layoutManager: LinearLayoutManager? = null
    private var weatherTimeModelArrayList: ArrayList<WeatherTimeModel>? = null
    private var weatherTimeAdapter: WeatherTimeAdapter? = null
    private var locationManager: LocationManager? = null
    private var cityName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindHideKeyboardListener(binding.root, binding.content)
        setBottomSheetBehavior()
        setDataTime()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                Constants.PERMISSION_CODE
            )
        }

        val location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location != null) {
            getCityName(location.longitude, location.latitude)
            getWeatherInformation(location.latitude, location.longitude)
            getWeatherDaily(location.latitude, location.longitude)
        } else {
            getCityName( 105.8544441, 21.0294498)
            getWeatherInformation(21.0294498, 105.8544441)
            getWeatherDaily(21.0294498, 105.8544441)
        }

        binding.layoutSearch.imgSearch.setOnClickListener {
            getLatAndLon(binding.layoutSearch.edtSearch.text.toString())
            InputMethodManager(this)?.hideKeyboard(it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted ...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please provide the permissions ...", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun getCityName(longitude: Double, latitude: Double) {
        var cityName = "Not found"
        val gcd = Geocoder(baseContext, Locale.getDefault())
        try {
            val addresses = gcd.getFromLocation(latitude, longitude, 10)
            for (adr in addresses) {
                if (adr != null) {
                    val city = adr.locality
                    if (city != null && city != "") {
                        cityName = city + " - " + adr.adminArea
                        binding.includeInformationWeather.txtLocal.text = cityName
                    } else {
                        Toast.makeText(this, "User City Not Found...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getLatAndLon(cityName: String) {
        val url = "https://api.openweathermap.org/geo/1.0/direct?q=" + cityName + "&limit=5&appid=2ee8d9863c95a45ac42ddbe5085fe3a6"
        val requestQueue = Volley.newRequestQueue(this@MainActivity)
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null, { response: JSONArray ->
                try {
                    val lat = response.getJSONObject(0).getDouble("lat")
                    val lon = response.getJSONObject(0).getDouble("lon")
                    getCityName(lon, lat)
                    getWeatherInformation(lat, lon)
                    getWeatherDaily(lat, lon)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }) { error: VolleyError? ->
                Toast.makeText(
                    this@MainActivity,
                    error.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        requestQueue.add(jsonArrayRequest)
    }

    private fun setBottomSheetBehavior() {
        layoutBottomSheet = binding.layoutBottomsheet.bottomSheetWeather
        bottomSheetBehavior = BottomSheetBehavior.from(layoutBottomSheet!!)
        (bottomSheetBehavior as BottomSheetBehavior<*>).addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        binding.layoutBottomsheet.txtTitle.setOnClickListener { v: View? ->
            if ((bottomSheetBehavior as BottomSheetBehavior<*>).state != BottomSheetBehavior.STATE_EXPANDED) {
                (bottomSheetBehavior as BottomSheetBehavior<*>).setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                (bottomSheetBehavior as BottomSheetBehavior<*>).setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
    }

    private fun setDataTime() {
        weatherTimeModelArrayList = ArrayList()
        weatherTimeAdapter = WeatherTimeAdapter(this, weatherTimeModelArrayList)
        layoutManager = LinearLayoutManager(this@MainActivity)
        binding.includeDayWeather.rcvWeatherDetail.layoutManager = layoutManager
        binding.includeDayWeather.textTitleDetail.setText(R.string.time_weather)
        binding.includeDayWeather.rcvWeatherDetail.adapter = weatherTimeAdapter
    }

    private fun getWeatherInformation(latitude: Double, longitude: Double) {
        val url =
            "https://api.openweathermap.org/data/2.5/weather?lat="+ latitude +
                "&lon="+ longitude + "&appid=2ee8d9863c95a45ac42ddbe5085fe3a6"

        val requestQueue = Volley.newRequestQueue(this@MainActivity)
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject ->
                try {
                    val temperature = response.getJSONObject("main").getDouble("temp") - 272.15
                    binding.includeInformationWeather.txtTemplate.text = String.format(getString(R.string.main_temp), temperature.toFloat())

                    val description = response.getJSONArray("weather").getJSONObject(0).getString("description")
                    binding.includeInformationWeather.txtForecast.text = description

                    val conditionIcon = response.getJSONArray("weather").getJSONObject(0).getString("icon")
                    Picasso.get().load("https://openweathermap.org/img/wn/${conditionIcon}.png").into(binding.includeInformationWeather.imageView)

                    val wind = response.getJSONObject("wind").getDouble("speed")
                    binding.includeInformationWeather.txtWindSpeed.text = String.format(getString(R.string.main_wind), wind.toFloat())

                    val hump = response.getJSONObject("main").getDouble("humidity")
                    binding.includeInformationWeather.txtHumidity.text = String.format(getString(R.string.main_hum), hump.toFloat())

                    val dewPoint = response.getDouble("visibility")
                    binding.includeInformationWeather.txtDewPoint.text = String.format(getString(R.string.main_visibility), dewPoint.toFloat()/1000)

                    val cloud = response.getJSONObject("clouds").getString("all")
                    binding.includeInformationWeather.txtClouds.text = cloud.toString()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }) { error: VolleyError? ->
                Toast.makeText(
                    this@MainActivity,
                    "Please enter valid city name...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        requestQueue.add(jsonObjectRequest)
    }


    private fun getWeatherDaily(latitude: Double, longitude: Double) {
        val url =
            "https://api.openweathermap.org/data/2.5/forecast?lat="+ latitude +
                    "&lon="+ longitude + "&appid=2ee8d9863c95a45ac42ddbe5085fe3a6"
        val weatherList = ArrayList<WeatherDaily>()
        val requestQueue = Volley.newRequestQueue(this@MainActivity)
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url, null, { response: JSONObject ->
                try {
                    val list = response.getJSONArray("list")

                    for (i in 0 until list.length()) {
                        val dt = list.getJSONObject(i).getLong("dt")
                        val sunrise = 0L
                        val sunset = 0L
                        val tempMin = list.getJSONObject(i).getJSONObject("main").getDouble("temp_min").toFloat()
                        val temMax = list.getJSONObject(i).getJSONObject("main").getDouble("temp_max").toFloat()
                        val humidity= list.getJSONObject(i).getJSONObject("main").getInt("humidity").toFloat()
                        val weatherIcon = list.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon")
                        weatherList.add(WeatherDaily(dt, sunrise, sunset, tempMin, temMax, humidity, weatherIcon))
                        val adapter = WeatherDailyAdapter(weatherList, this@MainActivity)
                        val manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        binding.includeDayWeather.rcvWeatherDetail.setHasFixedSize(true)
                        binding.includeDayWeather.rcvWeatherDetail.layoutManager = manager
                        binding.includeDayWeather.rcvWeatherDetail.adapter = adapter
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }) { error: VolleyError? ->

            }
        requestQueue.add(jsonObjectRequest)
    }


}