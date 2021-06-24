package com.example.flutter_amqp;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FlutterAmqpPlugin
 */
public class FlutterAmqpPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel methodChannel;
    private EventChannel eventChannel;
    private RabbitMqManager rabbitMqManager;
    static Handler incomingMessageHandler;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {

        methodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_amqp");
        methodChannel.setMethodCallHandler(this);
        eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "events");
        eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object listener, final EventChannel.EventSink eventSink) {
                incomingMessageHandler = new Handler() {
                    @SuppressLint("HandlerLeak")
                    @Override
                    public void handleMessage(Message msg) {
                        String message = msg.getData().getString("msg");
                        Log.i("test", "msg:" + message);
                        eventSink.success(message);
                    }
                };
            }

            @Override
            public void onCancel(Object listener) {
                //
            }
        });
        if (rabbitMqManager != null) {
            rabbitMqManager = new RabbitMqManager(incomingMessageHandler);
        }
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "connect":
                String url = call.argument("url");
                rabbitMqManager.connect(url, result);
                break;
            case "consumerQueue":
                String queueName = call.argument("queue_name");
                String queueKey = call.argument("queue_key");
                // first declare queue
                boolean success = rabbitMqManager.declareQueue(queueName,queueKey);
                if(success) {
                    // Then setup consumer
                    rabbitMqManager.consumerQueue(queueName, queueKey, result);
                }else{
                    result.success(false);
                }
                break;
            case "disconnect":
                rabbitMqManager.disConnect(result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
        eventChannel.setStreamHandler(null);
        rabbitMqManager.dispose();
        incomingMessageHandler = null;
    }
}
