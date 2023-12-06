package com.htnguyen.weatherapp.adater

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.htnguyen.weatherapp.R
import com.htnguyen.weatherapp.model.WeatherDaily
import com.squareup.picasso.Picasso

class WeatherDailyAdapter(weatherDailyArrayList: ArrayList<WeatherDaily>, context: Context) :
    RecyclerView.Adapter<WeatherDailyAdapter.ViewHolder>() {
    // creating a variable for array list and context.
    private val weatherDailyArrayList: ArrayList<WeatherDaily>
    private val context: Context

    // creating a constructor for our variables.
    init {
        this.weatherDailyArrayList = weatherDailyArrayList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // below line is to inflate our layout.
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_weather_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // setting data to our views of recycler view.
        val modal: WeatherDaily = weatherDailyArrayList[position]
        holder.txtTime.text = modal.dt.toString()
        holder.txtTemperatureA.text = String.format("%4.2f", modal.tempMin - 272.15)
        holder.txtTemperatureB.text = String.format("%4.2f", modal.temMax  - 272.15)
        holder.txtTemperatureC.text = modal.humidity.toString()
        Picasso.get().load("https://openweathermap.org/img/wn/${modal.weatherIcon}.png").into(holder.imgWeatherA)
        Picasso.get().load("https://openweathermap.org/img/wn/${modal.weatherIcon}.png").into(holder.imgWeatherB)
    }

    override fun getItemCount(): Int {
        return weatherDailyArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTime: TextView
        val txtTemperatureA: TextView
        val txtTemperatureB: TextView
        val txtTemperatureC: TextView
        val imgWeatherA: ImageView
        val imgWeatherB: ImageView


        init {
            txtTime = itemView.findViewById(R.id.txtTime)
            txtTemperatureA = itemView.findViewById(R.id.txtTemperatureA)
            txtTemperatureB = itemView.findViewById(R.id.txtTemperatureB)
            txtTemperatureC = itemView.findViewById(R.id.txtTemperatureC)
            imgWeatherA = itemView.findViewById(R.id.imgWeatherA)
            imgWeatherB = itemView.findViewById(R.id.imgWeatherB)
        }
    }
}