package com.soict.hoangviet.firebase.ui.presenter.impl

import android.net.Uri
import android.provider.SyncStateContract.Helpers.update
import android.text.TextUtils
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.soict.hoangviet.firebase.application.BaseApplication
import com.soict.hoangviet.firebase.data.network.request.UpdateProfileRequest
import com.soict.hoangviet.firebase.data.network.response.User
import com.soict.hoangviet.firebase.data.sharepreference.AppSharePreference
import com.soict.hoangviet.firebase.ui.interactor.UpdateProfileInteractor
import com.soict.hoangviet.firebase.ui.presenter.UpdateProfilePresenter
import com.soict.hoangviet.firebase.ui.view.UpdateProfileView
import com.soict.hoangviet.firebase.utils.ToastUtil
import org.greenrobot.eventbus.EventBus

class UpdateProfilePresenterImpl(mView: UpdateProfileView, mInteractor: UpdateProfileInteractor) :
    BasePresenterImpl<UpdateProfileView, UpdateProfileInteractor>(mView, mInteractor), UpdateProfilePresenter {
    private var mAvatarUpload: Uri? = null
    private var mGenderPosition: Int? = null
    private var mAvatarUploadUrl: String? = null
    private var storageRef: StorageReference =
        FirebaseStorage.getInstance().reference.child("images")

    override fun updateProfile(mUpdateProfileRequest: UpdateProfileRequest) {
        if (TextUtils.isEmpty(mUpdateProfileRequest.fullname)) {
            mView?.onFullNameEmpty()
            return
        }
        if (TextUtils.isEmpty(mUpdateProfileRequest.birthday)) {
            mView?.onBirthdayEmpty()
            return
        }
        mView?.showLoading()
        if (mAvatarUpload != null) {
            storageRef.child(AppSharePreference.getInstance(BaseApplication.instance).getUser().id + ".jpg")
                .putFile(mAvatarUpload!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    storageRef.downloadUrl
                }
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        updateProfileUser(mUpdateProfileRequest)
                        mAvatarUploadUrl = it.result!!.toString()
                    } else {
                        mView?.hideLoading()
                        mView?.onAvatarUploadError()
                    }
                }
                .addOnFailureListener {
                    mView?.hideLoading()
                    mView?.onAvatarUploadError()
                }
        } else {
            updateProfileUser(mUpdateProfileRequest)
        }
    }

    override fun setAvatar(uriImage: Uri) {
        mAvatarUpload = uriImage
    }

    override fun setGender(position: Int) {
        mGenderPosition = position
    }

    private fun updateProfileUser(mUpdateProfileRequest: UpdateProfileRequest) {
        val userRecored: MutableMap<String, Any> = mutableMapOf()
        userRecored.put("id", AppSharePreference.getInstance(BaseApplication.instance).getUser().id)
        userRecored.put("fullname", mUpdateProfileRequest.fullname)
        userRecored.put("email", mUpdateProfileRequest.email)
        userRecored.put("phone", mUpdateProfileRequest.phone)
        userRecored.put("birthday", mUpdateProfileRequest.birthday)
        userRecored.put("gender", mGenderPosition ?: 0)
        userRecored.put("avatar", mAvatarUploadUrl ?: "")
        datebaseRef?.setValue(userRecored)
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    mView?.hideLoading()
                    val mUser: User = AppSharePreference.getInstance(BaseApplication.instance).getUser()
                    mUser.fullname = mUpdateProfileRequest.fullname
                    mUser.birthday = mUpdateProfileRequest.birthday
                    mUser.gender = mGenderPosition ?: 0
                    mUser.avatar = mAvatarUploadUrl ?: ""
                    AppSharePreference.getInstance(BaseApplication.instance).setUser(mUser)
                    EventBus.getDefault().postSticky(mUser)
                    mView?.uploadSuccess()
                } else {
                    mView?.hideLoading()
                    mView?.uploadError()
                }
            }
            ?.addOnFailureListener {
                mView?.hideLoading()
                mView?.uploadError()
            }
    }
}