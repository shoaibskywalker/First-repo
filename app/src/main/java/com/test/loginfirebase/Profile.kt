package com.test.loginfirebase

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.test.loginfirebase.ImageLinks.FOUR_NO
import com.test.loginfirebase.ImageLinks.KHADHI
import com.test.loginfirebase.ImageLinks.KHADI_CHAIN
import com.test.loginfirebase.ImageLinks.ONE_NO
import com.test.loginfirebase.ImageLinks.SAFARI
import com.test.loginfirebase.ImageLinks.THREE_NO
import com.test.loginfirebase.ImageLinks.TIRANGA
import com.test.loginfirebase.ImageLinks.WROGN
import com.test.loginfirebase.adapter.BagAdapter
import com.test.loginfirebase.data.BagList
import com.test.loginfirebase.databinding.ActivityProfileBinding
import com.test.loginfirebase.utils.sessionManager.UserSessionManager
import de.hdodenhof.circleimageview.CircleImageView

class Profile : AppCompatActivity() {

    private lateinit var imageBack: ImageView
    private lateinit var bgImage: ImageView
    private lateinit var profileImg: CircleImageView
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var editImage: CircleImageView
    private lateinit var prefs: UserSessionManager

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var binding: ActivityProfileBinding

    lateinit var adapter: BagAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageBack.setOnClickListener {
            finish()
        }

        val bagList = listOf(
            BagList("https://firebasestorage.googleapis.com/v0/b/login-and-pass-firebase.appspot.com/o/101.jpg?alt=media&token=f222f972-666b-4c7d-95d7-fde5697f6002", "Tiranga", ""),
            BagList(KHADI_CHAIN, "Khadi Chain", ""),
            BagList(WROGN, "Wrogn", ""),
            BagList(SAFARI, "Safari", ""),
            BagList(KHADHI, "Khadhi", ""),
            BagList(ONE_NO, "Venture 1 Number", ""),
            BagList(THREE_NO, "Venture 3 Number", ""),
            BagList(FOUR_NO, "Venture 4 Number", ""),
        )

        adapter = BagAdapter(this,bagList)
        binding.bagListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bagListRecyclerView.adapter = adapter

       /* Glide.with(this).load(TIRANGA)
            .into(binding.userImage)

        binding.userImage.setOnClickListener {
            binding.showImage.visibility = View.VISIBLE
            binding.showImage.alpha = 0f  // Set the initial alpha to 0 for the fade-in effect

            Glide.with(this)
                .load(TIRANGA)
                .into(binding.showImage)

            // Animate the alpha property to 1 (fully visible) over 500ms
            binding.showImage.animate().alpha(1f).setDuration(300).start()

            // Animate background color change smoothly over 500ms
            binding.parentLayout.animate().setDuration(300).withStartAction {
                binding.parentLayout.setBackgroundColor(Color.parseColor("#18181831"))
            }.start()
        }

        binding.parentLayout.setOnClickListener {
            if (binding.showImage.visibility == View.VISIBLE) {
                // Fade out the image smoothly
                binding.showImage.animate().alpha(0f).setDuration(500).withEndAction {
                    binding.showImage.visibility = View.GONE
                }.start()

                // Animate background color back to white
                binding.parentLayout.animate().setDuration(500).withStartAction {
                    binding.parentLayout.setBackgroundColor(Color.WHITE)
                }.start()
            }
        }*/


    }
}