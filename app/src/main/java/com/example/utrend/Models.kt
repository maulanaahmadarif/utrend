package com.example.utrend

class HomeFeed(val items: MutableList<Item>, val nextPageToken: String, val pageInfo: PageInfo)

class Item(val id: String, val snippet: Snippet, val contentDetails: ContentDetails, val statistics: Statistics)

class Statistics(val viewCount: String)

class ContentDetails(val duration: String)

class Snippet(val title: String, val channelTitle: String, val publishedAt: String, val thumbnails: Thumbnails)

class Thumbnails(val default: Default, var medium: Medium, var high: High, var standard: Standard, var maxres: Maxres)

class PageInfo(val totalResults: Int)

class Default(val url: String)

class Medium(val url: String = "")

class High(val url: String = "")

class Standard(val url: String = "")

class Maxres(val url: String = "")