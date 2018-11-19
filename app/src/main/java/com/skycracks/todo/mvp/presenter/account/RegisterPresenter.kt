package com.skycracks.todo.mvp.presenter.account

import cancelByActive
import com.skycracks.todo.base.BasePresenter
import com.skycracks.todo.core.bean.BaseResponse
import com.skycracks.todo.core.bean.LoginBean
import com.skycracks.todo.core.http.HttpHelperImpl
import com.skycracks.todo.mvp.contract.account.RegisterContract
import kotlinx.coroutines.Deferred
import responseTransform

class RegisterPresenter : BasePresenter<RegisterContract.View>() ,RegisterContract.Presenter{

    private var registerAsync: Deferred<BaseResponse<LoginBean>>? = null

    override fun registerWanAndroid(username: String, password: String, repassowrd: String) {
        registerAsync?.cancelByActive()
        registerAsync = HttpHelperImpl.registerWanAndroid(username,password,repassowrd)
        obtainView()?.run {
           responseTransform(registerAsync){
               registerSuccess(it)
           }
        }
    }

    override fun cancelRequest() {
        registerAsync?.cancelByActive()
    }
}