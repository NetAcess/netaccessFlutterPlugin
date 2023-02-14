import 'package:flutter/material.dart';
import 'package:flutter_scan_bluetooth/flutter_scan_bluetooth.dart';
import 'package:na_flutter/na_flutter.dart';

class BleScanner extends StatefulWidget {
  @override
  _BleScannerState createState() => _BleScannerState();
}

class _BleScannerState extends State<BleScanner> {
  Map availableDevice = {};
  bool _scanning = false;
  final FlutterScanBluetooth _bluetooth = FlutterScanBluetooth();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) async {
      await _bluetooth.requestPermissions();
      if(_scanning) {
        await _bluetooth.stopScan();
      }
      await _bluetooth.startScan(pairedDevices: false);
    });
    _bluetooth.devices.listen((device) {
      _scanning = true;
      setState(() {
        availableDevice[device.address] = device.name;
      });
    });
    _bluetooth.scanStopped.listen((device) {
      setState(() {
        _scanning = false;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
        actions: [
          IconButton(onPressed: () async {
          if(_scanning) {
            await _bluetooth.stopScan();
          }
            await _bluetooth.startScan(pairedDevices: false);
          }, icon: const Icon(Icons.refresh))
        ],
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          Expanded(child: ListView.builder(
              shrinkWrap: false,
              itemCount: availableDevice.length,
              itemBuilder: (_, i) => getItem(i))),
        ],
      ),
    );
  }

  getItem(int index) {
    String address = availableDevice.keys.elementAt(index);
    return Container(
      color: Colors.grey,
      padding: const EdgeInsets.all(10.0),
      margin: const EdgeInsets.all(8.0),
      child: InkWell(
        onTap: () {
          final naFlutterPlugin = NaFlutter();
          naFlutterPlugin.print({"mac" : address});
          print("3.03");
        },
        child: Column(
          children: [
            Text(availableDevice[address]),
            Text(address),
          ],
        ),
      ),
    );
  }
}
