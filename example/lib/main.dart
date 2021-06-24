import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_amqp/flutter_amqp.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterAmqp.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Center(
              child: Text('Running on: $_platformVersion\n'),
            ),
            Center(
              child: StreamBuilder<String>(
                stream: FlutterAmqp.rabbitMQConsumer(),
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    return Text('Data: ${snapshot.data}');
                  } else if (snapshot.hasError) {
                    return Text('Error: ${snapshot.error}');
                  } else {
                    return Text("Loading");
                  }
                },
              ),
            ),
            ElevatedButton(
              onPressed: () async {
                try {
                  await FlutterAmqp.setupConnection();
                } catch (e) {
                  print(e);
                }
              },
              child: Text("Connect"),
            ),
            ElevatedButton(
              onPressed: () async {
                try {
                  await FlutterAmqp.subscribe();
                } catch (e) {
                  print(e);
                }
              },
              child: Text("Subscribe"),
            ),
          ],
        ),
      ),
    );
  }
}
