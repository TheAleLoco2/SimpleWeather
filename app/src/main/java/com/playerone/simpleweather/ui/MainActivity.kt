package com.playerone.simpleweather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.api.load
import coil.request.LoadRequest
import coil.transition.CrossfadeTransition
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.playerone.simpleweather.R
import com.playerone.simpleweather.model.current.CurrentWeatherResponse
import com.playerone.simpleweather.model.current.Weather
import com.playerone.simpleweather.model.forecast.Daily
import com.playerone.simpleweather.model.forecast.ForecastWeatherResponse
import com.playerone.simpleweather.model.forecast.Hourly
import com.playerone.simpleweather.network.VolleyGet
import com.playerone.simpleweather.utils.SharedPreferences
import com.playerone.simpleweather.utils.WeatherUtil
import com.utsman.recycling.setupAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.cardview_no_city.*
import kotlinx.android.synthetic.main.daily_cardview.*
import kotlinx.android.synthetic.main.daily_forecast_item.view.*
import kotlinx.android.synthetic.main.hourly_forecast_item.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    val API_FORECAST = "https://api.openweathermap.org/data/2.5/onecall"
    val API_CURRENT = "https://api.openweathermap.org/data/2.5/weather"
    val TAG = "MainActivity"
    val AUTOCOMPLETE_REQUEST_CODE = 100
    val apiKey = "0b51abdf1359842d93d19cbcdf623a8a"
    var savedLocation = ""
    var currentTempUnit = ""
    var useGPS = false

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hideItems(hideItems = true, hideProgress = false, hideCardGPS = true)

        val apiKey = getString(R.string.places_api)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        Places.createClient(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        savedLocation = SharedPreferences(this).currentLocation
        currentTempUnit = SharedPreferences(this).temperatureUnit
        useGPS = SharedPreferences(this).useGPS

        //Log.d(TAG, "TempUnit: $currentTempUnit")
        //Log.d(TAG, "CurrentLocation: $savedLocation")

        if (useGPS) {
            getLastLocation()
        } else {
            if (savedLocation.isNotBlank()) {
                getCurrentData(savedLocation, Locale.getDefault().language)
            } else {
                //preguntar si quieren utilizar el gps para cargar los datos
                hideItems(hideItems = true, hideProgress = true, hideCardGPS = false)
            }
        }

        img_author_txt.movementMethod = LinkMovementMethod.getInstance()

        btn_manual.setOnClickListener {
            onSearchCalled()
        }
        btn_gps.setOnClickListener {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (checkLocationPermission()) {

            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        getNewLocation()
                    } else {
                        Log.d(TAG, "Coord1: Lat: ${location.latitude}, Long: ${location.longitude}")

                        val city = getCityName(location.latitude, location.longitude)

                        SharedPreferences(this).currentLocation = city
                        savedLocation = city

                        if (savedLocation.isNotBlank()) {
                            hideItems(hideItems = true, hideProgress = false, hideCardGPS = true)
                            getCurrentData(savedLocation, Locale.getDefault().language)
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.location_enable),
                    Toast.LENGTH_LONG
                ).show()

                hideItems(hideItems = true, hideProgress = false, hideCardGPS = true)
                getCurrentData(savedLocation, Locale.getDefault().language)
            }
        } else {
            requestLocationPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
        locationRequest = LocationRequest()

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )

    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            val lastLocation: Location = p0!!.lastLocation

            Log.d(TAG, "Coord2: Lat: ${lastLocation.latitude}, Long: ${lastLocation.longitude}")

            val city = getCityName(lastLocation.latitude, lastLocation.longitude)

            SharedPreferences(this@MainActivity).currentLocation = city
            savedLocation = city

            if (savedLocation.isNotBlank()) {
                hideItems(hideItems = true, hideProgress = false, hideCardGPS = true)
                getCurrentData(savedLocation, Locale.getDefault().language)
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            110
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            (LocationManager.NETWORK_PROVIDER)
        )
    }

    private fun getCityName(lat: Double, long: Double): String {
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address: MutableList<Address> = geoCoder.getFromLocation(lat, long, 1)
        return address[0].locality
    }

    private fun hideItems(hideItems: Boolean, hideProgress: Boolean, hideCardGPS: Boolean) {
        if (hideItems) {
            cardView.visibility = View.GONE
            img_author_txt.visibility = View.GONE
            background_img.visibility = View.GONE
            recycler_hourly.visibility = View.GONE
            cardview1.visibility = View.GONE
        } else {
            cardView.visibility = View.VISIBLE
            img_author_txt.visibility = View.VISIBLE
            background_img.visibility = View.VISIBLE
            recycler_hourly.visibility = View.VISIBLE
            cardview1.visibility = View.VISIBLE
        }

        if (hideProgress) {
            progressBar.visibility = View.GONE
        } else {
            progressBar.visibility = View.VISIBLE
        }

        if (hideCardGPS) {
            cardview2.visibility = View.GONE
        } else {
            cardview2.visibility = View.VISIBLE
        }
    }

    private fun getCurrentData(city: String, language: String) {
        VolleyGet(API_CURRENT, {
            val gson = Gson()
            setCurrentData(gson.fromJson(it.toString(), CurrentWeatherResponse::class.java))
        }, {
            Log.d(TAG, it)
        }).GET("q" to city, "lang" to language, "appid" to apiKey)
    }

    private fun getForecastData(lat: Double, lon: Double) {

        VolleyGet(API_FORECAST, {
            val gson = Gson()

            val weatherResponse = gson.fromJson(it.toString(), ForecastWeatherResponse::class.java)
            setRecyclerHourly(weatherResponse.hourly.take(24))
            setRecyclerDaily(weatherResponse.daily)

            //forecastVM.setData(gson.fromJson(it.toString(), ForecastWeatherResponse::class.java))

        }, {
            Log.d(TAG, it)
        }).GET(
            "lat" to lat,
            "lon" to lon,
            "exclude" to "minutely,current",
            "lang" to "es",
            "appid" to apiKey
        )
    }

    private fun setCurrentData(weatherResponse: CurrentWeatherResponse) {

        val height = coordinator.bottom
        background_img.layoutParams.height = height

        getForecastData(weatherResponse.coord.lat, weatherResponse.coord.lon)

        Log.d(TAG, weatherResponse.toString())

        val currentWeatherData: Weather = weatherResponse.weather[0]

        val imgData = WeatherUtil().imgPath(currentWeatherData.icon)

        weather_icon.load(WeatherUtil().getIcon(currentWeatherData.icon))

        val imageLoader = ImageLoader.Builder(this).build()
        val request = LoadRequest.Builder(this)
            .data("https://source.unsplash.com/${imgData[0]}/576x1024")
            .placeholder(R.drawable.color_background)
            .crossfade(true)
            .transition(CrossfadeTransition())
            .target(
                onStart = { placeholder ->
                    background_img.background = placeholder
                },
                onSuccess = { result ->
                    background_img.background = result
                    hideItems(hideItems = false, hideProgress = true, hideCardGPS = true)
                    // Handle the successful result.
                }
            )
            .build()
        imageLoader.execute(request)

        supportActionBar!!.title = weatherResponse.name
        main_temp.text = getTemp(weatherResponse.main.temp)
        main_description.text = currentWeatherData.description.capitalize()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            img_author_txt.text = Html.fromHtml(imgData[1], HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            img_author_txt.text = Html.fromHtml(imgData[1])
        }

        //forecastVM.setCoord(mutableListOf(weatherResponse.coord.lat, weatherResponse.coord.lon))
    }

    private fun getTemp(temp: Double): String {
        //en esta funcion checamos los sharedpreferences para saber en que formato
        //quiere la temperatura el usuario para posteriormente hacer la conversion
        //y regresar el string
        return when (SharedPreferences(this).temperatureUnit) {
            "Fahrenheit" -> {
                val fahren = (temp - 273.15) * 9.0 / 5 + 32
                "${fahren.roundToInt()}°F"
            }
            else -> {
                "${(temp - 273.15).roundToInt()}°C"
            }
        }

    }

    private fun getTime(dt: Int, pattern: String): String {
        val yourSeconds = dt.toLong()
        val d = Date(yourSeconds * 1000)
        val df: DateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return df.format(d)
    }

    private fun setRecyclerHourly(hourlyList: List<Hourly>) {

        recycler_hourly.setupAdapter<Hourly>(R.layout.hourly_forecast_item) { adapter, context, list ->

            bind { itemView, position, item ->

                //Log.d(TAG, "Time: ${getTime(item!!.dt)}")

                itemView.forecast_hour.text = getTime(item!!.dt, "h a")
                itemView.forecast_icon.load(WeatherUtil().getIcon(item.weather[0].icon))
                itemView.forecast_temp.text = getTemp(item.temp)
            }
            setLayoutManager(LinearLayoutManager(context, RecyclerView.HORIZONTAL, false))
            submitList(hourlyList)

        }

    }

    private fun setRecyclerDaily(dailyList: List<Daily>) {

        recycler_daily2.setupAdapter<Daily>(R.layout.daily_forecast_item) { adapter, context, list ->

            bind { itemView, position, item ->

                itemView.daily_item_day.text = getTime(item!!.dt, "EEEE, d").capitalize()
                itemView.daily_item_icon.load(WeatherUtil().getIcon(item.weather[0].icon))
                itemView.daily_item_max.text = getTemp(item.temp.max)
                itemView.daily_item_min.text = getTemp(item.temp.min)

            }
            setLayoutManager(LinearLayoutManager(context))
            submitList(dailyList)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_search -> {
                onSearchCalled()
            }
            R.id.menu_refresh -> {
                hideItems(hideItems = true, hideProgress = false, hideCardGPS = true)
                if (SharedPreferences(this).useGPS) {
                    getLastLocation()
                } else {
                    if (savedLocation.isNotBlank()) {
                        getCurrentData(savedLocation, Locale.getDefault().language)
                    } else {
                        //preguntar si quieren utilizar el gps para cargar los datos
                        hideItems(hideItems = true, hideProgress = true, hideCardGPS = false)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        if (currentTempUnit != SharedPreferences(this).temperatureUnit) {

            if (useGPS != SharedPreferences(this).useGPS) {

                if (SharedPreferences(this).useGPS) {
                    //Log.d("onResume", "Unit & GPS")
                    currentTempUnit = SharedPreferences(this).temperatureUnit
                    useGPS = SharedPreferences(this).useGPS
                    getLastLocation()
                } else {
                    hideItems(hideItems = true, hideProgress = false, hideCardGPS = true)
                    currentTempUnit = SharedPreferences(this).temperatureUnit
                    useGPS = SharedPreferences(this).useGPS
                    getCurrentData(savedLocation, Locale.getDefault().language)
                }

            } else {
                //Log.d("onResume", "Only Unit")
                hideItems(hideItems = true, hideProgress = false, hideCardGPS = true)
                currentTempUnit = SharedPreferences(this).temperatureUnit
                getCurrentData(savedLocation, Locale.getDefault().language)
            }

        } else if (useGPS != SharedPreferences(this).useGPS) {
            //Log.d("onResume", "Only GPS")
            if (SharedPreferences(this).useGPS) {
                useGPS = SharedPreferences(this).useGPS
                getLastLocation()
            } else {
                hideItems(hideItems = true, hideProgress = false, hideCardGPS = true)
                useGPS = SharedPreferences(this).useGPS
                getCurrentData(savedLocation, Locale.getDefault().language)
            }
        }

    }

    override fun onPause() {
        super.onPause()

        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        val fields: List<Place.Field> = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG
        )
        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY, fields
        ).setHint(resources.getString(R.string.search_your_city))
            .setTypeFilter(TypeFilter.CITIES) //NIGERIA
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    Log.d(TAG, "Place: ${place.name}, ${place.id}, ${place.latLng}")
                    savedLocation = place.name.toString()
                    SharedPreferences(this).currentLocation = savedLocation
                    hideItems(hideItems = true, hideProgress = false, hideCardGPS = true)
                    getCurrentData(
                        savedLocation,
                        Locale.getDefault().language
                    )
                    if (SharedPreferences(this).useGPS) {
                        SharedPreferences(this).useGPS = false
                        useGPS = false
                        Toast.makeText(
                            this,
                            resources.getString(R.string.gps_deactivated),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Log.d(TAG, status.statusMessage.toString())
                }
                Activity.RESULT_CANCELED -> {

                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 110 && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted")
                SharedPreferences(this).useGPS = true
                getLastLocation()
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.accept_permission),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}