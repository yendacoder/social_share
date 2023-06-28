import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

enum InstalledApp {
  instagram('instagram'),
  facebook('facebook'),
  twitter('twitter'),
  whatsapp('whatsapp'),
  telegram('telegram'),
  sms('sms');

  const InstalledApp(this.appName);

  final String appName;
}

class SocialShare {
  static const MethodChannel _channel = const MethodChannel('social_share');

  static Future<String?> shareInstagramStory({
    required String imagePath,
    String? backgroundTopColor,
    String? backgroundBottomColor,
    String? attributionURL,
    String? backgroundImagePath,
    String? appId,
  }) async {
    Map<String, dynamic> args;
    args = <String, dynamic>{
      "stickerImage": imagePath,
      "backgroundTopColor": backgroundTopColor,
      "backgroundBottomColor": backgroundBottomColor,
      "attributionURL": attributionURL,
      "appId": appId,
    };
    if (backgroundImagePath != null) {
      args["backgroundImage"] = backgroundImagePath;
    }
    return await _channel.invokeMethod(
      'shareInstagramStory',
      args,
    );
  }

  static Future<String?> shareInstagramFeed(String imagePath) async {
    Map<String, dynamic> args = <String, dynamic>{
      "imagePath": imagePath,
    };
    final String? response = await _channel.invokeMethod(
      'shareInstagramFeed',
      args,
    );
    return response;
  }

  /// [appId] is optional, if not set, the value set in
  /// AndroidManifest/Info.plist will be used
  static Future<String?> shareFacebookStory({
    required String imagePath,
    String? backgroundTopColor,
    String? backgroundBottomColor,
    String? attributionURL,
    String? backgroundImagePath,
    String? appId,
  }) async {
    Map<String, dynamic> args;
    args = <String, dynamic>{
      "stickerImage": imagePath,
      "backgroundTopColor": backgroundTopColor,
      "backgroundBottomColor": backgroundBottomColor,
      "attributionURL": attributionURL,
      "appId": appId,
    };
    if (backgroundImagePath != null) {
      args["backgroundImage"] = backgroundImagePath;
    }
    return await _channel.invokeMethod(
      'shareFacebookStory',
      args,
    );
  }

  static Future<String?> shareTwitter(String captionText,
      {List<String>? hashtags, String? url, String? trailingText}) async {
    Map<String, dynamic> args;
    String modifiedUrl;
    if (Platform.isAndroid) {
      modifiedUrl = Uri.parse(url ?? '').toString().replaceAll('#', "%23");
    } else {
      modifiedUrl = Uri.parse(url ?? '').toString();
    }
    if (hashtags != null && hashtags.isNotEmpty) {
      String tags = "";
      hashtags.forEach((f) {
        tags += ("%23" + f.toString() + " ").toString();
      });
      args = <String, dynamic>{
        "captionText": captionText + "\n" + tags.toString(),
        "url": modifiedUrl,
        "trailingText": trailingText ?? ''
      };
    } else {
      args = <String, dynamic>{
        "captionText": captionText + " ",
        "url": modifiedUrl,
        "trailingText": trailingText ?? ''
      };
    }
    final String? version = await _channel.invokeMethod('shareTwitter', args);
    return version;
  }

  static Future<String?> shareSms(String message,
      {String? url, String? trailingText}) async {
    Map<String, dynamic>? args;
    if (Platform.isIOS) {
      if (url == null) {
        args = <String, dynamic>{
          "message": message,
        };
      } else {
        args = <String, dynamic>{
          "message": message + " ",
          "urlLink": Uri.parse(url).toString(),
          "trailingText": trailingText
        };
      }
    } else if (Platform.isAndroid) {
      args = <String, dynamic>{
        "message": message + (url ?? '') + (trailingText ?? ''),
      };
    }
    final String? version = await _channel.invokeMethod('shareSms', args);
    return version;
  }

  static Future<bool?> copyToClipboard(content) async {
    final Map<String, String> args = <String, String>{
      "content": content.toString()
    };
    final bool? response = await _channel.invokeMethod('copyToClipboard', args);
    return response;
  }

  static Future<bool?> shareOptions(String contentText,
      {String? imagePath}) async {
    Map<String, dynamic> args = <String, dynamic>{
      "image": imagePath,
      "content": contentText
    };
    final bool? version = await _channel.invokeMethod('shareOptions', args);
    return version;
  }

  static Future<String?> shareWhatsapp(String content) async {
    final Map<String, dynamic> args = <String, dynamic>{"content": content};
    final String? version = await _channel.invokeMethod('shareWhatsapp', args);
    return version;
  }

  static Future<Map<InstalledApp, bool>> checkInstalledApps() async {
    final Map? apps = await _channel.invokeMethod('checkInstalledApps');
    return Map.fromEntries(InstalledApp.values.map((app) =>
        MapEntry<InstalledApp, bool>(
            app,
            apps?.entries.any((entry) =>
                    entry.toString() == app.appName && entry.value == true) ==
                true)));
  }

  static Future<String?> shareTelegram(String content) async {
    final Map<String, dynamic> args = <String, dynamic>{"content": content};
    final String? version = await _channel.invokeMethod('shareTelegram', args);
    return version;
  }
}
