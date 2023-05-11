package io.agora.app.sample;

import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.agora.auikit.ui.basic.AUiAlertDialog;
import io.agora.auikit.ui.basic.AUiBottomDialog;


public class BasicUiListActivity extends AppCompatActivity {

    private int themeId = io.agora.auikit.R.style.Theme_AUIKit_Basic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(themeId);
        setContentView(R.layout.basic_ui_list_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(100, 1001, 0, "改变主题");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1001) {
            if (themeId == io.agora.auikit.R.style.Theme_AUIKit_Basic) {
                themeId = io.agora.auikit.R.style.Theme_AUIKit_Basic_Dark;
            } else {
                themeId = io.agora.auikit.R.style.Theme_AUIKit_Basic;
            }
            setTheme(themeId);
            setContentView(R.layout.basic_ui_list_activity);
        }
        return super.onOptionsItemSelected(item);
    }

    public void showBottomDialog01(View view) {
        AUiBottomDialog dialog = new AUiBottomDialog(this);
        dialog.setTitle("麦位管理");
        dialog.setMenu(R.menu.aui_bottom_dialog_menu_01);
        dialog.setOnItemClickListener(itemId -> Toast.makeText(BasicUiListActivity.this, "itemId=" + itemId, Toast.LENGTH_LONG).show());
        dialog.show();
    }

    public void showBottomDialog02(View view) {
        AUiBottomDialog dialog = new AUiBottomDialog(this);
        dialog.setTitle("麦位管理");
        dialog.setMenu(R.menu.aui_bottom_dialog_menu_02);
        dialog.setCustomView(R.layout.bottom_dialog_custom_layout);
        dialog.setOnItemClickListener(itemId -> Toast.makeText(BasicUiListActivity.this, "itemId=" + itemId, Toast.LENGTH_LONG).show());
        dialog.show();
    }

    public void showBottomDialog03(View view) {
        AUiBottomDialog dialog = new AUiBottomDialog(this);
        dialog.setTitle("变声");
        dialog.setMenu(R.menu.aui_bottom_dialog_menu_03);
        dialog.setItemOrientation(LinearLayout.HORIZONTAL);
        dialog.setCheckItemIds(new int[]{R.id.voice_origin, R.id.voice_konglin2, R.id.voice_dashu3});
        dialog.setListSingleCheck(true);
        dialog.setItemLayoutId(R.layout.bottom_dialog_custom_item);
        dialog.setOnItemCheckChangeListener((groupId, itemId, isChecked) -> Toast.makeText(BasicUiListActivity.this, "groupId=" + groupId + ", itemId=" + itemId + ", isChecked=" + isChecked, Toast.LENGTH_SHORT).show());
        dialog.show();
    }

    public void showAlertDialog01(View view) {
        AUiAlertDialog dialog = new AUiAlertDialog(this);
        dialog.setTitle("Confirm");
        dialog.setMessage("2023年2月14日，中国足球协会主席、党委副书记陈戌源涉嫌严重违纪违法，目前正接受中央纪委国家监委驻国家体育总局纪检监察组和湖北省监委审查调查。\n");
        dialog.setMessageGravity(Gravity.START);
        dialog.setMessageTextAlpha(0x80);
        dialog.setTitleCloseButton(v -> dialog.dismiss());
        dialog.setPositiveButton("确定", v -> dialog.dismiss());
        dialog.show();
    }

    public void showAlertDialog02(View view) {
        AUiAlertDialog dialog = new AUiAlertDialog(this);
        dialog.setMessage("2023年2月14日，中国足球协会主席、党委副书记陈戌源涉嫌严重违纪违法，目前正接受中央纪委国家监委驻国家体育总局纪检监察组和湖北省监委审查调查。\n");
        dialog.setPositiveButton("确定", v -> dialog.dismiss());
        dialog.show();
    }

    public void showAlertDialog03 (View view) {
        AUiAlertDialog dialog = new AUiAlertDialog(this);
        dialog.setTitle("Confirm");
        dialog.setMessage("2023年2月14日，中国足球协会主席、党委副书记陈戌源涉嫌严重违纪违法，目前正接受中央纪委国家监委驻国家体育总局纪检监察组和湖北省监委审查调查。\n");
        dialog.setMessageTextAlpha(0x80);
        dialog.setMessageGravity(Gravity.START);
        dialog.setTitleCloseButton(v -> dialog.dismiss());
        dialog.setNegativeButton("取消", v -> dialog.dismiss());
        dialog.setPositiveButton("确定", v -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    public void showAlertDialog04(View view) {
        AUiAlertDialog dialog = new AUiAlertDialog(this);
        dialog.setMessage("2023年2月14日，中国足球协会主席、党委副书记陈戌源涉嫌严重违纪违法，目前正接受中央纪委国家监委驻国家体育总局纪检监察组和湖北省监委审查调查。\n");
        dialog.setNegativeButton("取消", v -> dialog.dismiss());
        dialog.setPositiveButton("确定", v -> dialog.dismiss());
        dialog.show();
    }

    public void showAlertDialog05(View view) {
        AUiAlertDialog dialog = new AUiAlertDialog(this);
        dialog.setTitle("Confirm");
        dialog.setMessage("2023年2月14日，中国足球协会主席、党委副书记陈戌源涉嫌严重违纪违法，目前正接受中央纪委国家监委驻国家体育总局纪检监察组和湖北省监委审查调查。\n");
        dialog.setMessageTextAlpha(0x80);
        dialog.setMessageGravity(Gravity.START);
        dialog.setInput("请输入内容", "", true);
        dialog.setTitleCloseButton(v -> dialog.dismiss());
        dialog.setNegativeButton("取消", v -> dialog.dismiss());
        dialog.setPositiveButton("确定", v -> {
            Toast.makeText(BasicUiListActivity.this, dialog.getInputText(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
    }

    public void showAlertDialog06(View view) {
        AUiAlertDialog dialog = new AUiAlertDialog(this);
        dialog.setMessage("2023年2月14日，中国足球协会主席、党委副书记陈戌源涉嫌严重违纪违法，目前正接受中央纪委国家监委驻国家体育总局纪检监察组和湖北省监委审查调查。\n");
        dialog.setInput("请输入内容", "", true);
        dialog.setNegativeButton("取消", v -> dialog.dismiss());
        dialog.setPositiveButton("确定", v -> {
            Toast.makeText(BasicUiListActivity.this, dialog.getInputText(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
    }

    public void showAlertDialog07(View view) {
        AUiAlertDialog dialog = new AUiAlertDialog(this);
        dialog.setTitle("Confirm");
        dialog.setInput("请输入内容", "", false);
        dialog.setTitleCloseButton(v -> dialog.dismiss());
        dialog.setNegativeButton("取消", v -> dialog.dismiss());
        dialog.setPositiveButton("确定", v -> {
            Toast.makeText(BasicUiListActivity.this, dialog.getInputText(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
    }

    public void showAlertDialog08(View view) {
        AUiAlertDialog dialog = new AUiAlertDialog(this, io.agora.auikit.R.attr.aui_alertDialog_appearance_outline);
        dialog.setTitle("Confirm");
        dialog.setInput("请输入姓名", "", true);
        dialog.setTitleCloseButton(v -> dialog.dismiss());
        dialog.setNegativeButton("取消", v -> dialog.dismiss());
        dialog.setPositiveButton("确定", v -> {
            Toast.makeText(BasicUiListActivity.this, dialog.getInputText(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
    }

    public void showAlertDialog09(View view) {
        AUiAlertDialog dialog = new AUiAlertDialog(this);
        dialog.setTitle("Confirm");
        dialog.setInput("请输入内容", "", true);
        dialog.setTitleCloseButton(v -> dialog.dismiss());
        dialog.setPositiveButton("确定", v -> {
            Toast.makeText(BasicUiListActivity.this, dialog.getInputText(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
    }

    public void showAlertDialog10(View view) {
        AUiAlertDialog dialog = new AUiAlertDialog(this, io.agora.auikit.R.attr.aui_alertDialog_appearance_outline);
        dialog.setTitle("Confirm");
        dialog.setInput("请输入姓名", "", false);
        dialog.setTitleCloseButton(v -> dialog.dismiss());
        dialog.setPositiveButton("确定", v -> {
            Toast.makeText(BasicUiListActivity.this, dialog.getInputText(), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
    }


}
