package com.example.myapplication

import android.R
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.myapplication.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Locale


@SuppressLint("StaticFieldLeak")
lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity(), NewsItemClicked, ConnectivityInternet.ReceiverListener {

    private lateinit var mAdapter : NewsListAdapter
    private val itemsDes : ArrayList<News> = ArrayList()
    private val itemsAsc : ArrayList<News> = ArrayList()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.spinnerLl.background = getDrawable(com.example.myapplication.R.drawable.dropdown_bg)
        // Spinner Drop down elements
        val categories: MutableList<String> = ArrayList()
        categories.add("Latest to Old")
        categories.add("Old to Latest")

        // Creating adapter for spinner
        val dataAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        // attaching data adapter to spinner
        binding.spinner.adapter = dataAdapter

        checkConnection()
    }

    fun fetchData() {
        val url = "https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val newsJsonArray = response.getJSONArray("articles")
                val newsArray = ArrayList<News>()
                for(i in 0 until newsJsonArray.length()){
                    val newsJsonObject = newsJsonArray.getJSONObject(i)
                    val news = News(
                        newsJsonObject.getString("title"),
                        newsJsonObject.getString("description"),
                        newsJsonObject.getString("url"),
                        newsJsonObject.getString("urlToImage"),
                        newsJsonObject.getString("publishedAt")
                    )
                    newsArray.add(news)
                }
                // Sort the list of news articles by published time in descending order
                val sortedNewsDes = newsArray.sortedByDescending { article ->
                    // Parse the published time as a Date object and return it
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(article.date)
                }

                // Sort the list of news articles by published time in Ascending order
                val sortedNewsAes = newsArray.sortedBy { article ->
                    // Parse the published time as a Date object and return it
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(article.date)
                }

                if(itemsAsc.isNotEmpty() || itemsDes.isNotEmpty()){
                    itemsAsc.clear()
                    itemsDes.clear()
                }

                itemsDes.addAll(sortedNewsDes)
                itemsAsc.addAll(sortedNewsAes)

                mAdapter.updateNews(sortedNewsDes)
            },
            { error ->
                // TODO: Handle error
            }
        )

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    override fun onItemClicked(item: News) {
        val intent: CustomTabsIntent = CustomTabsIntent.Builder()
            .build()
        intent.launchUrl(this@MainActivity, Uri.parse(item.url))
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun checkConnection() {

        // initialize intent filter
        val intentFilter = IntentFilter()

        // add action
        intentFilter.addAction("android.new.conn.CONNECTIVITY_CHANGE")

        // register receiver
        registerReceiver(ConnectivityInternet(), intentFilter)

        // Initialize listener
        ConnectivityInternet.Listener = this

        // Initialize connectivity manager
        val manager =
            applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        // Initialize network info
        val networkInfo = manager.activeNetworkInfo

        // get connection status
        val isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting

        // display snack bar
        showSnackBar(isConnected)
    }

    private fun showSnackBar(isConnected: Boolean) {

        // initialize color and message
        val message: String
        val color: Int

        // check condition
        if (isConnected) {

            // when internet is connected
            // set message
            showData()


        } else {

            // when internet
            // is disconnected
            val dialog = Dialog(this@MainActivity)
            dialog.setContentView(com.example.myapplication.R.layout.item_error);
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(false);
            val btn : Button = dialog.findViewById(com.example.myapplication.R.id.retryButton);
            btn.setOnClickListener {
                checkConnection()
                dialog.dismiss()
            }

            binding.progressBar.visibility = View.GONE
            binding.progressText.visibility = View.GONE
            dialog.show()
        }
    }

    override fun onNetworkChange(isConnected: Boolean) {
        TODO("Not yet implemented")
    }

    fun showData(){
        binding.progressBar.visibility = View.VISIBLE
        binding.progressText.visibility = View.VISIBLE
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        fetchData()
        mAdapter = NewsListAdapter(this)
        binding.recyclerView.adapter = mAdapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if(position == 0){
                    mAdapter.updateNews(itemsDes)
                }else if(position == 1){
                    mAdapter.updateNews(itemsAsc)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

}