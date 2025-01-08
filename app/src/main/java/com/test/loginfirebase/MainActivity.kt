package com.test.loginfirebase

import android.app.Application
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.test.loginfirebase.adapter.StoryAdapter
import com.test.loginfirebase.adapter.UserAdapter
import com.test.loginfirebase.broadcastReceiver.BatteryLevelReceiver
import com.test.loginfirebase.data.Message
import com.test.loginfirebase.data.User
import com.test.loginfirebase.data.model.Story
import com.test.loginfirebase.databinding.ActivityMainBinding
import com.test.loginfirebase.utils.CommonUtil
import com.test.loginfirebase.utils.FirebaseStoryWorker
import com.test.loginfirebase.utils.FirebaseUtil
import com.test.loginfirebase.utils.sessionManager.UserSessionManager
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.config.DurationUpdateListener
import com.zegocloud.uikit.prebuilt.call.config.ZegoCallDurationConfig
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import de.hdodenhof.circleimageview.CircleImageView
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import java.util.Date
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    lateinit var mAdapter: UserAdapter
    lateinit var userList: ArrayList<User>
    lateinit var filterList: ArrayList<User>
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseRef: DatabaseReference
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var headerView: View
    private lateinit var headerNameTextView: TextView
    private lateinit var headerEmailTextView: TextView
    private lateinit var headerImageView: CircleImageView
    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var prefs: UserSessionManager
    private lateinit var emailLogin: String
    private lateinit var nameLogin: String
    private lateinit var emailSignUp: String
    private lateinit var progressBar: ProgressBar
    private lateinit var batteryLevelReceiver: BatteryLevelReceiver
    private var currentUserEmail: String? = null
    private lateinit var userDatabaseRef: DatabaseReference
    var senderRoom: String? = null
    var receiverUid: String? = null
    private lateinit var currentUserUid: String
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var userStory: ArrayList<MyStory>
    private var canNavigateToStoryView = true
    private val storyList = mutableListOf<Story>()


    private val unreadStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // When the broadcast is received, refresh the user list
            mAdapter.notifyDataSetChanged() // This will force the UI to refresh
        }
    }

    override fun onRestart() {
        super.onRestart()
        // Refresh user list when returning to main activity
        mAdapter.notifyDataSetChanged()
        Log.d("TAG", "On Restart call")
    }

    override fun onResume() {
        super.onResume()
        // Refresh user list when returning to main activity
        checkUserStories()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = UserSessionManager(this)

        currentUserUid = FirebaseUtil().currentUserId() ?: ""

        batteryLevelReceiver = BatteryLevelReceiver()
        currentUserEmail = prefs.userEmailLogin

        binding.fab.setOnClickListener {
            moveToAiChat()
        }

        binding.addStory.setOnClickListener {
            openImagePicker()

        }
        userStory = ArrayList()
        binding.profileStoryImage.setOnClickListener {
            fetchStories()
        }

        askForNotificationPermission()


        storyAdapter = StoryAdapter(this, storyList)
        binding.recyclerViewStatus.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewStatus.adapter = storyAdapter
        loadActiveStories()


        val application: Application = this.application // Android's application context
        val appID: Long = 165322314 // Replace with your actual app ID
        val appSign = "111d125b753a66a526fbdaea8c55da579fef1eb07669c9967bd31befc86984ee" // Replace with your actual app sign
        val userID: String = FirebaseUtil().currentUserId()!! // Replace with your actual user ID
        val userName: String = prefs.userNameLogin!! // Replace with your actual user name

        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        callInvitationConfig.provider = object : ZegoUIKitPrebuiltCallConfigProvider{
            override fun requireConfig(invitationData: ZegoCallInvitationData?): ZegoUIKitPrebuiltCallConfig {
                val isVideoCall = invitationData!!.type == ZegoInvitationType.VIDEO_CALL.value
                val isGroupCall = invitationData.invitees.size > 1

                val config = when {
                    isVideoCall && isGroupCall -> ZegoUIKitPrebuiltCallConfig.groupVideoCall()
                    !isVideoCall && isGroupCall -> ZegoUIKitPrebuiltCallConfig.groupVoiceCall()
                    !isVideoCall -> ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
                    else -> ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
                }
                config.topMenuBarConfig.isVisible = true
                config.topMenuBarConfig.buttons.add(ZegoMenuBarButtonName.MINIMIZING_BUTTON)

                config.durationConfig = ZegoCallDurationConfig().apply {
                    isVisible = true
                    durationUpdateListener = DurationUpdateListener { seconds ->
                        if (seconds == (60 * 5).toLong()) {
                            ZegoUIKitPrebuiltCallService.endCall()
                        }
                    }
                }
                return config
            }

        }

        callInvitationConfig.outgoingCallBackground = ColorDrawable(ContextCompat.getColor(this, R.color.caller_color))
        callInvitationConfig.incomingCallBackground = ColorDrawable(ContextCompat.getColor(this, R.color.caller_color))



        ZegoUIKitPrebuiltCallService.init(
            application,
            appID,
            appSign,
            userID,
            userName,
            callInvitationConfig
        )

        FirebaseMessaging.getInstance().subscribeToTopic("quotes")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Successfully subscribed to quotes topic.")
                } else {
                    Log.d("FCM", "Failed to subscribe to quotes topic.")
                }
            }
       // scheduleRandomQuoteWorker()


                    // val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid)
        // Set user to "online"
        userDatabaseRef.child("status").setValue("online")
        userDatabaseRef.child("online")
            .setValue(true)

        // Listen for disconnect event to set user as offline

        userDatabaseRef.child("status").onDisconnect().setValue("offline")
        userDatabaseRef.child("online").onDisconnect().setValue(false)

        // Cancel all notifications when the app is opened
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.backgroundTintList = ContextCompat.getColorStateList(this, R.color.normalColor)
        fab.imageTintList = ContextCompat.getColorStateList(this, R.color.white)


        // Register the receiver for battery changes
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryLevelReceiver, filter)

        //signup fetching name and email
        val nameSignUp = intent.getStringExtra("name")
        emailSignUp = intent.getStringExtra("email").toString()
        //login fetching name and email

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

        searchIcon.setOnClickListener {
            toolbarName.visibility = View.GONE
            hamburger.visibility = View.GONE
            searchIcon.visibility = View.GONE
            searchBar.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
        }
        cancelButton.setOnClickListener {
            toolbarName.visibility = View.VISIBLE
            hamburger.visibility = View.VISIBLE
            searchIcon.visibility = View.VISIBLE
            searchBar.visibility = View.GONE
            cancelButton.visibility = View.GONE
            searchBar.text.clear()

        }

        // Register a receiver to listen for broadcast updates
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(unreadStatusReceiver, IntentFilter("update_user_list"))


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


        headerNameTextView.text = "Placeholder"

        emailLogin = prefs.userEmailLogin
        nameLogin = prefs.userNameLogin.toString()
        if (emailLogin.isNotEmpty()) {
            headerEmailTextView.text = emailLogin
        } else {
        }
        if (nameLogin.isNotEmpty()) {
            headerNameTextView.text = nameLogin
        } else {
        }

        if (source == "signup") {
            headerEmailTextView.text = emailSignUp
            headerNameTextView.text = nameSignUp

        }


        toggle = ActionBarDrawerToggle(this, drawlayout, R.string.open, R.string.close)
        drawlayout.addDrawerListener(toggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navview.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.about -> moveToAboutActivity()
                R.id.share -> shareOurApp()
                R.id.rate -> showRatingDialog()
                R.id.logout -> showAlertDialog(
                    title = "Logging Out!",
                    message = "Are you sure you want to logout the app?",
                    negativeButton = "No",
                    positiveButton = "Yes"
                )
                R.id.profile -> moveToProfile2Activity()
                R.id.rate -> {
                    drawlayout.closeDrawer(GravityCompat.START)
                }
                R.id.invite_friend -> requestContactPermission()

            }
            true
        }

        binding.imageBack.setOnClickListener {
            binding.drawerlayout.openDrawer(GravityCompat.START)
        }
