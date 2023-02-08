import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'na_flutter_platform_interface.dart';

/// An implementation of [NaFlutterPlatform] that uses method channels.
class MethodChannelNaFlutter extends NaFlutterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('na_flutter');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  dynamic getCurrentLocation() async {
    dynamic currentLocation = await methodChannel.invokeMethod<dynamic>('getCurrentLocation');
    return currentLocation;
  }

  @override
  dynamic goToMap() async {
    dynamic result = await methodChannel.invokeMethod<dynamic>('goToMap');
    return result;
  }

  @override
  dynamic print() async {
    dynamic result = await methodChannel.invokeMethod<dynamic>('print');
    return result;
  }
}
