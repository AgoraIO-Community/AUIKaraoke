package io.agora.app.karaoke

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import io.agora.app.karaoke.RoomListActivity.Companion.ThemeId
import io.agora.app.karaoke.databinding.SettingActivityBinding

class SettingActivity : AppCompatActivity(){
    private val mViewBinding by lazy { SettingActivityBinding.inflate(LayoutInflater.from(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        if (ThemeId != View.NO_ID) {
            setTheme(ThemeId)
        }
        setContentView(mViewBinding.root)

        val out = TypedValue()
        if (theme.resolveAttribute(android.R.attr.windowBackground, out, true)) {
            window.setBackgroundDrawableResource(out.resourceId)
        }

        val themeAdapter = ImageAdapter(getThemImages()) { position ->
            ThemeId = if (position == 1){
                io.agora.asceneskit.R.style.Theme_AKaraoke_KTV
            }else{
                io.agora.asceneskit.R.style.Theme_AKaraoke
            }
            theme.setTo(resources.newTheme())
            initView()
        }
        themeAdapter.setSelectedPosition(
            if (ThemeId == io.agora.asceneskit.R.style.Theme_AKaraoke_KTV) 1 else 0
        )
        mViewBinding.rvTheme.adapter = themeAdapter

        mViewBinding.btnComplete.setOnClickListener{
            finish()
        }
    }

    override fun finish() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }

    private fun getThemImages(): List<Int> {
        return listOf(R.mipmap.ic_sun, R.mipmap.ic_moon)
    }


    private inner class ImageAdapter(
        private val images: List<Int>,
        private val onImageSelected: (position: Int) -> Unit)
        : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
        private var selectedPosition = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.setting_tag_item, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder,position: Int) {
            val index = position
            holder.bind(images[index], index == selectedPosition)
            holder.itemView.setOnClickListener {
                val previouslySelectedPosition = selectedPosition
                selectedPosition = index
                notifyItemChanged(previouslySelectedPosition)
                notifyItemChanged(selectedPosition)
                onImageSelected(index)
            }
        }

        override fun getItemCount(): Int {
            return images.size
        }

        fun setSelectedPosition(position: Int){
            this.selectedPosition = position
            notifyDataSetChanged()
        }

        private inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.img_tag)

            fun bind(imageResId: Int, isSelected: Boolean) {
                imageView.setImageResource(imageResId)
                if (isSelected){
                    val drawable = resources.getDrawable(R.drawable.bg_setting_tag_select)
                    imageView.background = drawable
                }else{
                    val drawable = resources.getDrawable(R.drawable.bg_setting_tag)
                    imageView.background = drawable
                }
            }
        }
    }
}