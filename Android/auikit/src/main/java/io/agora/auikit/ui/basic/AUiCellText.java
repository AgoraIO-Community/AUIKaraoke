package io.agora.auikit.ui.basic;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import io.agora.auikit.R;

public class AUiCellText extends ConstraintLayout {

    private TextView tvTitle, tvImportantMark, tvInfo, tvSubTitle, tvRedDot;
    private ImageView ivIndicator;
    private View vDivider;

    public AUiCellText(@NonNull Context context) {
        this(context, null);
    }

    public AUiCellText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUiCellText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.style.AUiCellText);

        initView();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AUiCellText, defStyleAttr, R.style.AUiCellText);

        tvTitle.setTextColor(typedArray.getColor(R.styleable.AUiCellText_aui_cellText_info_textColor, 0));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, typedArray.getDimension(R.styleable.AUiCellText_aui_cellText_title_textSize, 0));
        tvTitle.setText(typedArray.getText(R.styleable.AUiCellText_aui_cellText_info_text));

        String infoText = typedArray.getString(R.styleable.AUiCellText_aui_cellText_info_text);
        if (infoText == null || infoText.isEmpty()) {
            tvInfo.setVisibility(View.GONE);
        } else {
            tvInfo.setVisibility(View.VISIBLE);
            tvInfo.setTextColor(typedArray.getColor(R.styleable.AUiCellText_aui_cellText_info_textColor, 0));
            tvInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, typedArray.getDimension(R.styleable.AUiCellText_aui_cellText_title_textSize, 0));
            tvInfo.setText(infoText);
        }

        boolean important = typedArray.getBoolean(R.styleable.AUiCellText_aui_cellText_is_important, false);
        setItemImportant(important);

        String subtitleText = typedArray.getString(R.styleable.AUiCellText_aui_cellText_subtitle_text);
        if (subtitleText == null || subtitleText.isEmpty()) {
            tvSubTitle.setVisibility(View.GONE);
        } else {
            tvSubTitle.setVisibility(View.VISIBLE);
            tvSubTitle.setTextColor(typedArray.getColor(R.styleable.AUiCellText_aui_cellText_info_textColor, 0));
            tvSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, typedArray.getDimension(R.styleable.AUiCellText_aui_cellText_title_textSize, 0));
            tvSubTitle.setText(subtitleText);
        }

        boolean indicatorShow = typedArray.getBoolean(R.styleable.AUiCellText_aui_cellText_indicator_show, false);
        if (indicatorShow) {
            ivIndicator.setVisibility(View.GONE);
        } else {
            ivIndicator.setVisibility(View.VISIBLE);
            ivIndicator.setImageResource(typedArray.getResourceId(R.styleable.AUiCellText_aui_cellText_indicator_icon, 0));
        }
    }

    private void initView() {
        View.inflate(getContext(), R.layout.aui_celltext_layout, this);
        tvTitle = findViewById(R.id.tvTitle);
        tvImportantMark = findViewById(R.id.tvImportantMark);
        tvInfo = findViewById(R.id.tvInfo);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        tvRedDot = findViewById(R.id.tvRedDot);

        ivIndicator = findViewById(R.id.ivIndicator);
        vDivider = findViewById(R.id.vDivider);
    }

    public void setDotCount(int count) {
        if (count == 0) {
            tvRedDot.setVisibility(View.GONE);
        } else {
            tvRedDot.setText(String.format("%d", count));
            tvRedDot.setVisibility(View.VISIBLE);
        }
    }

    public void setShowIndicator(boolean isShow) {
        ivIndicator.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void setItemImportant(boolean important) {
        ivIndicator.setVisibility(important ? View.VISIBLE : View.GONE);
    }

}
