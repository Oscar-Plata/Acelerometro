package com.argent.acelerometro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var server: TextView
    private lateinit var topico: TextView
    private lateinit var puerto: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        server= findViewById<EditText>(R.id.server)
        topico= findViewById<EditText>(R.id.topic)
        puerto= findViewById<EditText>(R.id.port)
    }

    fun changeScreen(view: View){
        val txtser= server.text.toString()
        val txttop= topico.text.toString()
        val txtpue= puerto.text.toString()
        val intentbroker= Intent(this,AccActivity::class.java)
        Toast.makeText(this,"$txtser:$txtpue/$txttop",Toast.LENGTH_SHORT).show()
        startActivity(intentbroker)
    }
}