package com.majid_fit5.pokemonandroid

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : FragmentActivity(), OnMapReadyCallback {
    var myLocation:Location?=null
    var listOfPokemons= arrayListOf<Pockemon>()
    var myPower=0.0

    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkUserPermission()
        loadPockemon()
    }

    var ACCESSLOCATION=123 // request code. it can be changed any time.
    private fun checkUserPermission() {
        if(Build.VERSION.SDK_INT>23){
            if(ActivityCompat.
                    checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),ACCESSLOCATION)
                return
            }
        }
        getUserLocation()
    }
    /**
     * This function automatically fired when checkSelfPermission() is invoked.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            ACCESSLOCATION -> {
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){// grantResults[0] because there is 1 permission.
                    getUserLocation()
                }else{
                    Toast.makeText(this,"We can not access the myLocation.",Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getUserLocation() {
        Toast.makeText(this,"Accessing to the myLocation now..",Toast.LENGTH_SHORT).show()

        val myLocationListener=MyLocationListener()
        val myLocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager

        myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3,3f,myLocationListener) // will call onLocationChanged()
        val thread=MyThread()
        thread.start()

    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) { // automatically invoked when map starts.
        mMap = googleMap

     /*   // Add a marker in Sydney and move the camera
        val sydney = LatLng(37.422, -122.084)
      //  val sydney = LatLng(55.0,-122.401846647263 )
        mMap!!.addMarker(MarkerOptions()
                .position(sydney)
                .title("Me")
                .snippet(" here is my myLocation")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mario)))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14f)) //14f is for zooming.*/
    }

    // Get the user myLocation


    inner class MyLocationListener:LocationListener{

        constructor():super(){
            myLocation =Location("new")
            myLocation!!.longitude-0.0
            myLocation!!.latitude=0.0
        }
        override fun onLocationChanged(p0: Location?) {
            myLocation =p0
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onProviderEnabled(p0: String?) {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onProviderDisabled(p0: String?) {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    inner class MyThread: Thread { // to update the myLocation every time in the background.
        var myOldLocation:Location?=null
        constructor():super(){
            myOldLocation = Location("Start")
            myOldLocation!!.longitude=0.0
            myOldLocation!!.longitude=0.0
        }

        override fun run() {

            while(true){
                try{
                    if(myOldLocation!!.distanceTo(myLocation)==0f){ // there is no changing in the myLocation, do not call the next function.
                        continue
                    }
                    myOldLocation = myLocation

                    runOnUiThread{
                        mMap!!.clear() // this is so important , otherwise you will see the previous locations every time..
                        showMyLocation()
                        showPokemonsLocation()
                    }
                    Thread.sleep(1000)

                }catch (ex:Exception){}
            }
        }


        private fun showPokemonsLocation() {
            var noPokemons=true;
            listOfPokemons.forEach{
                if(!it.IsCatch!!){
                    val pockemonLoc = LatLng(it.location!!.latitude, it.location!!.longitude)
                    mMap!!.addMarker(MarkerOptions()
                            .position(pockemonLoc)
                            .title(it.name!!)
                            .snippet(it.description!! +", his power:"+ it!!.power)
                            .icon(BitmapDescriptorFactory.fromResource(it.image!!)))

                    // catch pokemon
                    if(myLocation!!.distanceTo(it.location)<3){ // if my location is less than 2 meters of the pokemon location.
                        it.IsCatch=true
                        myPower+=it.power!!
                        Toast.makeText(applicationContext,
                                "You catch new pokemon, your new power is $myPower",
                                Toast.LENGTH_LONG).show()
                    }
                    noPokemons=false // means still pokemons in map
                }/*else if(noPokemons){
                    Toast.makeText(applicationContext,
                            "Congratulations!!, you caught all the pokemons today!",
                            Toast.LENGTH_LONG).show()
                }*/
            }
        }

        private fun showMyLocation() {
            val sydney = LatLng(myLocation!!.latitude, myLocation!!.longitude)
            mMap!!.addMarker(MarkerOptions()
                    .position(sydney)
                    .title("Me")
                    .snippet(" here is my location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mario)))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14f))
        }
    }



    fun  loadPockemon(){
        listOfPokemons.add(Pockemon(R.drawable.sarpadian,
                "Sarpadian", "Sarpadian is in San Fransisco", 55.0, 37.7789994893035, -122.401846647263))

        listOfPokemons.add(Pockemon(R.drawable.nidoran,
                "Bulbasaur", "Bulbasaur living in usa", 90.5, 37.7949568502667, -122.410494089127))

        listOfPokemons.add( Pockemon(R.drawable.pangolin,
                "Pangolin", "Squirtle living in iraq", 33.5, 37.7816621152613, -122.41225361824))
    }

}
