import 'package:flutter/material.dart';

import 'package:flutter/services.dart';
import 'package:na_flutter/na_flutter.dart';
import 'package:na_flutter_example/bluetoothScan.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      routes: {
        "scanner" : (context) => BleScanner(),
      },
      home: HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  @override
  Widget build(BuildContext context) {
    String _platformVersion = 'Unknown';
    String _currentLocation = 'nill';
    String _mapScreenResult = 'nill';
    String _printScreenResult = 'nill';

    final _naFlutterPlugin = NaFlutter();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Center(child: Text('Running on: $_platformVersion\n')),
          ElevatedButton(onPressed: () async {
            try {
              _platformVersion = await _naFlutterPlugin.getPlatformVersion() ?? 'Unknown platform version';
              showSnack(_platformVersion);
            } on PlatformException {
              _platformVersion = 'Failed to get platform version.';
            }
            setState(() {});
          }, child: const Text('Get Version')),

          Center(child: Text('Current Location on: $_currentLocation\n')),
          ElevatedButton(onPressed: () async {
            try {
              _currentLocation = await _naFlutterPlugin.getCurrentLocation() ?? 'Unknown platform version';
              showSnack(_currentLocation);
            } on PlatformException {
              _currentLocation = 'Cant get Current Location';
            }
            setState(() {});
          }, child: const Text('Get Current Location')),

          Center(child: Text('Map Screen Result: $_mapScreenResult\n')),
          ElevatedButton(onPressed: () async {
            try {
              Map result = await _naFlutterPlugin.goToMap() ?? 'Unknown platform version';
              _mapScreenResult = result['xmlValue'];
              showSnack(result["savelat"] + " : " + result['savelng']);
            } on PlatformException {
              _mapScreenResult = 'platform Exception';
            }
            setState(() {});
          }, child: const Text('Go To Map')),

          Center(child: Text('Print Screen Result: $_printScreenResult\n')),
          ElevatedButton(onPressed: () {
            final naFlutterPlugin = NaFlutter();
            naFlutterPlugin.print({"mac" : "00:04:3E:90:AC:F9", "qrdt" : "123456", "fact" : "sdasd"});
            //Navigator.pushNamed(context, "scanner");
          }, child: const Text('Test Print')),
        ],
      ),
    );
  }

  showSnack(msg) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
  }

}
