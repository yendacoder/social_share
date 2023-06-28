package com.shekarmudaliyar.social_share

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.core.content.FileProvider
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File

class SocialSharePlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var activity: Activity? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "social_share")
        channel.setMethodCallHandler(this)
    }

    private fun getImageUri(imagePath: String?): Uri? {
        if (imagePath == null) {
            return null
        }
        val context = activity?.applicationContext ?: throw RuntimeException("Context unavailable")
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".com.shekarmudaliyar.social_share",
            File(imagePath)
        )
    }

    private fun getAppId(context: Context, call: MethodCall): String? {
        val appIdOverride = call.argument<String?>("appId")
        if (appIdOverride != null) {
            return appIdOverride
        }
        val meta = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData
        return meta.getString("com.facebook.sdk.ApplicationId")
    }

    private fun shareInstagramStory(activity: Activity, call: MethodCall): Boolean {
        val context = activity.applicationContext
        val stickerImage: String? = call.argument("stickerImage")
        val backgroundImage: String? = call.argument("backgroundImage")

        val stickerImageFile = getImageUri(stickerImage)
        val intent = Intent("com.instagram.share.ADD_TO_STORY")
        intent.type = "image/*"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("interactive_asset_uri", stickerImageFile)
        if (backgroundImage != null) {
            val backgroundImageFile = getImageUri(backgroundImage)
            intent.setDataAndType(backgroundImageFile, "image/*")
        }
        intent.putExtra("source_application", getAppId(context, call))
        intent.putExtra("content_url", call.argument<String?>("attributionURL"))
        intent.putExtra("top_background_color", call.argument<String?>("backgroundTopColor"))
        intent.putExtra("bottom_background_color", call.argument<String?>("backgroundBottomColor"))
        // Instantiate activity and verify it will resolve implicit intent
        context.grantUriPermission(
            "com.instagram.android",
            stickerImageFile,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        return if (context.packageManager.resolveActivity(intent, 0) != null) {
            activity.startActivity(intent)
            true
        } else {
            false
        }
    }

    private fun shareInstagramFeed(activity: Activity, call: MethodCall): Boolean {
        val context = activity.applicationContext
        val backgroundImage: String? = call.argument("imagePath")
        val backgroundImageFile = getImageUri(backgroundImage)

        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.instagram.android")
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, backgroundImageFile)

        context.grantUriPermission(
            "com.instagram.android",
            backgroundImageFile,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        return if (context.packageManager.resolveActivity(intent, 0) != null) {
            activity.startActivity(intent)
            true
        } else {
            false
        }
    }

    private fun shareFacebookStory(activity: Activity, call: MethodCall): Boolean {
        val context = activity.applicationContext
        val stickerImage: String? = call.argument("stickerImage")
        val backgroundTopColor: String? = call.argument("backgroundTopColor")
        val backgroundBottomColor: String? = call.argument("backgroundBottomColor")
        val attributionURL: String? = call.argument("attributionURL")

        val stickerImageFile = getImageUri(stickerImage)
        val intent = Intent("com.facebook.stories.ADD_TO_STORY")
        intent.type = "image/*"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("com.facebook.platform.extra.APPLICATION_ID", getAppId(context, call))
        intent.putExtra("interactive_asset_uri", stickerImageFile)
        intent.putExtra("content_url", attributionURL)
        intent.putExtra("top_background_color", backgroundTopColor)
        intent.putExtra("bottom_background_color", backgroundBottomColor)
        // Instantiate activity and verify it will resolve implicit intent
        context.grantUriPermission(
            "com.facebook.katana",
            stickerImageFile,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        return if (context.packageManager.resolveActivity(intent, 0) != null) {
            activity.startActivity(intent)
            true
        } else {
            false
        }
    }

    private fun shareOptions(activity: Activity, call: MethodCall): Boolean {
        val content: String? = call.argument("content")
        val image: String? = call.argument("image")
        val intent = Intent()
        intent.action = Intent.ACTION_SEND

        if (image != null) {
            //check if  image is also provided
            val imageFileUri = getImageUri(image)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, imageFileUri)
        } else {
            intent.type = "text/plain"
        }

        intent.putExtra(Intent.EXTRA_TEXT, content)

        //create chooser intent to launch intent
        //source: "share" package by flutter (https://github.com/flutter/plugins/blob/master/packages/share/)
        val chooserIntent: Intent = Intent.createChooser(intent, null /* dialog title optional */)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        activity.startActivity(chooserIntent)
        return true
    }

    private fun shareWhatsapp(activity: Activity, call: MethodCall): Boolean {
        val content: String? = call.argument("content")
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, content)
        return try {
            activity.startActivity(whatsappIntent)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    private fun shareSms(activity: Activity, call: MethodCall): Boolean {
        val content: String? = call.argument("message")
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setDataAndType(Uri.parse("sms:"), "vnd.android-dir/mms-sms")
        intent.putExtra("sms_body", content)
        return try {
            activity.startActivity(intent)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    private fun shareTwitter(activity: Activity, call: MethodCall): Boolean {
        val text: String? = call.argument("captionText")
        val url: String? = call.argument("url")
        val trailingText: String? = call.argument("trailingText")
        val urlScheme = "http://www.twitter.com/intent/tweet?text=$text$url$trailingText"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = Uri.parse(urlScheme)
        return try {
            activity.startActivity(intent)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    private fun shareTelegram(activity: Activity, call: MethodCall): Boolean {
        val content: String? = call.argument("content")
        val telegramIntent = Intent(Intent.ACTION_SEND)
        telegramIntent.type = "text/plain"
        telegramIntent.setPackage("org.telegram.messenger")
        telegramIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        telegramIntent.putExtra(Intent.EXTRA_TEXT, content)
        return try {
            activity.startActivity(telegramIntent)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    private fun checkInstalledApps(context: Context): Map<String, Boolean> {
        val packageNames = mapOf(
            "instagram" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "twitter" to "com.twitter.android",
            "whatsapp" to "com.whatsapp",
            "telegram" to "org.telegram.messenger",
        )

        val apps: MutableMap<String, Boolean> = mutableMapOf()
        val pm: PackageManager = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        packageNames.mapValuesTo(apps) { appEntry -> packages.any { it.packageName == appEntry.value } }

        //intent to check sms app exists
        val intent = Intent(Intent.ACTION_SENDTO).addCategory(Intent.CATEGORY_DEFAULT)
        intent.setDataAndType(Uri.parse("sms:"), "vnd.android-dir/mms-sms")
        val resolvedActivities: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        apps["sms"] = resolvedActivities.isNotEmpty()

        return apps
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val activity = activity
        if (activity == null) {
            result.error("no_context", null, null)
            return
        }
        val context = activity.applicationContext
        if (call.method == "checkInstalledApps") {
            result.success(checkInstalledApps(context))
        } else {
            val res = when (call.method) {
                "shareInstagramStory" -> {
                    shareInstagramStory(activity, call)
                }
                "shareInstagramFeed" -> {
                    shareInstagramFeed(activity, call)
                }
                "shareFacebookStory" -> {
                    shareFacebookStory(activity, call)
                }
                "shareWhatsapp" -> {
                    shareWhatsapp(activity, call)
                }
                "shareSms" -> {
                    shareSms(activity, call)
                }
                "shareTwitter" -> {
                    shareTwitter(activity, call)
                }
                "shareTelegram" -> {
                    shareTelegram(activity, call)
                }
                "shareOptions" -> {
                    shareOptions(activity, call)
                }
                "copyToClipboard" -> {
                    val content: String? = call.argument("content")
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("", content))
                    true
                }
                else -> {
                    result.notImplemented()
                    return
                }
            }
            if (res) {
                result.success("success")
            } else {
                result.success("error")
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}