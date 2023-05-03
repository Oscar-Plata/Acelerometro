package com.argent.acelerometro

import android.content.ContentValues.TAG
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
//import info.mqtt.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class AccActivity : AppCompatActivity(),SensorEventListener{
    private lateinit var  sensorManager:SensorManager
    private lateinit var sensor:Sensor
    private lateinit var txtX:TextView
    private lateinit var txtY:TextView
    private lateinit var txtZ:TextView
    private lateinit var broker: MqttAndroidClient
    private lateinit var ser:String
    private lateinit var por:String
    private lateinit var top:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acc)
        txtX=findViewById(R.id.tvX) as TextView
        txtY=findViewById(R.id.tvY) as TextView
        txtZ=findViewById(R.id.tvZ) as TextView
        setupSensor()

        //BROKER
//         ser= intent.getStringExtra("ser").toString()
//         por= intent.getStringExtra("pue").toString()
//         top= intent.getStringExtra("top").toString()
//        Toast.makeText(this,"tcp://$ser:$por", Toast.LENGTH_SHORT).show()
        //setupBroker(this,"tcp://$ser:$por")
        //connect(this,"tcp://$ser:$por")

    }


    //CONFIGURA E INICIA EL SENSOR ACELROMETRO
    fun setupSensor(){
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if(sensor!=null){
            sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    //EVENTO CUANDO EL SENSOR LEE DATOS
    override fun onSensorChanged(event: SensorEvent?) {
        val x= event?.values?.get(0)
        val y= event?.values?.get(1)
        val z= event?.values?.get(2)
        txtX.text =  String.format("%.2f", x)
        txtY.text =  String.format("%.2f", y)
        txtZ.text =  String.format("%.2f", z)
        //publish(top,"$x,$y,$z",0,false)
    }
    //EVENTO CUANDO EL SENSOR LEE ???
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    //AL DESTRUIR ACTIVITY
    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        //disconnect()
        super.onDestroy()
    }
    
    
    fun setupBroker(context: Context,url:String){
        try {
            broker = MqttAndroidClient(context, url, "accUser")
            var options = MqttConnectOptions()
            broker.connect(options)
            if (broker.isConnected) {
                Toast.makeText(context, "Conectado", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, "No conectado", Toast.LENGTH_SHORT).show()
            }
        }catch (e: MqttException){
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun connect(applicationContext : Context,url:String) {
        broker = MqttAndroidClient ( applicationContext,url,"CLIENTE" )
        try {
            val token = broker.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken)                        {
                    Log.i("Connection", "success ")
                    //connectionStatus = true
                    // Give your callback on connection established here
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    //connectionStatus = false
                    Log.i("Connection", "failure")
                    // Give your callback on connection failure here
                    exception.printStackTrace()
                }
            }
        } catch (e: MqttException) {
            // Give your callback on connection failure here
            e.printStackTrace()
        }
    }


    fun publish(topic: String, msg: String, qos: Int = 0, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            broker.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            broker.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }





}