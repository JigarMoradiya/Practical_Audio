package com.jigar.audioplayer.ui

import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.jigar.audioplayer.R
import com.jigar.audioplayer.databinding.ActivityAudioRecorderBinding
import com.jigar.audioplayer.ui.model.AudioViewModel
import com.jigar.audioplayer.ui.model.AudioViewModelFactory
import com.jigar.audioplayer.utils.AppConstants
import com.jigar.audioplayer.utils.DateUtil
import com.jigar.audioplayer.utils.extensions.*
import com.jigar.audioplayer.utils.ui.audioview.util.Helper
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.io.File
import java.io.IOException


class AudioRecordActivity : AppCompatActivity(), KodeinAware {
    override val kodein by closestKodein()
    private lateinit var binding: ActivityAudioRecorderBinding
    private lateinit var viewModel: AudioViewModel
    private val viewModelFactory: AudioViewModelFactory by instance()

    private var currentOutFile: String? = null
    private var myAudioRecorder: MediaRecorder? = null
    private var isRecording = false

    private val REPEAT_INTERVAL = 40
    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null
    private var startTime = 0L

    private val handlerAudioRecording = Handler(Looper.getMainLooper())
    private var updateRunnable = Runnable { updateTextView() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAudioRecorderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViewModel()
        setUpInit()
        setUpListener()
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(AudioViewModel::class.java)
    }

    private fun setUpInit() {
        checkStoragePermission()
    }

