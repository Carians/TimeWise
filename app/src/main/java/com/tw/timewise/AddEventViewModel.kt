package com.tw.timewise

import androidx.lifecycle.ViewModel

// I hope this will solve the problem of displaying the date and time in the AddEventActivity :)
class AddEventViewModel : ViewModel() {
    var eventName: String? = null
    var startDate: String? = null
    var startTime: String? = null
    var endDate: String? = null
    var endTime: String? = null
}
