package com.example.utrend

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.action_bar.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var arrayAdapter : ArrayAdapter<String>

    companion object {
        var totalFetched : Int = 50
        var maxResult : Int = 50
        var nextPageToken : String = ""
        var totalResults : Int = 0
        var countryCode : String = "ID"
        var pageInfo = PageInfo(0)
        var hf = HomeFeed(ArrayList(), "", pageInfo)
        var isFetching = false
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        var pastVisiblesItems: Int
        var visibleItemCount: Int
        var totalItemCount: Int


        val mLayoutManager =
            LinearLayoutManager(this)
        recyleView_main.layoutManager = mLayoutManager
        recyleView_main.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = mLayoutManager.getChildCount()
                    totalItemCount = mLayoutManager.getItemCount()
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition()
                    if (visibleItemCount + pastVisiblesItems >= totalItemCount && !isFetching) {
                        nextFetch()
                    }
                }
            }
        })

        textView_notFound.visibility = TextView.GONE
        imageView_notFound.visibility = ImageView.GONE

        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.action_bar)

        val arrayList: ArrayList<String> = ArrayList()

        arrayList.add("Pick Country")

        val countryCodes = Locale.getISOCountries()
        for (countryCode in countryCodes) {
            val locale = Locale("en", countryCode)
            val name = locale.displayCountry
            arrayList.add(name)
        }

        arrayAdapter =
            ArrayAdapter(this, R.layout.color_spinner_layout, arrayList)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_layout)
        spinner.adapter = arrayAdapter
        spinner.onItemSelectedListener = this
        requestLocationPermission()
