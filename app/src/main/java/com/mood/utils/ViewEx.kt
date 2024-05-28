package com.mood.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mood.R
import com.mood.base.RateAppDialog
import java.util.Calendar
import java.util.GregorianCalendar

@Suppress("DEPRECATION")
fun Activity.setFullScreenMode(isFullScreen: Boolean = false) {
    if (isFullScreen) {
        if (isSdkR()) {
            val controller = window.insetsController

            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    } else {
        if (isSdkR()) {
            val controller = window.insetsController

            if (controller != null) {
                controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }
}

fun Activity.setDarkMode(enable: Boolean = false) {
    val mode = if (enable) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    AppCompatDelegate.setDefaultNightMode(mode)
    recreate()
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.disableView() {
    this.isClickable = false
    this.postDelayed({ this.isClickable = true }, 300)
}

class SafeClickListener(val onSafeClickListener: (View) -> Unit) : View.OnClickListener {
    override fun onClick(v: View) {
        v.disableView()
        onSafeClickListener(v)
    }
}

fun View.setOnSafeClick(onSafeClickListener: (View) -> Unit) {
    val safeClick = SafeClickListener {
        onSafeClickListener(it)
    }
    setOnClickListener(safeClick)
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.showOrGone(isShow: Boolean) {
    if (isShow) {
        this.show()
    } else {
        this.gone()
    }
}

fun RecyclerView.setGridManager(
    mContext: Context,
    lin: Int,
    adapter: RecyclerView.Adapter<*>,
    orientation: Int = RecyclerView.VERTICAL,
) {
    val manager = GridLayoutManager(mContext, lin)
    manager.orientation = orientation
    this.layoutManager = manager
    this.adapter = adapter
}

fun RecyclerView.setLinearLayoutManager(
    context: Context,
    adapter: RecyclerView.Adapter<*>,
    orientation: Int = RecyclerView.VERTICAL
) {
    val manager = LinearLayoutManager(context)
    manager.orientation = orientation
    this.layoutManager = manager
    this.adapter = adapter
}

fun Context.openActivity(pClass: Class<out Activity>, bundle: Bundle?) {
    val intent = Intent(this, pClass)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
}

fun Fragment.openActivity(pClass: Class<out Activity>, isFinish: Boolean = false, bundle: Bundle? = null) {
    requireContext().openActivity(pClass, isFinish, bundle)
}

fun Context.openActivity(pClass: Class<out Activity>, isFinish: Boolean = false) {
    openActivity(pClass, null)
    if (isFinish) {
        (this as Activity).finish()
    }
}

fun Context.openActivity(pClass: Class<out Activity>, isFinish: Boolean = false, bundle: Bundle?) {
    openActivity(pClass, bundle)
    if (isFinish) {
        (this as Activity).finish()
    }
}

fun Context.showToast(msg: String, isShowDurationLong: Boolean = false) {
    val duration = if (isShowDurationLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, msg, duration).show()
}

fun Fragment.showToast(msg: String, isShowDurationLong: Boolean = false) {
    requireContext().showToast(msg, isShowDurationLong)
}

fun Context.getDrawableIdByName(name: String): Int {
    return resources.getIdentifier(name.split(".").last(), "drawable", packageName)
}

fun Context.inflateLayout(layoutResource: Int, parent: ViewGroup): View {
    return LayoutInflater.from(this).inflate(layoutResource, parent, false)
}

fun Context.shareApp() {
    val subject = "Let go to record your emoji today!!"
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    val shareBody = "https://play.google.com/store/apps/details?id=$packageName"
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
    this.startActivity(Intent.createChooser(sharingIntent, "Share to"))
}

fun Context.navigateToMarket() {
    val market = "market://details?id="
    val webPlayStore = "https://play.google.com/store/apps/details?id="
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(market + packageName)
                //market://details?id=<package_name>
            )
        )
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(webPlayStore + packageName)
                //https://play.google.com/store/apps/details?id=<package_name>
            )
        )
    }
}

fun Context.sendEmail(toEmail: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    val data = Uri.parse(
        "mailto:"
                + toEmail
                + "?subject=" + getString(R.string.feed_back) + "&body=" + ""
    )
    intent.data = data
    try {
        startActivity(intent)
    } catch (ex: Exception) {
        Toast.makeText(
            this,
            "Not have email app to send email!",
            Toast.LENGTH_SHORT
        ).show()
        ex.printStackTrace()
    }
}

