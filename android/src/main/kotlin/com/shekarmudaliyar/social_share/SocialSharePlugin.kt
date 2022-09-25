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
import java.lang.RuntimeException

class SocialSharePlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var activity: Activity? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "social_share")
        channel.setMethodCallHandler(this)
    }

    private fun getImageUri(imagePath: String?): Uri {
        val context = activity?.applicationContext ?: throw RuntimeException("Context unavailable")
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".com.shekarmudaliyar.social_share",
            File(imagePath)
        )
    }

    private fun shareInstagramStory(context: Context, call: MethodCall): Boolean {
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
        intent.putExtra("source_application", call.argument<String?>("sourceApplication"))
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
            context.startActivity(intent)
            true
        } else {
            false
        }
    }

    private fun shareInstagramFeed(context: Context, call: MethodCall): Boolean {
        // Create the new Intent using the 'Send' action.
        val backgroundImage: String? = call.argument("imagePath")
        //check if background image is also provided
        val backgroundImageFile = getImageUri(backgroundImage)

        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setPackage("com.instagram.android")
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, backgroundImageFile)

        // Instantiate activity and verify it will resolve implicit intent
        context.grantUriPermission(
            "com.instagram.android",
            backgroundImageFile,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        return if (context.packageManager.resolveActivity(intent, 0) != null) {
            context.startActivity(intent)
            true
        } else {
            false
        }
    }

    private fun shareFacebookStory(context: Context, call: MethodCall): Boolean {
        val stickerImage: String? = call.argument("stickerImage")
        val backgroundTopColor: String? = call.argument("backgroundTopColor")
        val backgroundBottomColor: String? = call.argument("backgroundBottomColor")
        val attributionURL: String? = call.argument("attributionURL")
        val appId: String? = call.argument("appId")

        val stickerImageFile = getImageUri(stickerImage)
        val intent = Intent("com.facebook.stories.ADD_TO_STORY")
        intent.type = "image/*"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("com.facebook.platform.extra.APPLICATION_ID", appId)
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
            context.startActivity(intent)
            true
        } else {
            false
        }
    }

    private fun shareOptions(context: Context, call: MethodCall): Boolean {
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

        context.startActivity(chooserIntent)
        return true
    }

    private fun shareWhatsapp(context: Context, call: MethodCall): Boolean {
        val content: String? = call.argument("content")
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, content)
        return try {
            context.startActivity(whatsappIntent)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    private fun shareSms(context: Context, call: MethodCall): Boolean {
        val content: String? = call.argument("message")
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.setDataAndType(Uri.parse("sms:"), "vnd.android-dir/mms-sms")
        intent.putExtra("sms_body", content)
        return try {
            context.startActivity(intent)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    private fun shareTwitter(context: Context, call: MethodCall): Boolean {
        val text: String? = call.argument("captionText")
        val url: String? = call.argument("url")
        val trailingText: String? = call.argument("trailingText")
        val urlScheme = "http://www.twitter.com/intent/tweet?text=$text$url$trailingText"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(urlScheme)
        return try {
            context.startActivity(intent)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    private fun shareTelegram(context: Context, call: MethodCall): Boolean {
        val content: String? = call.argument("content")
        val telegramIntent = Intent(Intent.ACTION_SEND)
        telegramIntent.type = "text/plain"
        telegramIntent.setPackage("org.telegram.messenger")
        telegramIntent.putExtra(Intent.EXTRA_TEXT, content)
        return try {
            context.startActivity(telegramIntent)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    private fun checkInstalledApps(context: Context): Map<String, Boolean> {
        val apps: MutableMap<String, Boolean> = mutableMapOf()
        val pm: PackageManager = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        //intent to check sms app exists
        val intent = Intent(Intent.ACTION_SENDTO).addCategory(Intent.CATEGORY_DEFAULT)
        intent.setDataAndType(Uri.parse("sms:"), "vnd.android-dir/mms-sms")
        val resolvedActivities: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        //if sms app exists
        apps["sms"] = resolvedActivities.isNotEmpty()
        //if other app exists
        apps["instagram"] =
            packages.any { it.packageName.toString().contentEquals("com.instagram.android") }
        apps["facebook"] =
            packages.any { it.packageName.toString().contentEquals("com.facebook.katana") }
        apps["twitter"] =
            packages.any { it.packageName.toString().contentEquals("com.twitter.android") }
        apps["whatsapp"] =
            packages.any { it.packageName.toString().contentEquals("com.whatsapp") }
        apps["telegram"] =
            packages.any { it.packageName.toString().contentEquals("org.telegram.messenger") }
        return apps
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val context = activity?.applicationContext
        if (context == null) {
            result.error("no_context", null, null)
            return
        }
        if (call.method == "checkInstalledApps") {
            result.success(checkInstalledApps(context))
        } else {
            val res = when (call.method) {
                "shareInstagramStory" -> {
                    shareInstagramStory(context, call)
                }
                "shareInstagramFeed" -> {
                    shareInstagramFeed(context, call)
                }
                "shareFacebookStory" -> {
                    shareFacebookStory(context, call)
                }
                "shareWhatsapp" -> {
                    shareWhatsapp(context, call)
                }
                "shareSms" -> {
                    shareSms(context, call)
                }
                "shareTwitter" -> {
                    shareTwitter(context, call)
                }
                "shareTelegram" -> {
                    shareTelegram(context, call)
                }
                "shareOptions" -> {
                    shareOptions(context, call)
                }
                "copyToClipboard" -> {
                    val content: String? = call.argument("content")
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("", content)
                    clipboard.primaryClip = clip
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