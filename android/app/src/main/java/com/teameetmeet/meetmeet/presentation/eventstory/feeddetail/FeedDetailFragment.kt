package com.teameetmeet.meetmeet.presentation.eventstory.feeddetail

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.teameetmeet.meetmeet.R
import com.teameetmeet.meetmeet.data.model.Comment
import com.teameetmeet.meetmeet.databinding.FragmentFeedDetailBinding
import com.teameetmeet.meetmeet.presentation.base.BaseFragment
import com.teameetmeet.meetmeet.presentation.model.EventAuthority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FeedDetailFragment :
    BaseFragment<FragmentFeedDetailBinding>(R.layout.fragment_feed_detail),
    ContentEventListener,
    CommentDeleteClickListener {

    @Inject
    lateinit var mediaSourceFactory: DefaultMediaSourceFactory

    @Inject
    lateinit var progressiveMediaSourceFactory: ProgressiveMediaSource.Factory

    private val viewModel: FeedDetailViewModel by viewModels()
    private val navArgs: FeedDetailFragmentArgs by navArgs()

    private val videoPlayers = hashMapOf<Int, ExoPlayer>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setFeedId(navArgs.feedId)
        viewModel.setFeedAuthority(navArgs.authority)
        setBinding()
        setCallBacks()
        setTopAppBar()
        collectViewModelEvent()
    }

    private fun collectViewModelEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.feedDetailEvent.collect {
                    when (it) {
                        is FeedDetailEvent.ShowMessage -> {
                            showMessage(it.messageId, it.extraMessage)
                        }

                        is FeedDetailEvent.FinishFeedDetail -> {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getFeedDetail()
        with(videoPlayers[viewModel.feedDetailUiState.value.contentPage]) {
            this?.playWhenReady = true
            this?.play()
        }
    }

    override fun onPause() {
        super.onPause()
        with(videoPlayers[viewModel.feedDetailUiState.value.contentPage]) {
            this?.pause()
            this?.playWhenReady = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoPlayers.values.forEach {
            it.stop()
            it.clearMediaItems()
            it.release()
        }
    }

    private fun setBinding() {
        with(binding) {
            vm = viewModel
            feedDetailVpMedia.adapter = FeedContentsAdapter(this@FeedDetailFragment)
            feedDetailRvComment.adapter =
                FeedCommentsAdapter(navArgs.authority, this@FeedDetailFragment)
            feedDetailClComment.isVisible = navArgs.authority != EventAuthority.GUEST
        }
    }

    private fun setCallBacks() {
        with(binding) {
            feedDetailVpMedia.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    with(videoPlayers[viewModel.feedDetailUiState.value.contentPage]) {
                        this?.pause()
                        this?.playWhenReady = false
                    }
                    with(videoPlayers[position]) {
                        this?.playWhenReady = true
                        this?.play()
                    }
                    videoPlayers[position]?.play()

                    viewModel.setContentPage(position)
                }
            })

            feedDetailIbCommentSend.setOnClickListener {
                viewModel.addComment()
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.feedDetailEtComment.windowToken, 0)
                feedDetailNsv.fullScroll(View.FOCUS_DOWN)
                feedDetailRvComment.scrollToPosition(
                    feedDetailRvComment.adapter?.itemCount?.minus(1) ?: 0
                )
            }
        }
    }

    private fun setTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_delete_feed_detail -> showFeedDeleteConfirmationDialog()
            }
            true
        }
    }

    private fun showFeedDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.feed_detail_delete_event_confirm_dialog_title))
            .setMessage(getString(R.string.feed_detail_delete_event_confirm_dialog_message))
            .setPositiveButton(R.string.story_detail_delete_event_confirm_dialog_description_delete) { _, _ ->
                viewModel.deleteFeed()
            }
            .setNegativeButton(R.string.story_detail_delete_event_dialog_description_delete_cancel) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun onClick() {
        viewModel.feedDetailUiState.value.feedDetail?.contents ?: return
        findNavController().navigate(
            FeedDetailFragmentDirections.actionFeedDetailFragmentToFeedContentFragment(
                viewModel.feedDetailUiState.value.feedDetail!!.contents.toTypedArray(),
                viewModel.feedDetailUiState.value.contentPage
            )
        )
    }

    override fun getPlayer(position: Int): ExoPlayer {
        return ExoPlayer
            .Builder(requireContext())
            .setMediaSourceFactory(mediaSourceFactory)
            .build().also {
                videoPlayers[position] = it
            }
    }

    @OptIn(UnstableApi::class)
    override fun getMediaSource(path: String): MediaSource {
        val mediaItem = MediaItem.Builder()
            .setCustomCacheKey(path)
            .setUri(Uri.parse(path))
            .build()
        return progressiveMediaSourceFactory.createMediaSource(mediaItem)
    }

    override fun onClick(comment: Comment) {
        Snackbar
            .make(
                binding.root,
                getString(R.string.feed_comment_delete_event_confirm_message),
                Snackbar.LENGTH_SHORT
            ).setAction(R.string.story_detail_delete_event_confirm_dialog_description_delete) {
                viewModel.deleteComment(comment)
            }.show()
    }
}