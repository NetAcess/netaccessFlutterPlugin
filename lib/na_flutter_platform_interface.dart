import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'na_flutter_method_channel.dart';

abstract class NaFlutterPlatform extends PlatformInterface {
  /// Constructs a NaFlutterPlatform.
  NaFlutterPlatform() : super(token: _token);

  static final Object _token = Object();

  static NaFlutterPlatform _instance = MethodChannelNaFlutter();

  /// The default instance of [NaFlutterPlatform] to use.
  ///
  /// Defaults to [MethodChannelNaFlutter].
  static NaFlutterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [NaFlutterPlatform] when
  /// they register themselves.
  static set instance(NaFlutterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  dynamic getCurrentLocation() {
    throw UnimplementedError('getCurrentLocation() has not been implemented.');
  }

  dynamic goToMap() {
    throw UnimplementedError('goToMap() has not been implemented.');
  }

  dynamic print() {
    throw UnimplementedError('print() has not been implemented.');
  }
}
