package com.tencent.qcloud.tuikit.tuicallkit.view.component.function

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.tencent.qcloud.tuikit.TUICommonDefine
import com.tencent.qcloud.tuikit.TUICommonDefine.Camera
import com.tencent.qcloud.tuikit.tuicallengine.TUICallDefine
import com.tencent.qcloud.tuikit.tuicallengine.impl.base.Observer
import com.tencent.qcloud.tuikit.tuicallkit.R
import com.tencent.qcloud.tuikit.tuicallkit.view.component.videolayout.VideoViewFactory
import com.tencent.qcloud.tuikit.tuicallkit.view.root.BaseCallView
import com.tencent.qcloud.tuikit.tuicallkit.viewmodel.component.function.VideoCallerAndCalleeAcceptedViewModel

class VideoCallerAndCalleeAcceptedView(context: Context) : BaseCallView(context) {
    private var rootLayout: MotionLayout? = null
    private var imageOpenCamera: ImageView? = null
    private var imageMute: ImageView? = null
    private var imageHandsFree: ImageView? = null
    private var imageHangup: ImageView? = null
    private var imageSwitchCamera: ImageView? = null
    private var imageExpandView: ImageView? = null
    private var textMute: TextView? = null
    private var textHandsFree: TextView? = null
    private var textCamera: TextView? = null

    private var viewModel = VideoCallerAndCalleeAcceptedViewModel()

    private var isCameraOpenObserver = Observer<Boolean> {
        imageOpenCamera?.isActivated = it
    }

    private var isMicMuteObserver = Observer<Boolean> {
        imageMute?.isActivated = it
    }

    private var isSpeakerObserver = Observer<Boolean> {
        imageHandsFree?.isActivated = it
    }

    private val isBottomViewExpandedObserver = Observer<Boolean> {
        updateView(it)
    }

    init {
        initView()

        addObserver()
    }

    override fun clear() {
        removeObserver()
        viewModel.removeObserver()
    }

    private fun addObserver() {
        viewModel.isCameraOpen.observe(isCameraOpenObserver)
        viewModel.isMicMute.observe(isMicMuteObserver)
        viewModel.isSpeaker.observe(isSpeakerObserver)
        viewModel.isBottomViewExpanded.observe(isBottomViewExpandedObserver)
    }

    private fun removeObserver() {
        viewModel.isCameraOpen.removeObserver(isCameraOpenObserver)
        viewModel.isMicMute.removeObserver(isMicMuteObserver)
        viewModel.isSpeaker.removeObserver(isSpeakerObserver)
        viewModel.isBottomViewExpanded.removeObserver(isBottomViewExpandedObserver)
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.tuicallkit_function_view_video, this)
        rootLayout = findViewById(R.id.cl_view_video)
        imageMute = findViewById(R.id.iv_mute)
        textMute = findViewById(R.id.tv_mic)
        imageHandsFree = findViewById(R.id.iv_speaker)
        textHandsFree = findViewById(R.id.tv_speaker)
        imageOpenCamera = findViewById(R.id.iv_camera)
        imageHangup = findViewById(R.id.iv_hang_up)
        textCamera = findViewById(R.id.tv_video_camera)
        imageSwitchCamera = findViewById(R.id.iv_function_switch_camera)
        imageExpandView = findViewById(R.id.iv_expanded)
        imageExpandView?.visibility = INVISIBLE

        imageOpenCamera?.isActivated = viewModel.isCameraOpen.get() == true
        imageMute?.isActivated = viewModel.isMicMute.get() == true
        imageHandsFree?.isActivated = viewModel.isSpeaker.get() == true

        if (viewModel.scene.get() == TUICallDefine.Scene.SINGLE_CALL) {
            imageSwitchCamera?.visibility = VISIBLE
            rootLayout?.enableTransition(R.id.video_function_view_transition, false)
        } else {
            imageSwitchCamera?.visibility = GONE
        }

        if (!viewModel.isBottomViewExpanded.get()) {
            viewModel.updateView()
        }
        initViewListener()
    }

    private fun initViewListener() {
        imageMute?.setOnClickListener {
            val resId = if (viewModel.isMicMute.get() == true) {
                viewModel.openMicrophone()
                R.string.tuicallkit_toast_disable_mute
            } else {
                viewModel.closeMicrophone()
                R.string.tuicallkit_toast_enable_mute
            }
            textMute?.text = context.getString(resId)
        }
        imageHandsFree?.setOnClickListener {
            val resId = if (viewModel.isSpeaker.get() == true) {
                viewModel.selectAudioPlaybackDevice(TUICommonDefine.AudioPlaybackDevice.Earpiece)
                R.string.tuicallkit_toast_use_handset
            } else {
                viewModel.selectAudioPlaybackDevice(TUICommonDefine.AudioPlaybackDevice.Speakerphone)
                R.string.tuicallkit_toast_speaker
            }
            textHandsFree?.text = context.getString(resId)
        }
        imageOpenCamera?.setOnClickListener {
            val resId = if (viewModel.isCameraOpen.get() == true) {
                viewModel.closeCamera()
                R.string.tuicallkit_toast_disable_camera
            } else {
                if (VideoViewFactory.instance.videoEntityList.size > 0) {
                    viewModel.openCamera(viewModel.frontCamera.get())
                }
                R.string.tuicallkit_toast_enable_camera
            }
            textCamera?.text = context.getString(resId)
        }
        imageHangup?.setOnClickListener { viewModel.hangup() }

        imageExpandView?.setOnClickListener() {
            viewModel.updateView()
        }

        imageSwitchCamera?.setOnClickListener() {
            viewModel.switchCamera(if (viewModel.frontCamera.get() == true) Camera.Back else Camera.Front)
            ToastUtil.toastShortMessage(context.getString(R.string.tuicallkit_toast_switch_camera))
        }

        rootLayout?.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout, startId: Int, endId: Int) {
                rootLayout?.background = context.resources.getDrawable(R.drawable.tuicallkit_bg_group_call_bottom)
            }

            override fun onTransitionChange(motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float) {}

            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                rootLayout?.getConstraintSet(R.id.start)?.getConstraint(R.id.iv_expanded)?.propertySet?.visibility =
                    VISIBLE
            }

            override fun onTransitionTrigger(motionLayout: MotionLayout, id: Int, positive: Boolean, progress: Float) {}
        })
    }

    private fun updateView(isExpand: Boolean) {
        if (viewModel.scene?.get() == TUICallDefine.Scene.SINGLE_CALL) {
            return
        }
        if (isExpand) {
            rootLayout?.transitionToStart()
            rootLayout?.getConstraintSet(R.id.start)?.getConstraint(R.id.iv_expanded)?.propertySet?.visibility = VISIBLE
        } else {
            rootLayout?.transitionToEnd()
        }

    }
}