package com.jigar.audioplayer.ui

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.jigar.audioplayer.R
import com.jigar.audioplayer.adapter.AudioListAdapter
import com.jigar.audioplayer.databinding.ActivityAudioListMainBinding
import com.jigar.audioplayer.datamodel.AudioDataModel
import com.jigar.audioplayer.ui.model.AudioViewModel
import com.jigar.audioplayer.ui.model.AudioViewModelFactory
import com.jigar.audioplayer.utils.AppConstants
import com.jigar.audioplayer.utils.AppConstants.MIME_TYPE_AUDIO
import com.jigar.audioplayer.utils.FileUtil
import com.jigar.audioplayer.utils.extensions.hide
import com.jigar.audioplayer.utils.extensions.isPermissionGrant
import com.jigar.audioplayer.utils.extensions.onClick
import com.jigar.audioplayer.utils.extensions.toastS
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.io.File
import java.util.*

class AudioListActivity : AppCompatActivity(), KodeinAware, AudioListAdapter.OnItemClickListener {
    override val kodein by closestKodein()
    private lateinit var binding: ActivityAudioListMainBinding
    private lateinit var viewModel: AudioViewModel
    private val viewModelFactory: AudioViewModelFactory by instance()

    private lateinit var audioListAdapter: AudioListAdapter
    private var mediaPlayer : MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAudioListMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViewModel()
        setUpInit()
        setUpListener()
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(AudioViewModel::class.java)
    }

    private fun setUpInit() {
        audioListAdapter = AudioListAdapter(arrayListOf(), this)
        binding.recyclerView.adapter = audioListAdapter
    }

    private fun setUpListener() {
        binding.cardImportFile.onClick {
            checkStoragePermission()
        }
        binding.cardRecord.onClick {
            val intent = Intent(this@AudioListActivity,AudioRecordActivity::class.java)
            recordActivityResultLauncher.launch(intent)
        }
    }

    // check permission
    private fun checkStoragePermission() {
        if (!isPermissionGrant(AppConstants.READ_EXTERNAL_STORAGE)) {
            /**
             * Permission not given.
             */
            Dexter.withContext(this)
                .withPermission(AppConstants.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        selectResumeIntent()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        snackPermissionListener.onPermissionDenied(response)
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?) {
                        token?.continuePermissionRequest()
                        shouldShowRequestPermissionRationale(AppConstants.READ_EXTERNAL_STORAGE)
                    }
                }).check()
        } else {
            selectResumeIntent()
        }
    }

    /**
     * file select Intent
     */
    private fun selectResumeIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val mimetypes = arrayOf(MIME_TYPE_AUDIO)
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        resumeActivityResultLauncher.launch(intent)
    }

    /**
     * Activity Result For Resume Result
     */
    private var resumeActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                val uri: Uri? = activityResult.data?.data
                val filePath = FileUtil.getFilePathFromUri(this, uri)
                if (!filePath.isNullOrEmpty()) {
                    addFileToList(filePath)
                }else{
                    toastS(getString(R.string.filepath_not_found))
                }

            }
        }

    private fun addFileToList(filePath: String) {
        val file = File(filePath)
        val date = Date(file.lastModified())
        val totalSecond = FileUtil.getAudioFilePathDuration(filePath)
        val model = AudioDataModel()
        model.run {
            this.filePath = filePath
            name = file.name
            time = date
            duration = totalSecond
        }
        audioListAdapter.addItem(model)
        binding.imgAudioNoData.hide()
    }

    private var recordActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                val filePath = activityResult.data?.getStringExtra("filePath")
                if (!filePath.isNullOrEmpty()){
                    addFileToList(filePath)
                }
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
    override fun onItemPlayStopClick(position: Int,isPlayClick : Boolean) {
        if (isPlayClick){
            val previousPlayPosition = audioListAdapter.currPositionPlaying
            audioListAdapter.currPositionPlaying = position
            if (previousPlayPosition != position){ // stop previous item music and start new
                if (previousPlayPosition != -1){
                    val viewHolder = binding.recyclerView.findViewHolderForAdapterPosition(previousPlayPosition)
                    if (viewHolder == null){
                        audioListAdapter.notifyItemChanged(previousPlayPosition)
                    }else{
                        (viewHolder as AudioListAdapter.ViewHolder).itemBinding.isPlaying = false
                    }
                }
                if (mediaPlayer != null){
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
                mediaPlayer = MediaPlayer.create(this, Uri.fromFile(File(audioListAdapter.listData[position].filePath?:"")))
            }

            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                updateStopAction()
                lifecycleScope.launch {
                    delay(500)
                    audioListAdapter.currPositionPlaying = -1
                }
            }
        }else{
            stopMedia()
        }
    }

    private fun updateStopAction() {
        val pos = audioListAdapter.currPositionPlaying
        val viewHolder = binding.recyclerView.findViewHolderForAdapterPosition(pos)
        if (viewHolder == null){
            audioListAdapter.notifyItemChanged(pos)
        }else{
            (viewHolder as AudioListAdapter.ViewHolder).itemBinding.isPlaying = false
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    override fun onPause() {
        super.onPause()
        stopMedia()
    }

    private fun stopMedia(){
        if (mediaPlayer?.isPlaying == true){
            mediaPlayer?.pause()
            updateStopAction()
        }
    }

}