//        setCountryBasedGPS()

        fetchJSON(countryCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    fun setCountryBasedGPS() {
        var geocoder = Geocoder(this, Locale.getDefault())
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                println("Last know location lat : " + location?.latitude)
                println("Last know location long : " + location?.longitude)
                val addresses: MutableList<Address> =
                    geocoder.getFromLocation(location!!.latitude, location!!.longitude, 1)
                val obj = addresses[0]
                println("Your country is : " + obj.countryName)
                println("Your country code is : " + obj.countryCode)
                countryCode = obj.countryCode
                val indexCountry = arrayAdapter.getPosition(obj.countryName)
                println("Your country index is : " + indexCountry)
                spinner.setSelection(indexCountry)
            }
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    fun requestLocationPermission() {
        val perms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (EasyPermissions.hasPermissions(this, *perms)) {
//            setCountryBasedGPS()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Please grant the location permission to get your country",
                REQUEST_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun nextFetch () {
        if (totalFetched < totalResults) {
            val bundle = Bundle()
            bundle.putInt("total_fetched", totalFetched)
            firebaseAnalytics.logEvent("fetching_data", bundle)

            progressBarApi.visibility = ProgressBar.VISIBLE
            isFetching = true
            val url = "https://www.googleapis.com/youtube/v3/videos?part=statistics,snippet,contentDetails&chart=mostPopular&regionCode=ID&maxResults=1&key=AIzaSyCJuEsWhS1Nbv5TwWfQ7HYkq9Ulepd5z3Q&pageToken=${nextPageToken}"

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()

                    val gson = GsonBuilder().create()

                    val homeFeed = gson.fromJson(body, HomeFeed::class.java)

                    val itemLength = homeFeed.items.size - 1

                    for(index in 0..itemLength){

                        println(index)

                        var maxresUrl : String = ""
                        var standardUrl : String = ""
                        var highUrl : String = ""
                        var mediumUrl : String = ""
                        val defaultUrl = homeFeed.items.get(index).snippet.thumbnails.default.url

                        try {
                            maxresUrl = homeFeed.items.get(index).snippet.thumbnails.maxres.url
                        } catch (e: NullPointerException) {
                            println("Maxres not found")
                        }

                        try {
                            standardUrl = homeFeed.items.get(index).snippet.thumbnails.standard.url
                        } catch (e: NullPointerException) {
                            println("Standard not found")
                        }

                        try {
                            highUrl = homeFeed.items.get(index).snippet.thumbnails.high.url
                        } catch (e: NullPointerException) {
                            println("High not found")
                        }

                        try {
                            mediumUrl = homeFeed.items.get(index).snippet.thumbnails.medium.url
                        } catch (e: NullPointerException) {
                            println("medium not found")
                        }

                        val _maxres = Maxres(maxresUrl)
                        val _standard = Standard(standardUrl)
                        val _high = High(highUrl)
                        val _medium = Medium(mediumUrl)
                        val _default = Default(defaultUrl)

                        val thumbnails = Thumbnails(_default, _medium, _high, _standard, _maxres)
                        val snippet = Snippet(homeFeed.items.get(index).snippet.title, homeFeed.items.get(index).snippet.channelTitle, homeFeed.items.get(index).snippet.publishedAt, thumbnails)
                        val contentDetails = ContentDetails(homeFeed.items.get(index).contentDetails.duration)
                        val statistics = Statistics(homeFeed.items.get(index).statistics.viewCount)
                        val item = Item(homeFeed.items.get(index).id, snippet, contentDetails, statistics)

                        hf.items.add(item)
                    }

                    if ((totalFetched + maxResult) < totalResults) {
                        nextPageToken = homeFeed.nextPageToken
                    }

                    runOnUiThread {
                        recyleView_main.adapter?.notifyDataSetChanged()
                        progressBarApi.visibility = ProgressBar.GONE
                        isFetching = false
                        totalFetched += maxResult

                        println(url)
                        println(totalFetched)
                        println(totalResults)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    println("Failed to fetch the data")
                    progressBarApi.visibility = ProgressBar.GONE
                }
            })
        }
//        val maxres = Maxres("https://i.ytimg.com/vi/TgOu00Mf3kI/maxresdefault.jpg")
//        val thumbnails = Thumbnails(maxres)
//        val snippet = Snippet("[MV] IU(아이유) _ eight(에잇) (Prod.&Feat. SUGA of BTS)", "1theK (원더케이)", "2020-05-06T09:00:16.000Z", thumbnails)
//        val contentDetails = ContentDetails("PT3M42S")
//        val statistics = Statistics("28530023")
//        val item = Item("TgOu00Mf3kI", snippet, contentDetails, statistics)
//
//        hf.items.add(item)
//
//        recyleView_main.adapter?.notifyDataSetChanged()
//        Timer("SettingUp", false).schedule(2000) {
//            isFetching = false
//        }
    }

    fun fetchJSON (countryCode: String) {
        progressBarApi.visibility = ProgressBar.VISIBLE
        val url = "https://www.googleapis.com/youtube/v3/videos?part=statistics,snippet,contentDetails&chart=mostPopular&regionCode=ID&maxResults=1&key=AIzaSyCJuEsWhS1Nbv5TwWfQ7HYkq9Ulepd5z3Q"

        println(url)

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                val gson = GsonBuilder().create()

                val homeFeed = gson.fromJson(body, HomeFeed::class.java)

                val itemLength = homeFeed.items.size - 1

                for(index in 0..itemLength){

                    var maxresUrl : String = ""
                    var standardUrl : String = ""
                    var highUrl : String = ""
                    var mediumUrl : String = ""
                    val defaultUrl = homeFeed.items.get(index).snippet.thumbnails.default.url

                    try {
                        maxresUrl = homeFeed.items.get(index).snippet.thumbnails.maxres.url
                    } catch (e: NullPointerException) {
                        println("Maxres not found")
                    }

                    try {
                        standardUrl = homeFeed.items.get(index).snippet.thumbnails.standard.url
                    } catch (e: NullPointerException) {
                        println("Standard not found")
                    }

                    try {
                        highUrl = homeFeed.items.get(index).snippet.thumbnails.high.url
                    } catch (e: NullPointerException) {
                        println("High not found")
                    }

                    try {
                        mediumUrl = homeFeed.items.get(index).snippet.thumbnails.medium.url
                    } catch (e: NullPointerException) {
                        println("medium not found")
                    }

                    val _maxres = Maxres(maxresUrl)
                    val _standard = Standard(standardUrl)
                    val _high = High(highUrl)
                    val _medium = Medium(mediumUrl)
                    val _default = Default(defaultUrl)

                    val thumbnails = Thumbnails(_default, _medium, _high, _standard, _maxres)
                    val snippet = Snippet(homeFeed.items.get(index).snippet.title, homeFeed.items.get(index).snippet.channelTitle, homeFeed.items.get(index).snippet.publishedAt, thumbnails)
                    val contentDetails = ContentDetails(homeFeed.items.get(index).contentDetails.duration)
                    val statistics = Statistics(homeFeed.items.get(index).statistics.viewCount)
                    val item = Item(homeFeed.items.get(index).id, snippet, contentDetails, statistics)

                    hf.items.add(item)
                }

                if (hf.items.size > 0) {
                    totalResults = homeFeed.pageInfo.totalResults
                    nextPageToken = homeFeed.nextPageToken
                } else {
                    runOnUiThread {
                        textView_notFound.text = "No trending video for country code " + countryCode
                        textView_notFound.visibility = TextView.VISIBLE
                        imageView_notFound.visibility = ImageView.VISIBLE
                    }
                }

                runOnUiThread {
                    recyleView_main.adapter = MainAdapter(hf)
                    progressBarApi.visibility = ProgressBar.GONE
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed to fetch the data")
                progressBarApi.visibility = ProgressBar.GONE
            }
        })
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        textView_notFound.visibility = TextView.GONE
        imageView_notFound.visibility = ImageView.GONE
        val countryName = parent?.selectedItem.toString()

        val bundle = Bundle()
        bundle.putString("country", countryName)
        firebaseAnalytics.logEvent("change_country", bundle)

        if (countryName == "Pick Country") {
            return
        }

        val isoCountryCodes = Locale.getISOCountries()
        for (code in isoCountryCodes) {
            val locale = Locale("en", code)
            if (countryName == locale.displayCountry) {
                hf.items.clear()
                recyleView_main.adapter?.notifyDataSetChanged()
                totalFetched = 10
                fetchJSON(locale.country)
                countryCode = locale.country
            }
        }
    }
}