//User recycler view
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)

        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference()
        databaseRef = FirebaseDatabase.getInstance().getReference("Users")

        val currentUserId = mAuth.currentUser?.uid
        currentUserId?.let { uid ->
            loadProfileImageFromFirebase(uid)  // This loads the image when you re-enter the activity
        }
        if (isNetworkConnected()) {
            userList = ArrayList()
            filterList = ArrayList()
            mAdapter = UserAdapter(this, filterList)

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = mAdapter

            databaseReference.child("User").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    progressBar.visibility = View.VISIBLE
                    userList.clear()
                    filterList.clear()
                    for (postSnapshot in snapshot.children) {

                        val opponentUser = postSnapshot.getValue(User::class.java)
                        senderRoom = currentUserId + opponentUser?.uid
                        receiverUid = opponentUser?.uid
                        if (mAuth.currentUser?.uid != opponentUser?.uid) {

                            userList.add(opponentUser!!)
                            filterList.add(opponentUser)
                        }

                    }
                    progressBar.visibility = View.GONE
                    mAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

            // Listen for new messages in the chat room
            listenForNewMessages(currentUserId!!)


            getFcmToken()


        } else {
            showAlertDialog(
                title = "No Internet Connection",
                message = "Please check your internet connection and try again.",
                negativeButton = "Close app",
                positiveButton = "Enable Internet"
            )
        }

    }

    private fun loadActiveStories() {
        // loading current active story in recycler view
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                storyList.clear() // Clear the list to avoid duplicates

                for (userSnapshot in snapshot.children) {
                    val uid = userSnapshot.key
                    // Skip the current user's story
                    if (uid == FirebaseUtil().currentUserId()) continue
                    val storySnapshot = userSnapshot.child("story")

                    if (storySnapshot.exists()) {
                        // Check if the user already has a story in the list
                        var isUserAdded = false

                        for (story in storySnapshot.children) {
                            val storyUserId = story.child("userId").value as? String
                            val name = userSnapshot.child("name").value as? String
                            val imageUrl = userSnapshot.child("profileImageUrl").value as? String

                            if (storyUserId != null && name != null && imageUrl != null && !isUserAdded) {
                                prefs.saveReceiverProfilePictureUrl(storyUserId, imageUrl)
                                storyList.add(Story(imageUrl = imageUrl, name = name, userId = storyUserId))
                                isUserAdded = true // Mark user as added to prevent duplicates
                            }
                        }
                    }
                }

                // Notify the adapter after data change
                storyAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Failed to load stories: ${error.message}")
            }
        })
    }


    private fun listenForNewMessages(currentUserId: String) {
        for (user in filterList) {
            val senderRoom = currentUserId + user.uid
            val chatRoomRef = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom)
                .child("messages")

            chatRoomRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    // Update last message in the user adapter when a new message is added
                    val lastMessage = snapshot.getValue(Message::class.java)?.message ?: ""
                    mAdapter.updateLastMessage(user.uid!!)
                }

                override fun onCancelled(error: DatabaseError) {}

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
            })
        }
    }


    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // Save the token to Firebase Realtime Database or Firestore under the user's ID
                Log.d("User token", token)
                // val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                currentUserUid.let { uid ->
                    FirebaseDatabase.getInstance().getReference("Users").child(uid)
                        .child("fcmToken").setValue(token)
                }
            }
        }
    }

    private fun refreshScreen() {

        databaseReference.child("User").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                progressBar.visibility = View.VISIBLE
                userList.clear()
                filterList.clear()
                for (postSnapshot in snapshot.children) {

                    val currentUser = postSnapshot.getValue(User::class.java)
                    if (mAuth.currentUser?.uid != currentUser?.uid) {

                        userList.add(currentUser!!)
                        filterList.add(currentUser)
                    }

                }
                progressBar.visibility = View.GONE
                mAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
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

    private fun showAlertDialog(
        title: String,
        message: String,
        negativeButton: String,
        positiveButton: String
    ) {

        val dialogView = layoutInflater.inflate(R.layout.custom_dialog, null)
        val positiveButtonDialog = dialogView.findViewById<Button>(R.id.positiveButton)
        val negativeButtonDialog = dialogView.findViewById<Button>(R.id.negativeButton)
        positiveButtonDialog.text = positiveButton
        negativeButtonDialog.text = negativeButton

        val dialog = AlertDialog.Builder(this)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        val alertDialog = dialog.create()

        alertDialog.show()

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val dialogSubTitle = dialogView.findViewById<TextView>(R.id.dialogSubTitle)
        dialogTitle.text = title
        dialogSubTitle.text = message


        positiveButtonDialog.setOnClickListener {
            if (positiveButton == "Enable Internet") {
                openDataSettings()
            } else {
                logoutUser()
            }
            alertDialog.dismiss()
        }
        negativeButtonDialog.setOnClickListener {
            if (negativeButton == "Close app") {
                finish()
            } else {
                alertDialog.dismiss()
            }
        }

    }


    private fun openDataSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_DATA_ROAMING_SETTINGS
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun logoutUser() {
        // val uidd = FirebaseAuth.getInstance().currentUser?.uid

        // Set user status to offline in Firebase
        FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid).child("status")
            .setValue("offline")
        FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid).child("online")
            .setValue(false)

        // Delete the FCM token to stop receiving notifications
        FirebaseMessaging.getInstance().deleteToken()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Token Deleted")
                } else {
                }
            }
        mAuth.signOut()
        navigateToLoginScreen()
        //  Toast.makeText(this@MainActivity, "Log out Successfully", Toast.LENGTH_LONG).show()
        CommonUtil.showToastMessage(this, "Log out Successfully")
        ZegoUIKitPrebuiltCallService.unInit()
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
                // Toast.makeText(this, "Please atleast give 0.5 star", Toast.LENGTH_SHORT).show()
                CommonUtil.showToastMessage(this, "Please atleast give 0.5 star")

            } else {
                /* Toast.makeText(this, "Thank you for your rating: $rating", Toast.LENGTH_SHORT)
                     .show()*/
                CommonUtil.showToastMessage(this, "Thank you for your rating: $rating")

                alertDialog.dismiss()
            }
            // Handle the rating, e.g., submit it to a server or store it locally

        }

        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun moveToAiChat() {
        startActivity(Intent(this, AiChat::class.java))
    }

    private fun moveToAboutActivity() {
        startActivity(Intent(this, About::class.java))
    }

    private fun moveToProfile2Activity() {
        startActivity(Intent(this, Profile2::class.java))

    }

    private fun loadProfileImageFromFirebase(uid: String?) {
        uid?.let {
            databaseRef.child(it).child("profileImageUrl")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val imageUrl = snapshot.value as? String
                        if (imageUrl != null && imageUrl.isNotEmpty()) {

                            imageUrl.let {
                                Glide.with(this@MainActivity)
                                    .load(it)
                                    .placeholder(R.drawable.portrait_placeholder)
                                    .error(R.drawable.portrait_placeholder)
                                    .skipMemoryCache(true)  // Ensure it's not loading from cache
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)  // Avoid disk caching
                                    .into(headerImageView)  // Load updated image
                                prefs.currentUserPicture = it
                                Glide.with(this@MainActivity)
                                    .load(it)
                                    .placeholder(R.drawable.portrait_placeholder)
                                    .error(R.drawable.portrait_placeholder)
                                    .skipMemoryCache(true)  // Ensure it's not loading from cache
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)  // Avoid disk caching
                                    .into(binding.profileStoryImage)
                            }
                        } else {
                            headerImageView.setImageResource(R.drawable.portrait_placeholder)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "Main activity image upload",
                            "Failed to listen for image URL changes: ${error.message}"
                        )
                    }
                })
        }
    }

    private fun shareOurApp() {
        val sendText = "Check Out this cool app!"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sendText)
            type = "text/plain"
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

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver when the activity is destroyed
        //  val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid).child("status")
            .setValue("offline")
        FirebaseDatabase.getInstance().getReference("Users").child(currentUserUid).child("online")
            .setValue(false)

        unregisterReceiver(batteryLevelReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(unreadStatusReceiver)

    }

    private val PICK_IMAGE_REQUEST = 1

    // Other methods...

    fun openImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            uploadImageToFirebase(data.data)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading story...") // Set your message
        progressDialog.setCancelable(false) // Prevents dismissal
        progressDialog.show()
        val userId = FirebaseUtil().currentUserId()
        if (userId != null && imageUri != null) {
            val storageReference = FirebaseStorage.getInstance()
                .getReference("Stories/$userId/${System.currentTimeMillis()}")
            storageReference.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    storageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveStoryToDatabase(downloadUrl.toString())
                        progressDialog.dismiss()
                        checkUserStories()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Upload failed: ${e.message}")
                    progressDialog.dismiss()
                }
        }
    }

    private fun saveStoryToDatabase(imageUrl: String) {
        val userId = FirebaseUtil().currentUserId()
        if (userId != null) {
            val databaseReference =
                FirebaseDatabase.getInstance().getReference("Users").child(userId).child("story")
            val storyId = databaseReference.push().key
            val uploadTime = System.currentTimeMillis() // Get current time in milliseconds

            if (storyId != null) {
                /*val storyDetails = mapOf(
                    "imageUrl" to imageUrl,
                    "uploadTime" to uploadTime,
                    "uid" to userId
                )*/
                databaseReference.child(storyId).setValue(
                    Story(
                        imageUrl = imageUrl,
                        name = prefs.userNameLogin!!,
                        timestamp = uploadTime,
                        userId = userId,
                    )
                ).addOnSuccessListener {
                        Log.d("MainActivity", "Story saved successfully.")
                        CommonUtil.showToastMessage(this, "Story uploaded successfully!'")
                    scheduleStoryDeletion(userId, storyId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("MainActivity", "Failed to save story: ${e.message}")
                    }
            }
        }
    }

    private fun scheduleStoryDeletion(userId: String, storyId: String) {
        val workRequest = OneTimeWorkRequestBuilder<FirebaseStoryWorker>()
            .setInitialDelay(4, TimeUnit.HOURS) // Set the delay to 24 hours
            .setInputData(workDataOf("userId" to userId, "storyId" to storyId))
            .build()

        WorkManager.getInstance(this).enqueue(workRequest)
    }


    private fun deleteStory(userId: String, storyId: String) {
        val databaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(userId).child("story")
                .child(storyId)
        databaseReference.removeValue()
            .addOnSuccessListener {
                Log.d("MainActivity", "Story deleted successfully after 10 seconds.")
                CommonUtil.showToastMessage(this, "Story remove successfully!'")
                canNavigateToStoryView = false
                checkUserStories()
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to delete story: ${e.message}")
            }
    }

    private fun fetchStories() {
        // fetching current user story
        val userId = FirebaseUtil().currentUserId()
        if (userId != null) {
            val databaseReference =
                FirebaseDatabase.getInstance().getReference("Users").child(userId).child("story")

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val storiesList = ArrayList<MyStory>() // Change to ArrayList

                    for (storySnapshot in snapshot.children) {
                        val storyData = storySnapshot.getValue(Story::class.java)
                        /* val imageUrl = storyData?.get("imageUrl") as? String
                         val uploadTime = storyData?.get("uploadTime") as? Long*/
                        if (storyData?.imageUrl != null && storyData.timestamp != null) {
                            val dateConvert = Date(storyData.timestamp)
                            storiesList.add(
                                MyStory(
                                    storyData.imageUrl,
                                    dateConvert
                                )
                            ) // Assuming MyStory has a constructor with imageUrl
                        }
                    }

                    // Call a method to display the stories
                    showStories(storiesList)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun showStories(storiesList: ArrayList<MyStory>) {
        if (storiesList.isNotEmpty()) {
            StoryView.Builder(supportFragmentManager)
                .setStoriesList(storiesList) // Required
                .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                .setTitleText(prefs.userNameLogin) // Default is Hidden
                .setSubtitleText("") // Default is Hidden
                .setTitleLogoUrl(prefs.currentUserPicture) // Default is Hidden
                .setStoryClickListeners(object : StoryClickListeners {
                    override fun onDescriptionClickListener(position: Int) {
                        // Handle description click
                    }

                    override fun onTitleIconClickListener(position: Int) {
                        // Handle title icon click
                    }
                }) // Optional Listeners
                .build() // Must be called before calling show method
                .show()
        } else {
        }
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission is already granted
                }
                else -> {
                    // Request notification permission
                    requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android 12 and below, notifications don't require explicit permission
            CommonUtil.showToastMessage(this,"No explicit permission required for notifications on this Android version.")
        }
    }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
            CommonUtil.showToastMessage(this,"Notifications enabled!")
        } else {
            // Permission denied
            CommonUtil.showToastMessage(this,"Please enable notifications for a better experience.")
        }
    }

    private fun checkUserStories() {
        // Adding current story border
        val userId = FirebaseUtil().currentUserId()
        if (userId != null) {
            val databaseReference =
                FirebaseDatabase.getInstance().getReference("Users").child(userId).child("story")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if the user has stories
                    if (snapshot.exists() && snapshot.childrenCount > 0) {
                        // User has stories, make the activeStory view visible
                        findViewById<View>(R.id.activeStory).visibility = View.VISIBLE
                    } else {
                        // User has no stories, hide the activeStory view
                        findViewById<View>(R.id.activeStory).visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun requestContactPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                REQUEST_CODE_READ_CONTACTS
            )
        } else {
            fetchContacts()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchContacts()
            } else {
                CommonUtil.showToastMessage(this,"Permission denied to read contacts")
            }
        }
    }

    private fun fetchContacts() {
        val contacts = mutableListOf<String>()
        val contentResolver = contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val name = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                )
                val phoneNumber = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
                contacts.add("$name: $phoneNumber")
            }
            cursor.close()
        }

        // Display contacts in a dialog or RecyclerView
        showContactsDialog(contacts)
    }

    private fun showContactsDialog(contacts: List<String>) {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.contact_list_dialog, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchView)
        val listView = dialogView.findViewById<ListView>(R.id.listView)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel)
        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)


        // Set up the adapter for the ListView
        val adapter = ArrayAdapter(this, R.layout.custom_list_item, contacts)
        listView.adapter = adapter

        // Filter the list based on the search query
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // We handle the filtering in real time
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
        cancelButton.setOnClickListener {
        }
        title.text = "Invite Friends"

        // Show a dialog with the custom layout
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)

        val alertDialog = dialogBuilder.create()
        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        // Handle contact selection
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedContact = adapter.getItem(position)
            alertDialog.dismiss()
            // Handle selected contact (e.g., send invite via SMS)
            sendInvite(selectedContact?.split(":")?.get(1)?.trim() ?: "")
        }

        alertDialog.show()
    }

    private fun sendInvite(phoneNumber: String) {
        // Create an SMS intent with the phone number and an optional message
        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("smsto:$phoneNumber") // Define the recipient's phone number
            putExtra("sms_body", "Let's chat on WhatsApp! It's a fast, simple, and secure app we can use to message and call each other for free. Get it at\nhttps://www.amazon.com/dp/B0DRS9SVNL/ref=apps_sf_sta") // Optional message
        }

        // Start the SMS app to send the message
        startActivity(smsIntent)
    }




    companion object {
        const val REQUEST_CODE_READ_CONTACTS = 100
    }

}
