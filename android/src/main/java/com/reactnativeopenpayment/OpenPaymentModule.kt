package com.reactnativeopenpayment

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter

import com.open.open_web_sdk.OpenPayment
import com.open.open_web_sdk.listener.PaymentStatusListener
import com.open.open_web_sdk.model.TransactionDetails

class OpenPaymentModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), PaymentStatusListener {

    val MAP_KEY_PAYMENT_ID = "payment_id"
    val MAP_KEY_PAYMENT_TOKEN_ID = "payment_token_id"
    val MAP_KEY_TRANSACTION_STATUS = "transaction_status"
    val MAP_KEY_ERROR_DESC = "description"
    val MAP_KEY_ACCESS_KEY = "accessKey"
    val MAP_KEY_ENVIRONMENT = "environment"
    private lateinit var openPayment: OpenPayment

    override fun getName(): String {
        return "OpenPayment"
    }

    // Example method
    // See https://facebook.github.io/react-native/docs/native-modules-android
    @ReactMethod
    fun multiply(a: Int, b: Int, promise: Promise) {
    
      promise.resolve(a * b)
    
    }

    @ReactMethod
    fun startPayment(options: ReadableMap) {
        val accessKey = options.getString(MAP_KEY_ACCESS_KEY)!!
        val paymentTokenId = options.getString(MAP_KEY_PAYMENT_TOKEN_ID)!!
        val environment = options.getString(MAP_KEY_ENVIRONMENT)!!
        openPayment =
            OpenPayment.Builder()
                .with(getCurrentActivity()!!)
                .setPaymentToken(paymentTokenId)
                .setEnvironment( if(environment=="LIVE") OpenPayment.Environment.LIVE else OpenPayment.Environment.SANDBOX)
                .setAccessKey(accessKey)
                .build()
        openPayment.setPaymentStatusListener(this)
        openPayment.startPayment()
    }

    override fun onError(message: String) {
        var writableMap:WritableMap = WritableNativeMap()
        writableMap.putString(MAP_KEY_ERROR_DESC, message);
        sendEvent("OpenPayment::PAYMENT_ERROR", writableMap);
        openPayment.detachListener()
    }

    override fun onTransactionCompleted(transactionDetails: TransactionDetails) {
        var writableMap:WritableMap = WritableNativeMap()
        writableMap.putString(MAP_KEY_PAYMENT_ID, transactionDetails.paymentId);
        writableMap.putString(MAP_KEY_PAYMENT_TOKEN_ID, transactionDetails.paymentTokenId);
        writableMap.putString(MAP_KEY_TRANSACTION_STATUS, transactionDetails.status);
        sendEvent("OpenPayment::PAYMENT_COMPLETED", writableMap);
        openPayment.detachListener() 
    }

    private fun sendEvent(eventName: String, params: WritableMap) {
        getReactApplicationContext()
                .getJSModule(RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
    }
    
}
