package com.test.loginfirebase

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class About : AppCompatActivity() {

    private lateinit var imageBack : ImageView
    private lateinit var about_Text : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        imageBack = findViewById(R.id.imageBack)
        about_Text = findViewById(R.id.about_text)
        imageBack.setOnClickListener{
            finish()
        }

        val aboutText = SpannableStringBuilder()

        val elaChat = SpannableStringBuilder("\nAbout LinkUp\n\n")
        elaChat.setSpan(StyleSpan(Typeface.BOLD), 0, elaChat.length, 0)
        elaChat.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, elaChat.length, 0)
        elaChat.setSpan(RelativeSizeSpan(1f), 0, elaChat.length, 0)
        aboutText.append(elaChat)

        val description = SpannableStringBuilder("LinkUp is a secure and user-friendly messaging application designed to facilitate seamless communication between users. With LinkUp, you can connect with friends, family, and colleagues in real-time, regardless of their location.\n\n")
        description.setSpan(RelativeSizeSpan(0.8f), 0, description.length, 0)
        description.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, description.length, 0)
        aboutText.append(description)

        val keyFeatures = SpannableStringBuilder("Key Features :\n\n")
        keyFeatures.setSpan(StyleSpan(Typeface.BOLD), 0, keyFeatures.length, 0)
        keyFeatures.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, keyFeatures.length, 0)
        keyFeatures.setSpan(RelativeSizeSpan(1f), 0, keyFeatures.length, 0)
        aboutText.append(keyFeatures)

        val endToEndEncryption = SpannableStringBuilder("1.End-to-End Encryption :")
        endToEndEncryption.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, endToEndEncryption.length, 0)

        endToEndEncryption.setSpan(StyleSpan(Typeface.BOLD), 0, endToEndEncryption.length, 0)
        endToEndEncryption.setSpan(RelativeSizeSpan(0.8f), 0, endToEndEncryption.length, 0)
        aboutText.append(endToEndEncryption)

        val endToEndEncryptionDetail= SpannableStringBuilder(" Your privacy and security are our top priorities. LinkUp uses end-to-end encryption to ensure that your messages, calls, and shared media remain private and secure.\n" +
                "\n")
        endToEndEncryptionDetail.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, endToEndEncryptionDetail.length, 0)

        endToEndEncryptionDetail.setSpan(RelativeSizeSpan(0.8f), 0, endToEndEncryptionDetail.length, 0)
        aboutText.append(endToEndEncryptionDetail)

        val multiPlatformSupport = SpannableStringBuilder("2.Multi-Platform Support :")
        multiPlatformSupport.setSpan(RelativeSizeSpan(0.8f), 0, multiPlatformSupport.length, 0)
        multiPlatformSupport.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, multiPlatformSupport.length, 0)
        multiPlatformSupport.setSpan(StyleSpan(Typeface.BOLD), 0, multiPlatformSupport.length, 0)
        aboutText.append(multiPlatformSupport)

        val multiPlatformSupportDetail = SpannableStringBuilder(" Whether you're using an Android device, iOS device, or web browser, LinkUp provides a consistent and reliable messaging experience across all platforms.\n\n")
        multiPlatformSupportDetail.setSpan(RelativeSizeSpan(0.8f), 0, multiPlatformSupportDetail.length, 0)
        multiPlatformSupportDetail.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, multiPlatformSupportDetail.length, 0)
        aboutText.append(multiPlatformSupportDetail)

        val RichMessaging = SpannableStringBuilder("3.Rich Messaging :")
        RichMessaging.setSpan(RelativeSizeSpan(0.8f), 0, RichMessaging.length, 0)
        RichMessaging.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, RichMessaging.length, 0)
        RichMessaging.setSpan(StyleSpan(Typeface.BOLD), 0, RichMessaging.length, 0)
        aboutText.append(RichMessaging)

        val RichMessagingDetail = SpannableStringBuilder(" Express yourself with a wide range of multimedia features, including text messages, photos, videos, voice messages, and stickers.\n\n")
        RichMessagingDetail.setSpan(RelativeSizeSpan(0.8f), 0, RichMessagingDetail.length, 0)
        RichMessagingDetail.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, RichMessagingDetail.length, 0)
        aboutText.append(RichMessagingDetail)

        val groupChats = SpannableStringBuilder("4.Group Chats :")
        groupChats.setSpan(RelativeSizeSpan(0.8f), 0, groupChats.length, 0)
        groupChats.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, groupChats.length, 0)
        groupChats.setSpan(StyleSpan(Typeface.BOLD), 0, groupChats.length, 0)
        aboutText.append(groupChats)

        val groupChaatsDetail = SpannableStringBuilder(" Stay connected with multiple friends or colleagues simultaneously with group chat functionality. Share updates, make plans, and collaborate effortlessly.\n\n")
        groupChaatsDetail.setSpan(RelativeSizeSpan(0.8f), 0, groupChaatsDetail.length, 0)
        groupChaatsDetail.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, groupChaatsDetail.length, 0)
        aboutText.append(groupChaatsDetail)

        val voiceAndVideoCall = SpannableStringBuilder("5.Voice & Video Calls :")
        voiceAndVideoCall.setSpan(RelativeSizeSpan(0.8f), 0, voiceAndVideoCall.length, 0)
        voiceAndVideoCall.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, voiceAndVideoCall.length, 0)
        voiceAndVideoCall.setSpan(StyleSpan(Typeface.BOLD), 0, voiceAndVideoCall.length, 0)
        aboutText.append(voiceAndVideoCall)

        val voiceAndVideoCallsDetail = SpannableStringBuilder(" Make crystal-clear voice and video calls to your contacts, with support for both one-on-one and group conversations.\n\n")
        voiceAndVideoCallsDetail.setSpan(RelativeSizeSpan(0.8f), 0, voiceAndVideoCallsDetail.length, 0)
        voiceAndVideoCallsDetail.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, voiceAndVideoCallsDetail.length, 0)
        aboutText.append(voiceAndVideoCallsDetail)

        val customize = SpannableStringBuilder("6.Customization Options :")
        customize.setSpan(RelativeSizeSpan(0.8f), 0, customize.length, 0)
        customize.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, customize.length, 0)
        customize.setSpan(StyleSpan(Typeface.BOLD), 0, customize.length, 0)
        aboutText.append(customize)


        val customizeDetail = SpannableStringBuilder(" Personalize your chat experience with customizable themes, chat backgrounds, and notification settings..\n\n")
        customizeDetail.setSpan(RelativeSizeSpan(0.8f), 0, customizeDetail.length, 0)
        customizeDetail.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, customizeDetail.length, 0)
        aboutText.append(customizeDetail)

        val messageRecall = SpannableStringBuilder("7.Message Recalls :")
        messageRecall.setSpan(RelativeSizeSpan(0.8f), 0, messageRecall.length, 0)
        messageRecall.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, messageRecall.length, 0)
        messageRecall.setSpan(StyleSpan(Typeface.BOLD), 0, messageRecall.length, 0)
        aboutText.append(messageRecall)

        val messageRecallDetail = SpannableStringBuilder(" Accidentally sent the wrong message? No problem! LinkUp allows you to recall sent messages within a specified time window.\n\n")
        messageRecallDetail.setSpan(RelativeSizeSpan(0.8f), 0, messageRecallDetail.length, 0)
        messageRecallDetail.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, messageRecallDetail.length, 0)
        aboutText.append(messageRecallDetail)

        val userProfile = SpannableStringBuilder("8.User Profile :")
        userProfile.setSpan(RelativeSizeSpan(0.8f), 0, userProfile.length, 0)
        userProfile.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, userProfile.length, 0)
        userProfile.setSpan(StyleSpan(Typeface.BOLD), 0, userProfile.length, 0)
        aboutText.append(userProfile)

        val userProfileDetail = SpannableStringBuilder(" Create and customize your user profile to let others know more about you. Add a profile picture, status message, and more.\n\n")
        userProfileDetail.setSpan(RelativeSizeSpan(0.8f), 0, userProfileDetail.length, 0)
        userProfileDetail.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, userProfileDetail.length, 0)
        aboutText.append(userProfileDetail)

        val lastDescription = SpannableStringBuilder("LinkUp is committed to providing a secure, reliable, and feature-rich messaging experience for all users. Download LinkUp now and start connecting with your loved ones today!\n\n")
        lastDescription.setSpan(RelativeSizeSpan(0.8f), 0, lastDescription.length, 0)
        lastDescription.setSpan(ForegroundColorSpan(resources.getColor(R.color.black)), 0, lastDescription.length, 0)
        aboutText.append(lastDescription)

       about_Text.text = aboutText
        
    }


}