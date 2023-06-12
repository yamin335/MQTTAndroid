package com.rtchubs.mqttproject.ui

import android.annotation.SuppressLint
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.rtchubs.mqttproject.MessageModel
import com.rtchubs.mqttproject.R
import com.rtchubs.mqttproject.databinding.MainActivityBinding
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.UnsupportedEncodingException

const val USERNAME = "ParkRFID"
const val PASSWORD = "4PRFID0nly"
const val HOST ="54.255.229.67"
const val PORT = 1883
const val TOPIC = "rfid/message"
class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding
    private lateinit var adapter: MessageListAdapter
    private lateinit var client: MqttAndroidClient
    private lateinit var token: IMqttToken

    private val messages = arrayListOf(
        MessageModel("000000001", R.drawable.img_0), MessageModel("48229571", R.drawable.img_1),
        MessageModel("49093351", R.drawable.img_2), MessageModel("28076931", R.drawable.img_3),
        MessageModel("48879751", R.drawable.img_4), MessageModel("48956001", R.drawable.img_5),
        MessageModel("48821271", R.drawable.img_6), MessageModel("48543801", R.drawable.img_7),
        MessageModel("48799541", R.drawable.img_8), MessageModel("49139421", R.drawable.img_9),
        MessageModel("48996531", R.drawable.img_10), MessageModel("48597591", R.drawable.img_11),
        MessageModel("48451191", R.drawable.img_12), MessageModel("47687151", R.drawable.img_13),
        MessageModel("47597751", R.drawable.img_14), MessageModel("47480761", R.drawable.img_15),
        MessageModel("47810491", R.drawable.img_16), MessageModel("48451071", R.drawable.img_17)
    )

    override fun onStart() {
        super.onStart()
        client.registerResources(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        client.disconnect().actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {

            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {

            }
        }
        client.unregisterResources()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this

        adapter = MessageListAdapter( itemCallback = {
            publishPayLoad(payload = "{Status: On}", onTopic = it.rfId)
        }, messageCallback =  {
            binding.preview.setImageResource(it.image)
        })

        binding.recyclerView.adapter = adapter
        adapter.submitList(messages)

        val clientId = MqttClient.generateClientId()
        val uri = "tcp://$HOST:$PORT"

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.userName = USERNAME
        mqttConnectOptions.password = PASSWORD.toCharArray()

        client = MqttAndroidClient(this, uri, clientId)
        token = client.connect(mqttConnectOptions)
        token.actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                subscribe(TOPIC)
            }

            @SuppressLint("LongLogTag")
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                exception?.message?.let { Log.e("MqttClient ERROR: ", it)}
            }
        }

        client.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.e("Mqtt Status: ", "CONNECTED!!")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                message?.toString()?.let {
                    adapter.setMessage(message = it)
                    subscribe(it)
                }

                Log.e("Mqtt Status: ", "MESSAGE ARRIVED!!")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.e("Mqtt Status: ", "CONNECTION LOST!!")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.e("Mqtt Status: ", "MESSAGE DELIVERED!!")
            }
        })

        val boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD)
        val normalTypeface = Typeface.defaultFromStyle(Typeface.NORMAL)

        binding.switchOnOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textOn.typeface = boldTypeface
                binding.textOff.typeface = normalTypeface
                binding.statusIndicator.setImageResource(R.drawable.status_on)
            } else {
                binding.textOff.typeface = boldTypeface
                binding.textOn.typeface = normalTypeface
                binding.statusIndicator.setImageResource(R.drawable.status_off)
            }
        }
    }

    private fun publishPayLoad(payload: String, onTopic: String) {
        val encodedPayload: ByteArray
        try {
            encodedPayload = payload.toByteArray(charset("UTF-8"))
            val message = MqttMessage(encodedPayload)
            client.publish(onTopic, message)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String) {
        client.subscribe(topic, 0, null, object : IMqttActionListener {
            @SuppressLint("LongLogTag")
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.e("Mqtt Status: ", "SUBSCRIBED!!")
            }

            @SuppressLint("LongLogTag")
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                exception?.message?.let { Log.e("MqttClient ERROR: ", it)}
            }
        })

    }
}