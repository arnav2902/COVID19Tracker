package com.example.covid19

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.AbsListView
import androidx.work.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.seconds

class MainActivity : AppCompatActivity() {
    lateinit var stateListAdapter: StateAdapter

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list.addHeaderView(LayoutInflater.from(this).inflate(R.layout.list_header, list, false))

        fetchResults()
        swipeToRefresh.setOnRefreshListener {
            fetchResults()
        }
        initWorker()
        list.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
            override fun onScroll(
                view: AbsListView,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                if (list.getChildAt(0) != null) {
                    swipeToRefresh.isEnabled = list.firstVisiblePosition === 0 && list.getChildAt(
                        0
                    ).getTop() === 0
                }
            }
        })
        list.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this,StateActivity::class.java)
            intent.putExtra("SID",position)
            startActivity(intent)
        }
    }

    private fun fetchResults() {
        GlobalScope.launch {
            val response = withContext(Dispatchers.IO) { Client.api.clone().execute() }
            if (response.isSuccessful) {
                swipeToRefresh.isRefreshing = false
                val data = Gson().fromJson(response.body?.string(), Response::class.java)
                launch(Dispatchers.Main) {
                    bindCombinedData(data.statewise[0])
                    bindStateWiseData(data.statewise.subList(0, data.statewise.size))
                }
            }
        }
    }

    private fun bindStateWiseData(subList: List<StatewiseItem>) {
        stateListAdapter = StateAdapter(subList)
        list.adapter = stateListAdapter
    }

    private fun bindCombinedData(data: StatewiseItem) {
        val lastUpdatedTime = data.lastupdatedtime
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        tvLastUpdatedTime.text =
            "Last Updated\n ${getTimeAgo(simpleDateFormat.parse(lastUpdatedTime))}"

        // To populate the HeaderView
        tvConfirmed.text = data.confirmed
        tvActive.text = data.active
        tvRecovered.text = data.recovered
        tvDeceased.text = data.deaths

    }

    @InternalCoroutinesApi
    private fun initWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val notificationWorkRequest =
            PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "JOB_TAG",
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWorkRequest
        )
    }


}

fun getTimeAgo(past: Date): String {
    val now = Date()
    val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
    val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)

    return when {
        seconds < 60 -> {
            "Few seconds ago"
        }
        minutes < 60 -> {
            "$minutes minutes ago"
        }
        hours < 24 -> {
            "$hours hour ${minutes % 60} min ago"
        }
        else -> {
            SimpleDateFormat("dd/MM/yy, hh:mm a").format(past).toString()
        }
    }}