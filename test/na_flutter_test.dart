import 'package:flutter_test/flutter_test.dart';
import 'package:na_flutter/na_flutter.dart';
import 'package:na_flutter/na_flutter_platform_interface.dart';
import 'package:na_flutter/na_flutter_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockNaFlutterPlatform
    with MockPlatformInterfaceMixin
    implements NaFlutterPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final NaFlutterPlatform initialPlatform = NaFlutterPlatform.instance;

  test('$MethodChannelNaFlutter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelNaFlutter>());
  });

  test('getPlatformVersion', () async {
    NaFlutter naFlutterPlugin = NaFlutter();
    MockNaFlutterPlatform fakePlatform = MockNaFlutterPlatform();
    NaFlutterPlatform.instance = fakePlatform;

    expect(await naFlutterPlugin.getPlatformVersion(), '42');
  });
}
