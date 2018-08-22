import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.skycracks.todo.base.IView
import com.skycracks.todo.core.bean.BaseResponse
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.JobCancellationException
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.text.SimpleDateFormat
import java.util.*

/**
 * Log
 */
fun loge(tag: String, content: String?) {
    Log.e(tag, content ?: tag)
}

/**
 * show toast
 * @param content String
 */
@SuppressLint("ShowToast")
fun Context.toast(content: String) {
    Constant.showToast?.apply {
        setText(content)
        show()
    } ?: run {
        Toast.makeText(this.applicationContext, content, Toast.LENGTH_SHORT).apply {
            Constant.showToast = this
        }.show()
    }
}

/**
 * show toast
 * @param content String
 */
@SuppressLint("ShowToast")
fun Fragment.toast(content: String) {
    Constant.showToast?.apply {
        setText(content)
        show()
    } ?: run {
        Toast.makeText(activity, content, Toast.LENGTH_SHORT).apply {
            Constant.showToast = this
        }.show()
    }
}

/**
 * show toast
 * @param id strings.xml
 */
fun Context.toast(@StringRes id: Int) {
    toast(getString(id))
}

fun Deferred<Any>?.cancelByActive() = this?.run {
    tryCatch {
        if (isActive) {
            cancel()
        }
    }
}

/**
 * save cookie string
 */
fun encodeCookie(cookies: List<String>): String {
    val sb = StringBuilder()
    val set = HashSet<String>()
    cookies
            .map { cookie ->
                cookie.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }
            .forEach {
                it.filterNot { set.contains(it) }.forEach { set.add(it) }
            }

    val ite = set.iterator()
    while (ite.hasNext()) {
        val cookie = ite.next()
        sb.append(cookie).append(";")
    }

    val last = sb.lastIndexOf(";")
    if (sb.length - 1 == last) {
        sb.deleteCharAt(last)
    }
    return sb.toString()
}

/**
 * LayoutInflater.from(this).inflate
 * @param resource layoutId
 * @return View
 */
fun Context.inflater(@LayoutRes resource: Int): View =
        LayoutInflater.from(this).inflate(resource, null)

/**
 * 格式化当前日期
 */
fun formatCurrentDate(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd")
    return formatter.format(Date())
}

/**
 * 关闭软键盘
 */
fun closeKeyBord(mEditText: EditText, mContext: Context) {
    val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(mEditText.windowToken, 0)
}


/**
 * In disappear assist cheng (cancel) will be submitted to the Job Cancellation Exception Exception.
 */
inline fun tryCatch(catchBlock: (Throwable) -> Unit = {}, tryBlock: () -> Unit) {
    try {
        tryBlock()
    } catch (_: JobCancellationException) {

    } catch (t: Throwable) {
        catchBlock(t)
    }
}

@Suppress("DeferredResultUnused")
fun <T> responseTransform(view: IView?, response: Deferred<BaseResponse<T>>?, transformBlock: (T) -> Unit = {}) {
    async(UI) {
        tryCatch({
            it.printStackTrace()
            view?.showError(Constant.RESULT_SERVER_ERROR)
        }) {
            val result = response?.await()
            view?.hideLoading()
            result ?: let {
                view?.showError(Constant.RESULT_NULL)
                return@async
            }
            if (result.errorCode != 0) {
                view?.showError(result.errorMsg)
            } else {
                transformBlock(result.data)
            }
        }
    }
}

@Suppress("DeferredResultUnused")
fun responseTransform(view: IView?, response: Deferred<BaseResponse<Any>>?, transformBlock: () -> Unit = {}) {
    async(UI) {
        tryCatch({
            it.printStackTrace()
            view?.showError(Constant.RESULT_SERVER_ERROR)
        }) {
            val result = response?.await()
            result ?: let {
                view?.showError(Constant.RESULT_NULL)
                return@async
            }
            if (result.errorCode != 0) {
                view?.showError(result.errorMsg)
            } else {
                transformBlock()
            }
        }
    }
}