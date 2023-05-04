package com.argent.acelerometro

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import info.mqtt.android.service.MqttAndroidClient;
//import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class AccActivity : AppCompatActivity(),SensorEventListener{
    //Varaiables Sensor
    private lateinit var  sensorManager:SensorManager
    private val lecturaACC = FloatArray(3)
    private val lecturaMGT = FloatArray(3)
    private val lecturaGYR = FloatArray(3)
    private val lecturaORT=FloatArray(3)
    private val matrizRotacion = FloatArray(9)
    private val angulosOrientacion = FloatArray(3)

    //Variables UI
    private lateinit var txtX:TextView
    private lateinit var txtY:TextView
    private lateinit var txtZ:TextView

    //Variables Broker
    private lateinit var broker: MqttAndroidClient
    private lateinit var ser:String
    private lateinit var por:String
    private lateinit var top:String
    private lateinit var url:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acc)
        txtX= findViewById(R.id.tvX)
        txtY= findViewById(R.id.tvY)
        txtZ= findViewById(R.id.tvZ)
        setupSensor()
         ser= intent.getStringExtra("ser").toString()
         por= intent.getStringExtra("pue").toString()
         top= intent.getStringExtra("top").toString()
         url="tcp://$ser:$por"
        Toast.makeText(this,url, Toast.LENGTH_SHORT).show()
        //setupBroker(this,"tcp://$ser:$por")
        connectBroker(this,url)
    }


    //CONFIGURA E INICIA EL SENSOR ACELROMETRO
    @Suppress("DEPRECATION")
    fun setupSensor(){
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
           }
        sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)?.also { orientation ->
            sensorManager.registerListener(
                this,
                orientation,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also { gyro ->
            sensorManager.registerListener(
                this,
                gyro,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onResume() {
        super.onResume()
        setupSensor()
        connectBroker(this,url)
    }

    //CUANDO LA ACTIVITY SE PAUSA
    override fun onPause() {
        sensorManager.unregisterListener(this)
        disconnectBroker()
        super.onPause()

    }

    //EVENTO CUANDO EL SENSOR LEE DATOS
    @Suppress("DEPRECATION")
    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, lecturaACC, 0, lecturaACC.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, lecturaMGT, 0, lecturaMGT.size)
            }
            Sensor.TYPE_ORIENTATION -> {
                System.arraycopy(event.values, 0, lecturaORT, 0, lecturaORT.size)
            }
            Sensor.TYPE_GYROSCOPE->{
                System.arraycopy(event.values, 0, lecturaGYR, 0, lecturaGYR.size)
            }
        }
        SensorManager.getRotationMatrix(matrizRotacion,null, lecturaACC, lecturaMGT)
        SensorManager.getOrientation(matrizRotacion, angulosOrientacion)
        val x:Double = Math.toDegrees(angulosOrientacion[1].toDouble())
        val y:Double = Math.toDegrees(angulosOrientacion[2].toDouble())
        val z:Double = Math.toDegrees(angulosOrientacion[0].toDouble())

        "P\t ${String.format("%.2f", x)}\t\t${String.format("%.2f", lecturaORT[1])}".also { txtX.text = it }
        "R:\t ${String.format("%.2f", y)}\t\t${String.format("%.2f", lecturaORT[2])}".also { txtY.text = it }
        "Y:\t ${String.format("%.2f", z)}\t\t${String.format("%.2f", lecturaORT[0])}".also { txtZ.text = it }
        //publish(top,"$x,$y,$z",0,false)
    }

    //EVENTO CUANDO EL SENSOR LEE ???
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    //CUANDO LA ACTIVITY SE DESTURYA
    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        disconnectBroker()
        super.onDestroy()
    }

    //FUNCION PARA CONECTAR EL BROKER
    private fun connectBroker(applicationContext : Context, url:String) {
        broker = MqttAndroidClient ( applicationContext,url,"CLIENTE" )
        try {
            val token = broker.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken)                        {
                    Log.i("Connection", "success ")
                    Toast.makeText(applicationContext,"CONECCION OK",Toast.LENGTH_LONG).show()
                    //connectionStatus = true
                    // Give your callback on connection established here
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    //connectionStatus = false

                    Toast.makeText(applicationContext,"CONECCION NO",Toast.LENGTH_LONG).show()
                    Log.i("Connection", "failure")
                    // Give your callback on connection failure here
                    exception.printStackTrace()
                }
            }
        } catch (e: MqttException) {
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    //FUNCION PARA PUBLICAR DATOS EN EL BROKER
    private fun publishBroker(topic: String, msg: String, qos: Int, retained: Boolean) {
        try {
            val token=broker.publish(topic,msg.toByteArray(),qos,retained)
            token.actionCallback = object : IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken) {
                   //Toast.makeText(applicationContext,"Publish OK",Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    //Toast.makeText(applicationContext,"Publish NO",Toast.LENGTH_SHORT).show()
                    exception.printStackTrace()
                }
            }
        } catch (e: MqttException) {
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    //FUNCION PARA DESCONECTAR EL BROKER, NO SE SI FUNCIONA
    private fun disconnectBroker() {
        try {
            broker.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    //FUNCION PARA QUE EL BOTON MANDE DATOS AL BROKER
    fun publicar(view: View){
        val x= Math.toDegrees(angulosOrientacion[1].toDouble())
        val y= Math.toDegrees(angulosOrientacion[2].toDouble())
        val z= Math.toDegrees(angulosOrientacion[0].toDouble())
        publishBroker(top,"$x,$y,$z",0,false)
    }
}