package com.example.myrun1.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.AsyncTask
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.example.myrun1.Activities.MapActivity
import com.example.myrun1.R
import com.example.myrun1.ViewModel.MyMapViewModel
import java.util.concurrent.ArrayBlockingQueue
import kotlin.math.sqrt

class MapService : Service(), SensorEventListener {
    private lateinit var notificationManager: NotificationManager
    private val NOTIFY_ID = 777
    private val CHANNEL_ID = "notification channel"
    private lateinit var  myBinder: MyBinder
    private var msgHandler: Handler? = null

    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private lateinit var mAccBuffer: ArrayBlockingQueue<Double>
    private lateinit var mAsyncTask: OnSensorChangedTask

    private var activityType = "Unknown"

    companion object{
        val STOP_SERVICE_ACTION = "stop service action"
        val MSG_INT_VALUE = 0
        val CLASSIFY_MSG_INT_VALUE = 1
    }

    // Modify the code from BindDemoKotlin
    override fun onCreate() {
        super.onCreate()
        mAccBuffer = ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY)

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        showNotification()
        myBinder = MyBinder()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        println("debug: Service onStartCommand() called")

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST)

        mAsyncTask = OnSensorChangedTask()
        mAsyncTask.execute()
        return START_NOT_STICKY
    }

    //XD:Multiple clients can connect to the service at once. However, the system calls your
    // service's onBind() method to retrieve the IBinder only when the first client binds.
    // The system then delivers the same IBinder to any additional clients that bind, without
    // calling onBind() again.
    override fun onBind(intent: Intent?): IBinder? {
        println("debug: Service onBind() called")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun setmsgHandler(msgHandler: Handler) {
            this@MapService.msgHandler = msgHandler
        }
    }

    private fun cleanupTasks()
    {
        msgHandler = null
        notificationManager.cancel(NOTIFY_ID)

        mAsyncTask.cancel(true)
        try {
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        mSensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("debug: Service onDestroy")
        cleanupTasks()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        println("debug: app removed from the application list")
        stopSelf()
    }


    private fun showNotification() {
        val intent = Intent(this, MapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ) //XD: see book p1019 why we do not use Notification.Builder
        notificationBuilder.setSmallIcon(R.drawable.rocket)
        notificationBuilder.setContentTitle("MyRuns")
        notificationBuilder.setContentText("Recording your path now")
        notificationBuilder.setContentIntent(pendingIntent)

        val notification = notificationBuilder.build()
        if (Build.VERSION.SDK_INT > 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFY_ID, notification)
    }

    // Modify the code from MyrunDataCollectorKotlin
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val m = sqrt((event.values[0] * event.values[0] + event.values[1] * event.values[1] + (event.values[2]
                    * event.values[2])).toDouble())

            // Inserts the specified element into this queue if it is possible
            // to do so immediately without violating capacity restrictions,
            // returning true upon success and throwing an IllegalStateException
            // if no space is currently available. When using a
            // capacity-restricted queue, it is generally preferable to use
            // offer.
            try {
                mAccBuffer.add(m)
            } catch (e: IllegalStateException) {

                // Exception happens when reach the capacity.
                // Doubling the buffer. ListBlockingQueue has no such issue,
                // But generally has worse performance
                val newBuf = ArrayBlockingQueue<Double>(mAccBuffer.size * 2)
                mAccBuffer.drainTo(newBuf)
                mAccBuffer = newBuf
                mAccBuffer.add(m)
            }
        }
    }

    // Modify the code from MyrunDataCollectorKotlin
    inner class OnSensorChangedTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg arg0: Void?): Void? {
            val featureVector = ArrayList<Double>(Globals.ACCELEROMETER_BLOCK_CAPACITY + 2)
            var blockSize = 0
            val fft = FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val accBlock = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            val im = DoubleArray(Globals.ACCELEROMETER_BLOCK_CAPACITY)
            var max = Double.MIN_VALUE
            while (true) {
                try {
                    // need to check if the AsyncTask is cancelled or not in the while loop
                    if (isCancelled() == true) {
                        return null
                    }

                    // Dumping buffer
                    accBlock[blockSize++] = mAccBuffer.take().toDouble()
                    if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0

                        // time = System.currentTimeMillis();
                        max = .0
                        for (`val` in accBlock) {
                            if (max < `val`) {
                                max = `val`
                            }
                        }
                        fft.fft(accBlock, im)
                        for (i in accBlock.indices) {
                            val msg = Math.sqrt(accBlock[i] * accBlock[i] + im[i]
                                    * im[i])
                            featureVector.add(msg)

                            im[i] = .0 // Clear the field
                        }

                        // Append max after frequency component
                        featureVector.add(max)

                        val result = WekaClassifier.classify(featureVector.toTypedArray()).toInt()
                        sendActivityType(activityType)

                        when (result) {
                            0 -> activityType = "Standing"
                            1 -> activityType = "Walking"
                            2 -> activityType = "Running"
                        }
                        println("DEBUG: type is $activityType")
                        featureVector.clear()
                    }
                }catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun sendActivityType(activityType: String) {
        if(msgHandler!=null){
            val bundle = Bundle()
            bundle.putString("CLASSIFY_KEY", activityType)

            val message = msgHandler!!.obtainMessage(CLASSIFY_MSG_INT_VALUE)
            message.data = bundle
            msgHandler!!.sendMessage(message)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}