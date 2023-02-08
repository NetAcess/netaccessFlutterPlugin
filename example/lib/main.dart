import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:na_flutter/na_flutter.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _currentLocation = 'nill';
  String _mapScreenResult = 'nill';
  String _printScreenResult = 'nill';
  final _naFlutterPlugin = NaFlutter();

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
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
              } on PlatformException {
                _platformVersion = 'Failed to get platform version.';
              }
              setState(() {});
            }, child: const Text('Get Version')),

            Center(child: Text('Current Location on: $_currentLocation\n')),
            ElevatedButton(onPressed: () async {
              try {
                _currentLocation = await _naFlutterPlugin.getCurrentLocation() ?? 'Unknown platform version';
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
              } on PlatformException {
                _mapScreenResult = 'platform Exception';
              }
              setState(() {});
            }, child: const Text('Go To Map')),

            Center(child: Text('Print Screen Result: $_printScreenResult\n')),
            ElevatedButton(onPressed: () async {
              try {
                _printScreenResult = await _naFlutterPlugin.print() ?? 'Unknown platform version';
              } on PlatformException {
                _printScreenResult = 'platform Exception';
              }
              setState(() {});
            }, child: const Text('Test Print')),
          ],
        ),
      ),
    );
  }
}
