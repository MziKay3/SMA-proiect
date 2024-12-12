package com.example.moviebookingapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.view.Gravity
import android.widget.GridLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ToggleButton
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.widget.AdapterView
import android.widget.ArrayAdapter

class MainActivity : ComponentActivity() {

    private val selectedSeats = mutableListOf<String>()
    private val basePrice = 10
    private val premiumPrice = 15
    private lateinit var totalPriceTextView: TextView
    private lateinit var movieSpinner: Spinner
    private lateinit var dateSpinner: Spinner
    private lateinit var timeRadioGroup: RadioGroup
    private lateinit var bookTicketButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        movieSpinner = findViewById(R.id.movieSpinner)
        dateSpinner = findViewById(R.id.dateSpinner)
        timeRadioGroup = findViewById(R.id.timeRadioGroup)
        val seatGrid: GridLayout = findViewById(R.id.seatGrid)
        totalPriceTextView = findViewById(R.id.totalPriceTextView)
        bookTicketButton = findViewById(R.id.bookTicketButton)
        val deselectAllButton: Button = findViewById(R.id.deselectAllButton)

        val movieTitles = arrayOf("-", "Inception", "Interstellar", "The Dark Knight")
        val movieDates = mapOf(
            "Inception" to arrayOf("12th November", "15th November", "22nd November"),
            "Interstellar" to arrayOf("14th November", "21st November", "25th November"),
            "The Dark Knight" to arrayOf("10th November", "17th November", "29th November")
        )
        val dateTimes = mapOf(
            "12th November" to arrayOf("16:30", "19:00"),
            "15th November" to arrayOf("14:00", "18:30"),
            "22nd November" to arrayOf("13:00", "17:30"),
            "14th November" to arrayOf("17:00", "18:45"),
            "21th November" to arrayOf("13:50", "20:10"),
            "25th November" to arrayOf("12:15", "18:15"),
            "10th November" to arrayOf("15:10", "19:45"),
            "17th November" to arrayOf("14:25", "21:00"),
            "29th November" to arrayOf("16:40", "18:50")
        )

        val movieAdapter = createWhiteTextAdapter(movieTitles)
        movieSpinner.adapter = movieAdapter

        movieSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedMovie = parent.getItemAtPosition(position).toString()
                val datesForMovie = movieDates[selectedMovie] ?: arrayOf("-")
                val dateAdapter = createWhiteTextAdapter(datesForMovie)
                dateSpinner.adapter = dateAdapter
                dateSpinner.setSelection(0)
                setTimeOptionsPlaceholder()
                checkBookingAvailability()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedDate = parent.getItemAtPosition(position).toString()
                val timesForDate = dateTimes[selectedDate] ?: emptyArray()
                if (timesForDate.isNotEmpty()) {
                    updateTimeOptions(timesForDate)
                } else {
                    setTimeOptionsPlaceholder()
                }
                checkBookingAvailability()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        timeRadioGroup.setOnCheckedChangeListener { _, _ ->
            checkBookingAvailability()
        }

        setupSeatSelectionGrid(seatGrid)

        bookTicketButton.setOnClickListener {
            confirmBooking()
        }

        deselectAllButton.setOnClickListener {
            clearSelections()
        }

        checkBookingAvailability()
    }

    private fun createWhiteTextAdapter(items: Array<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.WHITE)
                return view
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(Color.WHITE)
                return view
            }
        }.apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setTimeOptionsPlaceholder() {
        timeRadioGroup.removeAllViews()
        val placeholderRadioButton = RadioButton(this).apply {
            text = "-"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        timeRadioGroup.addView(placeholderRadioButton)
    }

    private fun updateTimeOptions(times: Array<String>) {
        timeRadioGroup.removeAllViews()
        times.forEach { time ->
            val radioButton = RadioButton(this).apply {
                text = time
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
            }
            timeRadioGroup.addView(radioButton)
        }
    }

    private fun setupSeatSelectionGrid(seatGrid: GridLayout) {
        val rows = 'A'..'H'
        val columns = 1..10

        for (row in rows) {
            for (column in columns) {
                val seatButton = ToggleButton(this).apply {
                    text = "$row$column"
                    textOn = "$row$column"
                    textOff = "$row$column"
                    gravity = Gravity.CENTER
                    setPadding(8, 8, 8, 8)
                }

                seatButton.setOnCheckedChangeListener { _, isChecked ->
                    val seatId = "$row$column"
                    if (isChecked) {
                        selectedSeats.add(seatId)
                    } else {
                        selectedSeats.remove(seatId)
                    }
                    updateTotalPrice()
                    checkBookingAvailability()  // Check if booking can be enabled
                }

                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                seatButton.layoutParams = params
                seatGrid.addView(seatButton)
            }
        }
    }

    private fun checkBookingAvailability() {
        val movieSelected = movieSpinner.selectedItem != "-"
        val dateSelected = dateSpinner.selectedItem != "-"
        val timeSelected = timeRadioGroup.checkedRadioButtonId != -1 &&
                (timeRadioGroup.findViewById<RadioButton>(timeRadioGroup.checkedRadioButtonId)?.text != "-")
        val seatsSelected = selectedSeats.isNotEmpty()

        bookTicketButton.isEnabled = movieSelected && dateSelected && timeSelected && seatsSelected
    }

    private fun updateTotalPrice() {
        var totalPrice = 0
        for (seat in selectedSeats) {
            val isPremiumRow = seat.startsWith("F") || seat.startsWith("G") || seat.startsWith("H")
            totalPrice += if (isPremiumRow) premiumPrice else basePrice
        }
        totalPriceTextView.text = "Total Price: $totalPrice"
    }

    private fun confirmBooking() {
        val selectedMovie = movieSpinner.selectedItem?.toString() ?: "No movie selected"
        val selectedDate = dateSpinner.selectedItem?.toString() ?: "No date selected"
        val selectedTime = (timeRadioGroup.findViewById<RadioButton>(timeRadioGroup.checkedRadioButtonId)?.text.toString()
            ?: "No time selected")
        val seatList = if (selectedSeats.isNotEmpty()) selectedSeats.joinToString(", ") else "No seats selected"

        // Extract only the numeric part of the total price
        val totalPriceText = totalPriceTextView.text.toString().replace("Total Price: ", "")

        val intent = Intent(this, TicketConfirmationActivity::class.java).apply {
            putExtra("MOVIE_NAME", selectedMovie)
            putExtra("DATE", selectedDate)
            putExtra("TIME", selectedTime)
            putExtra("SEATS", seatList)
            putExtra("TOTAL_PRICE", totalPriceText)  // Pass only the numeric value
        }
        startActivity(intent)
    }

    private fun clearSelections() {
        selectedSeats.clear()
        updateTotalPrice()
        movieSpinner.setSelection(0)
        dateSpinner.setSelection(0)
        setTimeOptionsPlaceholder()

        for (i in 0 until timeRadioGroup.childCount) {
            (timeRadioGroup.getChildAt(i) as RadioButton).isChecked = false
        }

        checkBookingAvailability()
    }
}
