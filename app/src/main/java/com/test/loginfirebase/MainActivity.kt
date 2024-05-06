package com.test.loginfirebase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.test.loginfirebase.adapter.UserAdapter
import com.test.loginfirebase.data.User
import com.test.loginfirebase.databinding.ActivityMainBinding
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    lateinit var mAdapter: UserAdapter
    lateinit var userList: ArrayList<User>
    lateinit var filterList: ArrayList<User>
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserRef:DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var headerView: View
    private lateinit var headerNameTextView: TextView
    private lateinit var headerEmailTextView: TextView
    private lateinit var headerImageView: CircleImageView
    private lateinit var refresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//Online Status
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users Online").child(FirebaseAuth.getInstance().currentUser?.uid!!)
        currentUserRef.child("online").setValue(true) // Set online status to true when the app is opened
        currentUserRef.child("online").onDisconnect().setValue(false)
        createNotificationChannel()
        //signup fetching name and email
        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        //login fetching name and email
        val  mail = intent.getStringExtra("mail")
        val nameLog = intent.getStringExtra("name")
        val source = intent.getStringExtra("source")

        val toolbarName = findViewById<TextView>(R.id.toolbarNme)
        val hamburger = findViewById<ImageView>(R.id.imageBack)
        val searchIcon = findViewById<ImageView>(R.id.search_icon)
        val searchBar = findViewById<EditText>(R.id.searchField)
        val cancelButton = findViewById<ImageView>(R.id.cancelbuttonsearch)
         refresh = findViewById(R.id.swipe_refresh_layout)

        refresh.setOnRefreshListener {
            refreshScreen()
        }

        searchIcon.setOnClickListener{
            toolbarName.visibility =  View.GONE
            hamburger.visibility = View.GONE
            searchIcon.visibility = View.GONE
            searchBar.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
        }
        cancelButton.setOnClickListener{
            toolbarName.visibility =  View.VISIBLE
            hamburger.visibility = View.VISIBLE
            searchIcon.visibility = View.VISIBLE
            searchBar.visibility = View.GONE
            cancelButton.visibility = View.GONE
            searchBar.text.clear()

        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })


//side menu
        val drawlayout = findViewById<DrawerLayout>(R.id.drawerlayout)
        val navview = findViewById<NavigationView>(R.id.navigationv)

        headerView = navview.getHeaderView(0)
        headerNameTextView = headerView.findViewById(R.id.shoaib)
        headerEmailTextView = headerView.findViewById(R.id.email)
        headerImageView = headerView.findViewById(R.id.imageView)
        headerNameTextView.text = "Shoaib"

        if (source == "login") {
            headerEmailTextView.text = mail

        } else if (source == "signup") {
            headerEmailTextView.text = email

        }


        toggle = ActionBarDrawerToggle(this, drawlayout,R.string.open, R.string.close)
        drawlayout.addDrawerListener(toggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navview.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.profile -> {moveToProfileActivity()
                }
                R.id.about -> moveToAboutActivity()
                R.id.help -> moveToHelpActivity()
                R.id.share -> shareOurApp()
                R.id.rate -> showRatingDialog()
                R.id.logout -> showDialogForLogOut()


                R.id.rate -> {
                    drawlayout.closeDrawer(GravityCompat.START)
                }
            }
            true
        }

        binding.imageBack.setOnClickListener{
            binding.drawerlayout.openDrawer(GravityCompat.START)
        }
//User recycler view
        recyclerView = findViewById(R.id.recyclerView)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)

        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference()
        if (isNetworkConnected()) {
            userList = ArrayList()
            filterList = ArrayList()
            mAdapter = UserAdapter(this, userList,filterList)

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = mAdapter

            databaseReference.child("User").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    progressBar.visibility = View.VISIBLE
                    userList.clear()
                    filterList.clear()
                    for (postSnapshot in snapshot.children) {

                        val currentUser = postSnapshot.getValue(User::class.java)
                        if (mAuth.currentUser?.uid != currentUser?.uid) {

                            userList.add(currentUser!!)
                            filterList.add(currentUser!!)
                        }

                    }
                    progressBar.visibility = View.GONE
                    mAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        } else {
            showDialog()
        }

    }

    private fun refreshScreen() {
        mAdapter.notifyDataSetChanged()
        refresh.isRefreshing = false
    }

    private fun navigateToLoginScreen() {
        startActivity(Intent(this, Login::class.java))
        finish() // Finish this activity to prevent going back to the home screen
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("No Internet Connection")
        builder.setMessage("Please check your internet connection and try again.")

        builder.setNegativeButton("Close app") { dialog, which ->
            // Handle the click event, e.g., close the app
            finish()
        }
        builder.setPositiveButton("Enable Internet") { dialog, which ->
            openDataSettings()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun showDialogForLogOut() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logging Out!")
        builder.setMessage("Are you sure you want to logout the app?")

        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.setPositiveButton("Yes") { dialog, which ->
            logoutUser()
        }
        builder.setCancelable(true)
        builder.show()
    }

    private fun openDataSettings() {
        val intent = Intent().apply {
            action = android.provider.Settings.ACTION_WIRELESS_SETTINGS
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun logoutUser() {
        mAuth.signOut()
        navigateToLoginScreen()
        Toast.makeText(this@MainActivity, "Log out Successfully", Toast.LENGTH_LONG).show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ELA"
            val descriptionText = "Some message"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("1234", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(receiverId: String,message: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("uid",receiverId)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        val builder = NotificationCompat.Builder(this, "1234")
            .setSmallIcon(R.drawable.set_targets_icon)
            .setContentTitle("ELA Chat")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(1, builder.build())
        }
    }

    private fun showRatingDialog() {
        val dialogView = layoutInflater.inflate(R.layout.rating_bar, null)
        val ratingBar: RatingBar = dialogView.findViewById(R.id.ratingBar)
        val text: TextView = dialogView.findViewById(R.id.text2)
        val buttonSubmit: Button = dialogView.findViewById(R.id.submit_button)
        val buttonCancel: Button = dialogView.findViewById(R.id.cancel_button)

        ratingBar.setOnRatingBarChangeListener { rBar, fl, b ->
            text.text = fl.toString()
            when (rBar.rating.toInt()) {
                1 -> text.text = "Very Bad"
                2 -> text.text = "Bad"
                3 -> text.text = "Nice"
                4 -> text.text = "Good"
                5 -> text.text = "Awesome"
                6 -> text.text = "Excellent"
                else -> text.text = "    "
            }
        }

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Rate Our App")
            .setMessage("Please rate our app and help us to improve.")
            .setCancelable(false)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        buttonSubmit.setOnClickListener {
            val rating = ratingBar.rating
            if (rating.toInt() == 0) {
                Toast.makeText(this, "Please atleast give 0.5 star", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Thank you for your rating: $rating", Toast.LENGTH_SHORT)
                    .show()
                alertDialog.dismiss()
            }
            // Handle the rating, e.g., submit it to a server or store it locally

        }

        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun moveToAboutActivity() {
        startActivity(Intent(this, About::class.java))
    }

    private fun moveToHelpActivity() {
        startActivity(Intent(this, Help::class.java))
    }

    private fun moveToProfileActivity() {
        startActivity(Intent(this, Profile::class.java))
    }

    private fun shareOurApp(){
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, "content://com.test.loginfirebase/")
            type = "application/vnd.android.package-archive"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
    private fun filterUsers(query: String) {
        val filteredUsers = userList.filter { user ->
            user.name!!.contains(query, ignoreCase = true)
        }
        mAdapter.filterList(ArrayList(filteredUsers))
    }
}
