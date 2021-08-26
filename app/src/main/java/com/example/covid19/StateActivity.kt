package com.example.covid19

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_state.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eazegraph.lib.models.PieModel

class StateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_state)
            val position = intent.getIntExtra("SID",0)

        fetchResult(position)
    }

    private fun fetchResult(position: Int) {
         GlobalScope.launch(Dispatchers.Main) {
             val response = withContext(Dispatchers.IO){
                 Client.api.clone().execute()
             }
             if (response.isSuccessful){
                 val data = Gson().fromJson<Response>(response.body?.string(),Response::class.java)

                 data.let {
                     toolBar.title = it.statewise[position].state

                     tvActvState.text = it.statewise[position].active
                     tvConfState.text = it.statewise[position].confirmed
                     tvRcvrdState.text = it.statewise[position].recovered
                     tvDcsdState.text = it.statewise[position].deaths

                     pieChart.addPieSlice(
                         PieModel(
                             "Confirmed",
                             Integer.parseInt(tvConfState.text.toString()).toFloat(),
                             Color.parseColor("#D32F2F")
                         )
                     )
                     pieChart.addPieSlice(
                         PieModel(
                             "Active",
                             Integer.parseInt(tvActvState.text.toString()).toFloat(),
                             Color.parseColor("#1976D2")
                         )
                     )
                     pieChart.addPieSlice(
                         PieModel(
                             "Recovered",
                             Integer.parseInt(tvRcvrdState.text.toString()).toFloat(),
                             Color.parseColor("#388E3C")
                         )
                     )
                     pieChart.addPieSlice(
                         PieModel(
                             "Deceased",
                             Integer.parseInt(tvDcsdState.text.toString()).toFloat(),
                             Color.parseColor("#FBC02D")
                         )
                     )
                     pieChart.startAnimation()
                 }
             }
         }
    }
}