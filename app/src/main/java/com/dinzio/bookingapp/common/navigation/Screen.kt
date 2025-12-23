package com.dinzio.bookingapp.common.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object BookingList : Screen("booking_list")
}
