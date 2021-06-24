import 'dart:async';

import 'package:flutter/services.dart';

class FlutterAmqp {
  static StreamController<String> _streamController;

  static StreamSubscription _streamSubscription;
  static const MethodChannel _methodChannel =
      const MethodChannel('flutter_amqp');

  static const EventChannel _eventChannel = const EventChannel('events');

  static Future<String> get platformVersion async {
    final String version =
        await _methodChannel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Stream<String> rabbitMQConsumer() {
    _streamSubscription?.cancel();
    _streamController = StreamController();
    _streamController.add("event");
    _streamSubscription =
        _eventChannel.receiveBroadcastStream().listen((event) {
      _streamController.add(event.toString());
    });
    return _streamController.stream;
  }

  static Future<bool> setupConnection() async {
    final bool res = await _methodChannel.invokeMethod('setupConnection');
    return res;
  }

  static Future<bool> subscribe() async {
    final bool res = await _methodChannel.invokeMethod('subscribe');
    return res;
  }

  static void close() {
    _streamSubscription?.cancel();
    _streamController?.close();
  }
}
