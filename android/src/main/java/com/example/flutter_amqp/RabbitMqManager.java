package com.example.flutter_amqp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;


import io.flutter.plugin.common.MethodChannel;

public class RabbitMqManager {
    private final Handler handler;

    // members
    Connection connection;
    Channel channel;
    Thread subscribeThread;

    public RabbitMqManager(Handler handler) {
        this.handler = handler;
    }

    /// connect
    /// Params -> `url` the rabbitmq connection uri and `result` for sending
    /// events bac to the flutter world via method channels
    public void connect(String url, @NonNull MethodChannel.Result result) {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(url);
            connection = factory.newConnection();
            if (connection != null) {
                channel = connection.createChannel();
                channel.basicQos(1);
            } else {
                result.success(false);
            }
            result.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            result.error(null, e.toString(), null);
        }
    }
    
    // disconnect
    public void disConnect(@NonNull MethodChannel.Result result){
        try{
            if(connection ==null){
                result.success(false);
            }else{
                connection.close();
                result.success(true);
            }
        }catch (Exception e){
            e.printStackTrace();
            result.error(null, e.toString(), null);
        }
    }

    // declareQueue
    public void declareQueue(String queueName, String queueKey) {
        try {
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, "amq.direct", queueKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // consumerQueue
    public void consumerQueue(final String queueName, final String queueKey, @NonNull final MethodChannel.Result result) {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Consumer consumer = new DefaultConsumer(channel) {
                            @Override
                            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                                try {
                                    long deliveryTag = envelope.getDeliveryTag();
                                    System.out.println(body.toString());
                                    channel.basicAck(deliveryTag, false);
                                    String msg = new String(body);
                                    channel.basicAck(deliveryTag, false);
                                    // Get the object from the message msg pool more efficient
                                    Message uiMsg = handler.obtainMessage();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("msg", msg);
                                    uiMsg.setData(bundle);
                                    handler.sendMessage(uiMsg);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    result.error(null, e.toString(), null);
                                }
                            }
                        };
                        channel.queueBind(queueName, "amq.direct", queueKey);
                        channel.basicConsume(queueName, true, consumer);
                    } catch (Exception e) {
                        e.printStackTrace();
                        result.error(null, e.toString(), null);
                    }
                }
            }
        });
        subscribeThread.start();
    }

    public void publishToExchange(byte[] message, String exchangeName, String routingKey) {
        // adding a expiration
        // new AMQP.BasicProperties.Builder().expiration("60000").build(),
        // then add to props
        try {
            channel.basicPublish(exchangeName, routingKey, null, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // dispose
    public void dispose() {
        try {
            if (channel != null) {
                channel.close();
            }
            if (connection != null) {
                connection.close();
            }
            if (subscribeThread != null) {
                subscribeThread.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
