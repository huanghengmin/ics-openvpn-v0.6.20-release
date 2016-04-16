package com.zd.vpn.ui;



import com.zd.vpn.R;
import com.zd.vpn.base.BaseActivity;
import com.zd.vpn.base.TitleBar;
import com.zd.vpn.util.TitlebarFactory;

public class AboutActivity extends BaseActivity {

    @Override
    public int rootViewRes() {
        return R.layout.about;
    }

    @Override
    public TitleBar initTitlebar() {
        TitleBar titleBar = TitlebarFactory.createCustomBackTitlebar(this,
                "关于");
        return titleBar;
    }

}