fun TextView.clear() {
    this.text = ""
}

fun View.loadBitmapFromView(done: (Bitmap) -> Unit) {
    post {
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        layout(left, top, right, bottom)
        draw(c)
        done(b)
    }
}
fun View.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}
fun View.setSize(width: Int, height: Int) {
    val rootParam = this.layoutParams
    rootParam.width = width
    rootParam.height = height
    layoutParams = rootParam
    requestLayout()
}

fun Context.loadImage(
    imageView: ImageView,
    url: String,
    error: Int = R.drawable.ic_bean_type_default
) {
    Glide.with(this).load(url)
        .fitCenter()
        .placeholder(error)
        .into(imageView)
}

fun Context.loadImage(
    imageView: ImageView,
    url: Uri,
    error: Int = R.drawable.ic_bean_type_default
) {
    Glide.with(this).load(url)
        .fitCenter()
        .placeholder(error)
        .into(imageView)

}

fun Context.loadImage(
    imageView: ImageView,
    url: Int,
    error: Int = R.drawable.ic_bean_type_default
) {
    Glide.with(this).load(url)
        .fitCenter()
        .placeholder(error)
        .into(imageView)

}

fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == GRAND_PERMISSION
}

fun Context.hasReadStoragePermission() = checkReadImagePermission()
fun Fragment.hasReadStoragePermission() = requireContext().hasReadStoragePermission()
fun Fragment.getActivityResultLauncher(callBack: (Map<String, Boolean>) -> Unit): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        callBack.invoke(permissions)
    }
}

fun ComponentActivity.getActivityResultLauncher(callBack: (Map<String, Boolean>) -> Unit): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        callBack.invoke(permissions)
    }
}

fun Context.requestPermissionReadStorage(permissionLauncher: ActivityResultLauncher<Array<String>>) {
    val isReadPermissionGranted = hasReadStoragePermission()

    val permissionRequest = mutableListOf<String>()
    val permission = if (isSdk33()) {
        READ_MEDIA_IMAGES
    } else {
        READ_EXTERNAL_STORAGE
    }
    if (!isReadPermissionGranted) {
        permissionRequest.add(permission)
    }
    if (permissionRequest.isNotEmpty()) {
        permissionLauncher.launch(permissionRequest.toTypedArray())
    }
}

fun Fragment.requestPermissionReadStorage(permissionLauncher: ActivityResultLauncher<Array<String>>) {
    requireContext().requestPermissionReadStorage(permissionLauncher)
}

fun NestedScrollView.scrollToBottom() {
    this.post {
        fullScroll(View.FOCUS_DOWN)
    }
}

fun View.isSoftKeyboardVisible(): Boolean {
    val rect = Rect()
    rootView.getWindowVisibleDisplayFrame(rect)
    val screenHeight = rootView.height
    val keyboardHeight = screenHeight - rect.bottom
    val threshold = screenHeight * 0.15 // Adjust this value as per your requirements
    return keyboardHeight > threshold
}

fun Context.showDialogRate(onRate: (isLike: Boolean) -> Unit, onClose: (() -> Unit)? = null) {
    RateAppDialog(this).also { dialog ->
        dialog.show(onClickSubmit = { isLike ->
            onRate.invoke(isLike)
        }, onClickClose = {
            onClose?.invoke()
        })
    }
}

fun Context.showDialogRating() {
    Constant.isShowRateApp = false
    SharePrefUtils.saveKey(Constant.COUNT_OPEN_APP, 0)
    showDialogRate(onRate = { isLike ->
        if (isLike) {
            SharePrefUtils.saveKey(Constant.IS_SHOW_DIALOG, false)
            navigateToMarket()
        } else {
            val gc = GregorianCalendar().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            gc.add(Calendar.DATE, 3)
            val time = gc.timeInMillis
            SharePrefUtils.saveKey(Constant.TIME_TO_REPEAT_RATE, time)
        }
    }, onClose = {
        val gc = GregorianCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        gc.add(Calendar.DATE, 1)
        val time = gc.timeInMillis
        SharePrefUtils.saveKey(Constant.TIME_TO_REPEAT_RATE, time)
    })
}

fun Fragment.navigateToMarket() {
    requireContext().navigateToMarket()
}