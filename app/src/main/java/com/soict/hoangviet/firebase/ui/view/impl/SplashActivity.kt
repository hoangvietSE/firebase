package com.soict.hoangviet.firebase.ui.view.impl

import android.content.DialogInterface
import android.content.Intent
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.soict.hoangviet.baseproject.extension.hasNetworkConnection
import com.soict.hoangviet.firebase.ui.interactor.impl.SplashInteractorImpl
import com.soict.hoangviet.firebase.ui.presenter.MainPresenter
import com.soict.hoangviet.firebase.ui.presenter.SplashPresenter
import com.soict.hoangviet.firebase.ui.presenter.impl.SplashPresenterImpl
import com.soict.hoangviet.firebase.ui.view.SplashView
import com.soict.hoangviet.firebase.utils.DialogUtil
import javax.inject.Inject

class SplashActivity : BaseActivity(), SplashView {
    @Inject
    lateinit var mPresenter: SplashPresenter

    override fun initListener() {
    }

    companion object {
        const val SPLASH_TIME = 1500L
    }

    override fun initView() {
        mPresenter.onAttach(this)
        Handler().postDelayed({
            checkNetworkConnection()
        }, SPLASH_TIME)
    }

    private fun checkNetworkConnection() {
        if (hasNetworkConnection()) {
            checkCurrentUser()
        } else {
            showAlertNoNetworkConnection()
        }
    }

    private fun checkCurrentUser() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            mPresenter.saveCurrentUser()
        } else {
            goToLogin()
        }
    }

    override fun goToHomeScreen() {
        startActivity(Intent(this, MainActivity::class.java).apply {
        })
        finish()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
        })
        finish()
    }

    private fun showAlertNoNetworkConnection() {
        DialogUtil.showMessageDialog(
            this,
            "Warning!",
            "Không có kết nối Internet. Vui lòng quay lại khi có kết nối!",
            false,
            "Đồng ý",
            object : DialogUtil.BaseDialogInterface {
                override fun onPositiveClick(dialog: DialogInterface) {
                    finish()
                }

                override fun onNegativeClick(dialog: DialogInterface) {
                }

                override fun onItemClick(dialog: DialogInterface, position: Int) {
                }
            }
        )
    }

    override fun onFragmentAttached() {
    }

    override fun onFragmentDetached(tag: String) {
    }
}