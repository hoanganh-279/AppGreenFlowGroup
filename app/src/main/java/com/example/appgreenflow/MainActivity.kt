package com.example.appgreenflow

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.appgreenflow.ui.home.HomeFragment
import com.example.appgreenflow.ui.notifications.NotificationsFragment
import com.example.appgreenflow.ui.policy.PolicyFragment
import com.example.appgreenflow.ui.route.RouteFragment
import com.example.appgreenflow.ui.settings.SettingsFragment
import com.example.appgreenflow.ui.support.SupportFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

// Thêm import
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var drawerLayout: DrawerLayout? = null
    private var drawerToggleBtn: ImageButton? = null
    private var navigationView: NavigationView? = null
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    private var user: FirebaseUser? = null

    // Getter cho role
    var userRole: String? = "customer"
        private set

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setTitle("GreenFlow")
        }

        drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        drawerToggleBtn = findViewById<ImageButton>(R.id.drawerToggleBtn)
        navigationView = findViewById<NavigationView>(R.id.navigationView)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = mAuth!!.getCurrentUser()
        if (user == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        drawerToggleBtn!!.setOnClickListener(View.OnClickListener { v: View? ->
            if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
                drawerLayout!!.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout!!.openDrawer(GravityCompat.START)
            }
        })

        navigationView!!.setNavigationItemSelectedListener(this)

        setupHeader()
        checkUserRole()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun setupHeader() {
        val headerView = navigationView!!.getHeaderView(0)
        if (headerView != null) {
            val textUserName = headerView.findViewById<TextView>(R.id.textUserName)
            val emailUser = headerView.findViewById<TextView>(R.id.emailUser)
            val userImage = headerView.findViewById<ImageView>(R.id.userImage)

            if (user != null) {
                emailUser.setText(if (user!!.getEmail() != null) user!!.getEmail() else "No email")
                textUserName.setText(if (user!!.getDisplayName() != null) user!!.getDisplayName() else (if (user!!.getEmail() != null) user!!.getEmail() else "User"))
                userImage.setImageResource(R.drawable.outline_account)
            }
        }
    }

    private fun checkUserRole() {
        db!!.collection("users").document(user!!.getUid()).get()
            .addOnSuccessListener(OnSuccessListener { documentSnapshot: DocumentSnapshot? ->
                if (documentSnapshot!!.exists()) {
                    userRole =
                        if (documentSnapshot.getString("role") != null) documentSnapshot.getString(
                            "role"
                        ) else "customer"
                    // Customize menu theo role
                    navigationView!!.getMenu().findItem(R.id.nav_support)
                        .setVisible("employee" == userRole)
                    Toast.makeText(this, "Chào " + userRole + "!", Toast.LENGTH_SHORT).show()

                    // Subscribe FCM topic cho employee (sau khi load role)
                    if ("employee" == userRole) {
                        FirebaseMessaging.getInstance().subscribeToTopic("employee")
                            .addOnCompleteListener(OnCompleteListener { task: Task<Void?>? ->
                                if (task!!.isSuccessful()) {
                                    Toast.makeText(
                                        this,
                                        "Đã subscribe thông báo nhân viên!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(this, "Lỗi subscribe FCM!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            })
                    }
                }
            }).addOnFailureListener(OnFailureListener { e: Exception? ->
                Toast.makeText(
                    this,
                    "Lỗi load role: " + e!!.message,
                    Toast.LENGTH_SHORT
                ).show()
            })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null
        var fragmentClass: Class<*>? = null
        val itemId = item.getItemId()

        if (itemId == R.id.nav_home) {
            fragmentClass = HomeFragment::class.java
        } else if (itemId == R.id.nav_route) {
            fragmentClass = RouteFragment::class.java
        } else if (itemId == R.id.nav_notifications) {
            fragmentClass = NotificationsFragment::class.java
        } else if (itemId == R.id.nav_support && "employee" == userRole) {
            fragmentClass = SupportFragment::class.java
        } else if (itemId == R.id.nav_consumer_policy) {
            fragmentClass = PolicyFragment::class.java
        } else if (itemId == R.id.nav_settings) {
            fragmentClass = SettingsFragment::class.java
        } else if (itemId == R.id.nav_logout) {
            mAuth!!.signOut()
            startActivity(Intent(this, Login::class.java))
            finish()
            return true
        }

        try {
            fragment = fragmentClass!!.newInstance() as Fragment
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi load fragment: " + e.message, Toast.LENGTH_SHORT).show()
        }

        if (fragment != null) {
            loadFragment(fragment)
            navigationView!!.setCheckedItem(itemId)
        }

        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = getSupportFragmentManager().beginTransaction()
        transaction.replace(R.id.nav_home, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth!!.getCurrentUser() == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    fun performLogout() {
        mAuth!!.signOut()
        startActivity(Intent(this, Login::class.java))
        finish()
    }

    fun loadRouteFragment(area: String?, lat: Double, lng: Double, percent: Int) {
        val args = Bundle()
        args.putString("area", area)
        args.putDouble("lat", lat)
        args.putDouble("lng", lng)
        args.putInt("percent", percent)

        val fragment = RouteFragment()
        fragment.setArguments(args)

        loadFragment(fragment)
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }
        Toast.makeText(this, "Chuyển đến vị trí thùng rác " + area, Toast.LENGTH_SHORT).show()
    }
}