    private fun setUpListener() {
        binding.cardPlayRecording.onClick {
            if (!currentOutFile.isNullOrEmpty()) {
                if (mediaPlayer == null) {
                    playAudio()
                } else if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                    stopMedia()
                } else if (mediaPlayer != null && mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                    binding.imgPlayPause.setImageResource(R.drawable.ic_baseline_pause_24)
                }
            }
        }
        binding.btnSaveFile.onClick {
            if (binding.edtFileName.text.isNullOrEmpty()) {
                toastS(getString(R.string.enter_file_name))
            } else {
                val newFilePath = Helper.RECORDING_PATH + "/" + binding.edtFileName.text + ".mp3"
                val newFile = File(newFilePath)
                if (newFile.exists()) {
                    toastS(context.getString(R.string.file_exist))
                } else {
                    val oldFile = File(currentOutFile ?: "")
                    oldFile.renameTo(newFile)
                    binding.linearBottomsheet.hide()
                    val returnIntent = Intent()
                    returnIntent.putExtra("filePath", newFile.path)
                    setResult(RESULT_OK, returnIntent)
                    finish()
                }
            }

        }
        binding.cardSaveRecording.onClick {
            binding.linearBottomsheet.show()

        }
        binding.cardStopContinueRecording.onClick {
            if (myAudioRecorder != null) {
                stopRecording()
            } else {
                myAudioRecorder?.stop()
                myAudioRecorder?.release()
                myAudioRecorder = null
                checkStoragePermission()
            }
        }
        binding.cardUndoRecording.onClick {
            val recording = File(currentOutFile ?: "")
            if (recording.exists() && recording.delete()) {
                toastS(context.getString(R.string.recording_deleted))
                finish()
            } else {
                toastS(context.getString(R.string.recording_deleted_fail))
            }
        }
    }

    /**
     * stop recording
     */
    private fun stopRecording() {
        try {
            if (myAudioRecorder != null) {
                myAudioRecorder?.stop()
                myAudioRecorder?.release()
                myAudioRecorder = null

                binding.grpRecordingComplete.show()
                binding.cardPlayRecording.show()
                binding.txtPlayRecordingMsg.show()
                binding.txtPlayRecordingTime.hide()
                binding.txtPlayRecordingTime.text = ""
                handlerAudioRecording.removeCallbacks(updateRunnable)
                binding.imgPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                binding.imgStopContinue.setImageResource(R.drawable.ic_baseline_keyboard_voice_24)
                binding.txtStopContinue.text = getString(R.string.continues)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toastS(getString(R.string.recording_fail))
        }

        isRecording = false

        handler.removeCallbacks(updateVisualizer)
    }

    /**
     * start recording
     */
    private fun startRecording() {
        mediaPlayer = null
        val recording = File(currentOutFile ?: "")
        if (recording.exists()) {
            recording.delete()
        }
        binding.grpRecordingComplete.hide()
        binding.cardPlayRecording.hide()
        binding.txtPlayRecordingMsg.hide()
        binding.imgStopContinue.setImageResource(R.drawable.ic_baseline_stop_recording)
        binding.txtStopContinue.text = getString(R.string.stop)
        binding.visualizerView.clear()
        if (Helper.helperInstance?.createRecordingFolder() == true) {
            currentOutFile = Helper.RECORDING_PATH + "/recording_" + DateUtil.getCurrentTimeStamp() + ".mp3"
            myAudioRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this@AudioRecordActivity)
            } else {
                MediaRecorder()
            }
            myAudioRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            myAudioRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            myAudioRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//            myAudioRecorder?.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
            myAudioRecorder?.setOutputFile(currentOutFile)
            try {
                startTime = System.currentTimeMillis()
                updateTextView()
                binding.txtPlayRecordingTime.show()
                myAudioRecorder?.prepare()
                myAudioRecorder?.start()
                toastS(getString(R.string.recording_start))
                isRecording = true
                handler.post(updateVisualizer)
            } catch (e: IllegalStateException) {
                toastS(getString(R.string.recording_fail))
                finish()
                e.printStackTrace()
                isRecording = false
            } catch (e: IOException) {
                toastS(getString(R.string.recording_fail))
                e.printStackTrace()
                isRecording = false
            }
        } else {
            toastS(getString(R.string.recording_fail))
            isRecording = false
        }
    }

    // updates the visualizer every 50 milliseconds
    private var updateVisualizer: Runnable = object : Runnable {
        override fun run() {
            if (isRecording) // if we are already recording
            {
                // get the current amplitude
                val x = myAudioRecorder!!.maxAmplitude
                binding.visualizerView.addAmplitude(x) // update the VisualizeView
                binding.visualizerView.invalidate() // refresh the VisualizerView


                // update in 40 milliseconds
                handler.postDelayed(this, REPEAT_INTERVAL.toLong())
            }
        }
    }

    /*
    * play recorded audio
    * */
    private fun playAudio() {
        mediaPlayer =
            MediaPlayer.create(this@AudioRecordActivity, Uri.fromFile(File(currentOutFile ?: "")))
        mediaPlayer?.start()
        binding.imgPlayPause.setImageResource(R.drawable.ic_baseline_pause_24)
        mediaPlayer?.setOnCompletionListener {
            binding.imgPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }

    // check permission
    private fun checkStoragePermission() {
        if (!isPermissionGrant(AppConstants.WRITE_EXTERNAL_STORAGE) || !isPermissionGrant(
                AppConstants.RECORD_AUDIO
            )
        ) {
            /**
             * Permission not given.
             */
            Dexter.withContext(this)
                .withPermissions(AppConstants.WRITE_EXTERNAL_STORAGE, AppConstants.RECORD_AUDIO)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0?.areAllPermissionsGranted() == true) {
                            startRecording()
                        } else {
                            // check for permanent denial of any permission
                            if (p0?.isAnyPermissionPermanentlyDenied == true) {
                                snackPermissionListener.onPermissionDenied(p0.deniedPermissionResponses.first())
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                }).check()
        } else {
            startRecording()
        }
    }

    val snackPermissionListener: PermissionListener by lazy {
        SnackbarOnDeniedPermissionListener.Builder
            .with(binding.root, getString(R.string.permission_needed))
            .withOpenSettingsButton(getString(R.string.settings))
            .withCallback(object : Snackbar.Callback() {
                override fun onShown(snackbar: Snackbar) {
                    // Event handler for when the given Snack is visible
                }

                override fun onDismissed(snackbar: Snackbar, event: Int) {
                    // Event handler for when the given Sandbar has been dismissed
                }
            }).build()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    override fun onPause() {
        super.onPause()
        stopMedia()
        if (isRecording) {
            stopRecording()
        }
    }

    private fun stopMedia() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            binding.imgPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }

    private fun updateTextView() {
        val duration: Long = ((System.currentTimeMillis() - startTime) / 1000)
        handlerAudioRecording.postDelayed(updateRunnable, 1000)
        binding.txtPlayRecordingTime.text = DateUtil.secToTime(duration)
    }

    override fun onBackPressed() {
        val recording = File(currentOutFile ?: "")
        if (recording.exists()) {
            recording.delete()
        }
        finish()
    }
}