package com.homeops.grocery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.homeops.grocery.navigation.NavGraph
import com.homeops.grocery.ui.theme.HomeOpsGroceryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeOpsGroceryTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
