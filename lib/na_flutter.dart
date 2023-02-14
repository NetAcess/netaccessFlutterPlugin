
import 'na_flutter_platform_interface.dart';

class NaFlutter {
  Future<String?> getPlatformVersion() {
    return NaFlutterPlatform.instance.getPlatformVersion();
  }

  dynamic getCurrentLocation() {
    return NaFlutterPlatform.instance.getCurrentLocation();
  }

  dynamic goToMap() {
    return NaFlutterPlatform.instance.goToMap();
  }

  dynamic print(input) {
    return NaFlutterPlatform.instance.print(input);
  }
}
