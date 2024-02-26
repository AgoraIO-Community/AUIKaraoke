package io.agora.app.karaoke

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import io.agora.app.karaoke.databinding.RoomListActivityBinding
import io.agora.app.karaoke.databinding.RoomListItemBinding
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.ui.basic.AUIAlertDialog
import io.agora.auikit.ui.basic.AUISpaceItemDecoration
import io.agora.auikit.utils.BindingViewHolder
import java.util.UUID

class RoomListActivity : AppCompatActivity() {

    private var viewBinding: RoomListActivityBinding? = null
    private val listAdapter by lazy { RoomListAdapter() }
    private var mList = listOf<AUIRoomInfo>()

    private val launcher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                theme.setTo(resources.newTheme())
                initView()
            }
        }

    companion object {
        var ThemeId = io.agora.asceneskit.R.style.Theme_AKaraoke
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }


    private fun initView() {
        setTheme(ThemeId)
        val viewBinding = RoomListActivityBinding.inflate(LayoutInflater.from(this))
        this.viewBinding = viewBinding
        setContentView(viewBinding.root)

        val isDarkTheme = ThemeId != io.agora.asceneskit.R.style.Theme_AKaraoke
        var systemUiVisibility: Int = window.decorView.systemUiVisibility
        if (isDarkTheme) {
            systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }else{
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.decorView.systemUiVisibility = systemUiVisibility
        window.setBackgroundDrawable(ColorDrawable(if(isDarkTheme) Color.parseColor("#171A1C") else Color.parseColor("#F9FAFA")))
        viewBinding.tvEmptyList.setCompoundDrawables(
            null,
            getDrawable(
                if (isDarkTheme) R.mipmap.ic_empty_dark else R.mipmap.ic_empty_light
            ).apply {
                this?.setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            },
            null,
            null
        )

        viewBinding.rvList.addItemDecoration(
            AUISpaceItemDecoration(
                resources.getDimensionPixelSize(R.dimen.room_list_item_space_h),
                resources.getDimensionPixelSize(R.dimen.room_list_item_space_v)
            )
        )
        viewBinding.rvList.adapter = listAdapter

        viewBinding.swipeRefresh.setOnRefreshListener {
            refreshRoomList()
        }
        viewBinding.btnCreateRoom.setOnClickListener {
            AUIAlertDialog(this@RoomListActivity).apply {
                setTitle("房间主题")
                setInput("房间主题", RandomUtils.randomRoomName(), true)
                setPositiveButton("一起嗨歌") {
                    dismiss()
                    createRoom(inputText)
                }
                show()
            }
        }
        viewBinding.btnSetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            launcher.launch(intent)
        }

        fetchRoomList()
    }

    private fun createRoom(roomName: String) {
        val roomInfo = AUIRoomInfo()
        roomInfo.roomId = UUID.randomUUID().toString()
        roomInfo.roomName = roomName
        roomInfo.thumbnail = RandomUtils.randomAvatar()
        roomInfo.owner = AUIRoomContext.shared().currentUserInfo

        RoomActivity.launch(this, true, roomInfo, ThemeId)
    }

    private fun refreshRoomList() {
        val viewBinding = this.viewBinding ?: return
        viewBinding.swipeRefresh.isRefreshing = true
        listAdapter.loadingMoreState = LoadingMoreState.Loading
        fetchRoomList()
    }

    private fun loadMore() {
        listAdapter.loadingMoreState = LoadingMoreState.Loading
        fetchRoomList()
    }

    private fun fetchRoomList() {
        val viewBinding = this.viewBinding ?: return
        var lastCreateTime: Long? = null
        if (!viewBinding.swipeRefresh.isRefreshing) {
            mList.lastOrNull()?.let {
                lastCreateTime = it.createTime
            }
        }
        KaraokeUIKit.getRoomList(lastCreateTime, 10,
            success = { roomList ->
                if (roomList.size < 10) {
                    listAdapter.loadingMoreState = LoadingMoreState.NoMoreData
                } else {
                    listAdapter.loadingMoreState = LoadingMoreState.Normal
                }
                mList = if (viewBinding.swipeRefresh.isRefreshing) { // 下拉刷新则重新设置数据
                    roomList
                } else {
                    val temp = mutableListOf<AUIRoomInfo>()
                    temp.addAll(mList)
                    temp.addAll(roomList)
                    temp
                }
                runOnUiThread {
                    viewBinding.swipeRefresh.isRefreshing = false
                    listAdapter.submitList(mList)
                    viewBinding.tvEmptyList.visibility = if (mList.isEmpty()) View.VISIBLE else View.GONE
                }
            },
            failure = {
                runOnUiThread {
                    viewBinding.swipeRefresh.isRefreshing = false
                    listAdapter.loadingMoreState = LoadingMoreState.Normal
                    Toast.makeText(
                        this@RoomListActivity,
                        "Fetch room list failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }


    enum class LoadingMoreState {
        Normal,
        Loading,
        NoMoreData,
    }

    inner class RoomListAdapter :
        ListAdapter<AUIRoomInfo, BindingViewHolder<RoomListItemBinding>>(object :
            ItemCallback<AUIRoomInfo>() {

            override fun areItemsTheSame(oldItem: AUIRoomInfo, newItem: AUIRoomInfo) =
                oldItem.roomId == newItem.roomId

            override fun areContentsTheSame(
                oldItem: AUIRoomInfo,
                newItem: AUIRoomInfo
            ) = false
        }) {

        var loadingMoreState: LoadingMoreState = LoadingMoreState.NoMoreData

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ) =
            BindingViewHolder(RoomListItemBinding.inflate(LayoutInflater.from(parent.context)))

        override fun onBindViewHolder(
            holder: BindingViewHolder<RoomListItemBinding>,
            position: Int
        ) {
            val item = getItem(position)
            holder.binding.tvRoomName.text = item.roomName
            holder.binding.tvRoomOwner.text = item.owner?.userName ?: "unKnowUser"
            holder.binding.tvMember.text = "${item.memberCount}人正在嗨歌"
            holder.binding.root.setOnClickListener {
                RoomActivity.launch(this@RoomListActivity, false, item, ThemeId)
            }
            Glide.with(holder.binding.ivAvatar)
                .load(item.owner?.userAvatar)
                .into(holder.binding.ivAvatar)
            if (loadingMoreState == LoadingMoreState.Normal && itemCount > 0 && position == itemCount - 1) {
                this@RoomListActivity.loadMore()
            }
        }
    }

}