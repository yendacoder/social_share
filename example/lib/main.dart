import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:social_share/social_share.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Container(
          color: Colors.white,
          alignment: Alignment.center,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[
              ElevatedButton(
                onPressed: () async {
                  final file = await ImagePicker().pickImage(
                    source: ImageSource.gallery,
                  );
                  if (file?.path != null) {
                    SocialShare.shareInstagramStory(
                      imagePath: file!.path,
                      backgroundTopColor: "#ffffff",
                      backgroundBottomColor: "#000000",
                      attributionURL: "https://deep-link-url",
                    ).then((data) {
                      print(data);
                    });
                  }
                },
                child: Text("Share On Instagram Story"),
              ),
              ElevatedButton(
                onPressed: () async {
                  final file = await ImagePicker().pickImage(
                    source: ImageSource.gallery,
                  );
                  if (file?.path != null) {
                    SocialShare.shareInstagramStory(
                      imagePath: file!.path,
                      backgroundTopColor: "#ffffff",
                      backgroundBottomColor: "#000000",
                      attributionURL: "https://deep-link-url",
                      backgroundImagePath: file.path,
                    ).then((data) {
                      print(data);
                    });
                  }
                  ;
                },
                child: Text("Share On Instagram Story with background"),
              ),
              ElevatedButton(
                onPressed: () async {
                  final file = await ImagePicker().pickImage(
                    source: ImageSource.gallery,
                  );
                  if (file?.path != null) {
                    //facebook appId is mandatory for andorid or else share won't work
                    Platform.isAndroid
                        ? SocialShare.shareFacebookStory(
                            imagePath: file!.path,
                            backgroundTopColor: "#ffffff",
                            backgroundBottomColor: "#000000",
                            attributionURL: "https://google.com",
                            appId: "xxxxxxxxxxxxx",
                          ).then((data) {
                            print(data);
                          })
                        : SocialShare.shareFacebookStory(
                            imagePath: file!.path,
                            backgroundTopColor: "#ffffff",
                            backgroundBottomColor: "#000000",
                            attributionURL: "https://google.com",
                          ).then((data) {
                            print(data);
                          });
                  }
                  ;
                },
                child: Text("Share On Facebook Story"),
              ),
              ElevatedButton(
                onPressed: () async {
                  SocialShare.copyToClipboard(
                    "This is Social Share plugin",
                  ).then((data) {
                    print(data);
                  });
                },
                child: Text("Copy to clipboard"),
              ),
              ElevatedButton(
                onPressed: () async {
                  SocialShare.shareTwitter(
                    "This is Social Share twitter example",
                    hashtags: ["hello", "world", "foo", "bar"],
                    url: "https://google.com/#/hello",
                    trailingText: "\nhello",
                  ).then((data) {
                    print(data);
                  });
                },
                child: Text("Share on twitter"),
              ),
              ElevatedButton(
                onPressed: () async {
                  SocialShare.shareSms(
                    "This is Social Share Sms example",
                    url: "\nhttps://google.com/",
                    trailingText: "\nhello",
                  ).then((data) {
                    print(data);
                  });
                },
                child: Text("Share on Sms"),
              ),
              ElevatedButton(
                onPressed: () async {
                  SocialShare.shareOptions("Hello world").then((data) {
                    print(data);
                  });
                },
                child: Text("Share Options"),
              ),
              ElevatedButton(
                onPressed: () async {
                  SocialShare.shareWhatsapp(
                    "Hello World \n https://google.com",
                  ).then((data) {
                    print(data);
                  });
                },
                child: Text("Share on Whatsapp"),
              ),
              ElevatedButton(
                onPressed: () async {
                  SocialShare.shareTelegram(
                    "Hello World \n https://google.com",
                  ).then((data) {
                    print(data);
                  });
                },
                child: Text("Share on Telegram"),
              ),
              ElevatedButton(
                onPressed: () async {
                  SocialShare.checkInstalledApps().then((data) {
                    print(data.toString());
                  });
                },
                child: Text("Get all Apps"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
