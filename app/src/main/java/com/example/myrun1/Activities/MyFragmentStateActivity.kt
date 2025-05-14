package com.example.myrun1.Activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.myrun1.Fragments.HistoryFragment
import com.example.myrun1.MyFragmentStateAdapter
import com.example.myrun1.R
import com.example.myrun1.Fragments.SettingFragment
import com.example.myrun1.Fragments.StartFragment
import com.example.myrun1.Util
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import java.util.ArrayList

// Modify the code from ActiontabsKotlin
class MyFragmentStateActivity : AppCompatActivity() {
    private lateinit var startFragment: StartFragment
    private lateinit var historyFragment: HistoryFragment
    private lateinit var settingFragment: SettingFragment
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var myMyFragmentStateAdapter: MyFragmentStateAdapter
    private lateinit var fragments: ArrayList<Fragment>

    //Tab titles
    private val tabTitles = arrayOf("START", "HISTORY", "SETTINGS")
    private lateinit var tabConfigurationStrategy: TabConfigurationStrategy
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_fragment_state)

        // Initialize the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Ran_WANG_MyRuns5"

        viewPager2 = findViewById(R.id.viewpager)
        tabLayout = findViewById(R.id.tab)

        startFragment = StartFragment()
        historyFragment = HistoryFragment()
        settingFragment = SettingFragment()

        fragments = ArrayList()
        fragments.add(startFragment)
        fragments.add(historyFragment)
        fragments.add(settingFragment)

        myMyFragmentStateAdapter = MyFragmentStateAdapter(this, fragments)
        viewPager2.adapter = myMyFragmentStateAdapter

        tabConfigurationStrategy = TabConfigurationStrategy {
                tab: TabLayout.Tab, position: Int ->
            tab.text = tabTitles[position] }
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabLayoutMediator.attach()

        Util.checkPermissions(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator.detach()
    }
}