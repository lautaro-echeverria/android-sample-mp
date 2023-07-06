package com.example.mpandroidapp

import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mparticle.MPEvent
import com.mparticle.MParticle
import com.mparticle.MParticleOptions
import com.mparticle.commerce.CommerceEvent
import com.mparticle.commerce.Impression
import com.mparticle.commerce.Product
import com.mparticle.commerce.TransactionAttributes
import com.mparticle.consent.CCPAConsent
import com.mparticle.consent.ConsentState
import com.mparticle.identity.BaseIdentityTask
import com.mparticle.identity.IdentityApiRequest
import com.mparticle.identity.MParticleUser
import java.util.*
import kotlin.collections.HashMap
import com.google.firebase.analytics.FirebaseAnalytics
import com.mparticle.consent.GDPRConsent


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val test = FirebaseAnalytics.getInstance(this).appInstanceId

        var mPID = ""
        val mPIDTextView = findViewById<TextView>(R.id.text1)
        var currUser = MParticle.getInstance()?.Identity()?.currentUser
        val currMPID = currUser?.id.toString()
        if (currMPID != "null") {
            val mPIDText = "mPID: $currMPID"
            mPIDTextView.text = mPIDText
        } else {
            mPIDTextView.text = "Pending"
        }

        val identityRequest = IdentityApiRequest.withEmptyUser()
            .customerId("LautaroTest1")
            .build()
        val identityTask = BaseIdentityTask()
            .addFailureListener { identityHttpResponse ->
                Log.d("IDSYNC FAIL" , identityHttpResponse.toString())
            }
            .addSuccessListener {
                it ->
                    Log.d("IDSYNC OK", it.toString())
                    currUser = it.user
                    mPID = it.getUser().id.toString()
                    mPIDTextView.text = "mPID: $mPID"
                    Log.d("MPID", mPID)
            }

        val options = MParticleOptions.builder(this)
            .credentials(
                "us1-1ad2ab7d87c237408c36e1faffbfa193",
                "kfh7enVhuGM9pH7oSIButfPEPqHNtKfYFov1kWNYGJx7TCwUD-AMjK53ijdnsQ-l")
            .identify(identityRequest)
            .environment(MParticle.Environment.Development)
            .logLevel(MParticle.LogLevel.VERBOSE)
            .identifyTask(identityTask)
            .dataplan("lautaro_s_data_plan",1)
            .build()
        MParticle.start(options)

        // enable location tracking
        MParticle.getInstance()?.enableLocationTracking(LocationManager.NETWORK_PROVIDER, 3*1000, 1000);

        MParticle.getInstance()?.Messaging()?.enablePushNotifications("621391172511");
        MParticle.getInstance()?.Messaging()?.displayPushNotificationByDefault(true);

        val logViewBut = findViewById<Button>(R.id.logPageViewBut)
        val loginBut = findViewById<Button>(R.id.loginBut)
        val logoutBut = findViewById<Button>(R.id.logoutBut)
        val modifyAttrBut = findViewById<Button>(R.id.modifyAttrBut)
        val removeAttrBut = findViewById<Button>(R.id.removeAttrBut)
        val customEventBut = findViewById<Button>(R.id.customEventBut)
        val purchaseEventBut = findViewById<Button>(R.id.purchaseEventBut)
        val impressionEventBut = findViewById<Button>(R.id.impressionBut)
        val ccpaOptInBut = findViewById<Button>(R.id.consentOptInBut)
        val ccpaOptOutBut = findViewById<Button>(R.id.consentOptOutBut)
        val customerIdBut = findViewById<Button>(R.id.updateCustomerIDBut)
        val webViewBut = findViewById<Button>(R.id.btnWebView)
        //val myWebView: WebView = findViewById(R.id.webview)
        //myWebView.loadUrl("http://www.example.com")

        webViewBut.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@MainActivity, WebViewActivity::class.java)
            startActivity(intent)
        })
        logViewBut.setOnClickListener(View.OnClickListener {
            val screenInfo = HashMap<String, String>()
            screenInfo["rating"] = "5"
            screenInfo["user::market"] = "androidcolontest"
            MParticle.getInstance()?.logScreen("Home", screenInfo)
//            val screenViewEvent = MPEvent.Builder("Home", MParticle.EventType.Navigation).build()
//            MParticle.getInstance()?.logEvent(screenViewEvent)
            Log.d("Hello", "Click log page view")
        })
        loginBut.setOnClickListener(View.OnClickListener{
            Log.d("LOGIN", "Click login")
            var userEmail = findViewById<EditText>(R.id.emailET).text.toString()
            var identityRequest = IdentityApiRequest.withEmptyUser().run {
                email(userEmail)
                build()
            }
            MParticle.getInstance()?.Identity()?.login(identityRequest)
            ?.addSuccessListener {
                Log.d("LOGIN", "OK")
                currUser = it.user
                mPID = it.getUser().id.toString()
                mPIDTextView.text = "mPID: $mPID"
            }
        })
        logoutBut.setOnClickListener(View.OnClickListener {
            Log.d("LOGOUT", "Click logout")
            MParticle.getInstance()?.Identity()?.logout()?.addSuccessListener {
                mPID = it.getUser().id.toString()
                currUser = it.user
                mPIDTextView.text = "mPID: $mPID"
                Log.d("LOGOUT", "OK")
            }
        })
        modifyAttrBut.setOnClickListener(View.OnClickListener {
            val userAttrKeyET = findViewById<EditText>(R.id.userAttrKeyET)
            val userAttrKey = userAttrKeyET.text.toString()
            val userAttrValET = findViewById<EditText>(R.id.userAttrValET)
            val userAttrVal = userAttrValET.text.toString()
            val currUser = MParticle.getInstance()?.Identity()?.currentUser
            currUser?.setUserAttribute(userAttrKey, userAttrVal)
            Log.d("USERATTR_MODIFY", "OK")
        })
        removeAttrBut.setOnClickListener(View.OnClickListener {
            val userAttrKeyET = findViewById<EditText>(R.id.userAttrKeyET)
            val userAttrKey = userAttrKeyET.text.toString()
            val currUser = MParticle.getInstance()?.Identity()?.currentUser
            currUser?.removeUserAttribute(userAttrKey)
            Log.d("USERATTR_REMOVE", "OK")
        })
        customEventBut.setOnClickListener(View.OnClickListener {
            val customAttributes = mapOf(
                "category" to "Destination Intro",
                "title" to "Paris",
                //"is_true" to true
            )
            val event = MPEvent.Builder("My Custom Event", MParticle.EventType.Navigation)
                .customAttributes(customAttributes)
                .build()
            MParticle.getInstance()?.logEvent(event)

            Log.d("EVENT LOGGED", "Clicked")
        })
        purchaseEventBut.setOnClickListener(View.OnClickListener {
            val product = Product.Builder("Item1", "SKU1234", 100.00)
                .quantity(1.0)
                .build()
            val attributes = TransactionAttributes("TxId1234")
            val event = CommerceEvent.Builder(Product.ADD_TO_WISHLIST, product)
                .transactionAttributes(attributes)
                .build()
        })
        impressionEventBut.setOnClickListener(View.OnClickListener {
            val product = Product.Builder("Item1", "SKU1234", 100.00)
                .quantity(1.0)
                .build()
            Impression("Suggested Products List", product).let {
                CommerceEvent.Builder(it).build()
            }.let {
                MParticle.getInstance()?.logEvent(it)
            }
        })
        ccpaOptInBut.setOnClickListener(View.OnClickListener {
            var ccpaConsent = CCPAConsent.builder(false)
                .document(getString(R.string.ccpa_agreement))
                .build()
            var state = ConsentState.builder()
                .setCCPAConsentState(ccpaConsent)
                .build()
            currUser?.setConsentState(state)
        })
        ccpaOptOutBut.setOnClickListener(View.OnClickListener {
            var ccpaConsent = CCPAConsent.builder(true)
                .document(getString(R.string.ccpa_agreement))
                .build()
            var state = ConsentState.builder()
                .setCCPAConsentState(ccpaConsent)
                .build()
            currUser?.setConsentState(state)
        })
        customerIdBut.setOnClickListener(View.OnClickListener{
//            var email = findViewById<EditText>(R.id.emailET).text.toString()
            val email = "lautaro@test.com"
            var customerId = findViewById<EditText>(R.id.userAttrValET).text.toString()
            val apiRequest = IdentityApiRequest.withUser(
                MParticle.getInstance()!!.Identity()?.currentUser
            )
                .customerId(customerId)
                .email(email)
                .build()

            if (MParticle.getInstance()!!.Identity().currentUser != null) {
                MParticle.getInstance()!!.Identity().modify(apiRequest)
            } else {
                MParticle.getInstance()!!.Identity().identify(apiRequest)
            }
        })
    }
}
