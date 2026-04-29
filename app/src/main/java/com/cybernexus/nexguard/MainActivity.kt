package com.cybernexus.nexguard
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val tv = TextView(this)
  tv.text = "NexGuard V2 Running"
  tv.textSize = 24f
  setContentView(tv)
 }
}
