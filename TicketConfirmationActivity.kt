package com.example.moviebookingapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class TicketConfirmationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_confirmation)

        // Retrieve data from the intent
        val movieName = intent.getStringExtra("MOVIE_NAME") ?: "No movie selected"
        val date = intent.getStringExtra("DATE") ?: "No date selected"
        val time = intent.getStringExtra("TIME") ?: "No time selected"
        val seats = intent.getStringExtra("SEATS") ?: "No seats selected"
        val totalPrice = intent.getStringExtra("TOTAL_PRICE") ?: "0"

        // Bind data to the TextViews
        val movieTextView: TextView = findViewById(R.id.movieNameTextView)
        val dateTextView: TextView = findViewById(R.id.dateTextView)
        val timeTextView: TextView = findViewById(R.id.timeTextView)
        val seatsTextView: TextView = findViewById(R.id.seatsTextView)
        val totalPriceTextView: TextView = findViewById(R.id.totalPriceTextView)

        movieTextView.text = "Movie: $movieName"
        dateTextView.text = "Date: $date"
        timeTextView.text = "Time: $time"
        seatsTextView.text = "Seats: $seats"
        totalPriceTextView.text = "Total Price: $totalPrice"

        // Set up the "Return" button click listener
        val returnButton: Button = findViewById(R.id.returnButton)
        returnButton.setOnClickListener {
            finish() // Close the current activity and return to MainActivity
        }
    }
}
