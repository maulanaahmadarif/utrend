package com.example.utrend

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.video_row.view.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*


class MainAdapter(val homeFeed: HomeFeed): androidx.recyclerview.widget.RecyclerView.Adapter<CustomViewHolder>() {

    override fun getItemCount(): Int {
        return homeFeed.items.count()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(p0.context)
        val cellForRow = layoutInflater.inflate(R.layout.video_row, p0, false)
        return CustomViewHolder(cellForRow)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(p0: CustomViewHolder, p1: Int) {
        val item = homeFeed.items.get(p1)
        p0.view.videoTitle.text = item.snippet.title
        p0.view.videoDetail.text = item.snippet.channelTitle + " · " + getViewsNumber(item.statistics.viewCount.toDouble()) + " views · " + getPublishDate(item.snippet.publishedAt)
        p0.view.textView_duration.text = formatDuration(item.contentDetails.duration)

        val videoThumbnail = p0.view.imageView

        var imageUrl : String = ""

        if (item.snippet.thumbnails.maxres.url.isNotEmpty()) {
            imageUrl = item.snippet.thumbnails.maxres.url
        } else if (item.snippet.thumbnails.standard.url.isNotEmpty()) {
            imageUrl = item.snippet.thumbnails.standard.url
        } else if (item.snippet.thumbnails.high.url.isNotEmpty()) {
            imageUrl = item.snippet.thumbnails.high.url
        } else if (item.snippet.thumbnails.medium.url.isNotEmpty()) {
            imageUrl = item.snippet.thumbnails.medium.url
        } else {
            imageUrl = item.snippet.thumbnails.default.url
        }

        Picasso.get().load(imageUrl).fit().centerCrop().into(videoThumbnail)

        p0.item = item
    }

    private fun getViewsNumber (num: Double): String {
        if (num < 1000) return "" + num
        val exp = (Math.log(num) / Math.log(1000.0)).toInt()
        return java.lang.String.format(
            "%.1f%c",
            num / Math.pow(1000.0, exp.toDouble()),
            "kMGTPE"[exp - 1]
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPublishDate (date: String): String {
        val currentDate = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneOffset.ofHours(7)).format(
            Instant.now())
        val date1: Date
        val date2: Date
        val publishDate = date.substring(0, 19)
        val dates = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        date1 = dates.parse(currentDate)
        date2 = dates.parse(publishDate)
        var difference = Math.abs(date1.time - date2.time)
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val weeksInMilli = daysInMilli * 7
        val monthsInMilli = weeksInMilli * 4
        val yearsInMilli = monthsInMilli * 12

        val elapsedYears: Long = difference / yearsInMilli
        difference = difference % yearsInMilli

        val elapsedMonths: Long = difference / monthsInMilli
        difference = difference % monthsInMilli

        val elapsedWeeks: Long = difference / weeksInMilli
        difference = difference % weeksInMilli

        val elapsedDays: Long = difference / daysInMilli
        difference = difference % daysInMilli

        val elapsedHours: Long = difference / hoursInMilli
        difference = difference % hoursInMilli

        val elapsedMinutes: Long = difference / minutesInMilli
        difference = difference % minutesInMilli

        val elapsedSeconds: Long = difference / secondsInMilli

        var timeDifference = ""
        if (elapsedYears > 0) {
            timeDifference = elapsedYears.toString() + " year" + isMoreThanOne(elapsedYears.toInt()) + " ago"
        } else if (elapsedMonths > 0) {
            timeDifference = elapsedMonths.toString() + " month" + isMoreThanOne(elapsedMonths.toInt()) + " ago"
        } else if (elapsedWeeks > 0) {
            timeDifference = elapsedWeeks.toString() + " week" + isMoreThanOne(elapsedWeeks.toInt()) + " ago"
        } else if (elapsedDays > 0) {
            timeDifference = elapsedDays.toString() + " day" + isMoreThanOne(elapsedDays.toInt()) + " ago"
        } else if (elapsedHours > 0) {
            timeDifference = elapsedHours.toString() + " hour" + isMoreThanOne(elapsedHours.toInt()) + " ago"
        } else if (elapsedMinutes > 0) {
            timeDifference = elapsedMinutes.toString() + " minute" + isMoreThanOne(elapsedMinutes.toInt()) + " ago"
        } else {
            timeDifference = elapsedSeconds.toString() + " second" + isMoreThanOne(elapsedSeconds.toInt()) + " ago"
        }
        return timeDifference
    }

    private fun isMoreThanOne (num: Int): String {
        return if (num > 1) "s" else ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDuration (duration: String): String {
        val duration = Duration.parse(duration)
        var formatDuration = ""
        if (duration.toDays() > 0) {
            if (duration.toDays().toString().length == 1) {
                formatDuration = "0" + duration.toDays() + ":"
            } else {
                formatDuration = duration.toDays().toString() + ":"
            }
        } else {
            formatDuration = ""
        }

        if (duration.toHours() > 0) {
            if (duration.toHours().toString().length == 1) {
                formatDuration = formatDuration + "0" + duration.toHours() + ":"
            } else {
                formatDuration = formatDuration + duration.toHours().toString() + ":"
            }
        } else {
            formatDuration = ""
        }

        if (duration.toMinutes() > 0) {
            if (duration.toMinutes().toString().length == 1) {
                formatDuration = formatDuration + "0" + duration.toMinutes() + ":"
            } else {
                formatDuration = formatDuration + duration.toMinutes().toString() + ":"
            }
        } else {
            formatDuration = ""
        }

        if (duration.seconds < 60) {
            if ((duration.seconds % 60).toString().length == 1) {
                formatDuration = "00:0" + duration.seconds % 60
            } else {
                formatDuration = "00:" + duration.seconds % 60
            }
        } else {
            if ((duration.seconds % 60).toString().length == 1) {
                formatDuration = formatDuration + "0" + duration.seconds % 60
            } else {
                formatDuration = formatDuration + duration.seconds % 60
            }
        }

        return formatDuration
    }
}

class CustomViewHolder(val view: View, var item: Item? = null): androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

    companion object {
        val VIDEO_TITLE = "VIDEO_TITLE"
        val PAGE_URL = "PAGE_URL "
    }

    init {
        view.setOnClickListener {
            val intent = Intent(view.context, WebActivity::class.java)
            intent.putExtra(PAGE_URL, "https://www.youtube.com/watch?v=" + item?.id)
            intent.putExtra(VIDEO_TITLE, item?.snippet?.channelTitle)

            view.context.startActivity(intent)
        }
    }
}