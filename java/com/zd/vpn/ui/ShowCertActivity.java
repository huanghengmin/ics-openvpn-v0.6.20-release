package com.zd.vpn.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import com.zd.vpn.R;
import com.zd.vpn.base.BaseActivity;
import com.zd.vpn.base.TitleBar;
import com.zd.vpn.util.TitlebarFactory;


/**
 * 查看证书
 */
public class ShowCertActivity extends BaseActivity {

    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = findView(android.R.id.list);
        CertListAdapter adapter = new CertListAdapter(getContext());
        listView.setAdapter(adapter);
        adapter.setData(loadSettings());
        adapter.notifyDataSetChanged();
    }

    private List loadSettings() {
        List list = new ArrayList();

        SharedPreferences sharedPreferences = this.getSharedPreferences( "com.zd.vpn", Context.MODE_PRIVATE);

        String serialNumber = sharedPreferences.getString("vpn.serialNumber","");
        CertListItem item = new CertListItem("证书序列号",serialNumber.length() > 0 ? serialNumber : "信息不全");
        list.add(item);

        String subject = sharedPreferences.getString("vpn.subject", "");
         item = new CertListItem("主题", subject.length() > 0 ? subject: "信息不全");
        list.add(item);


        String issue = sharedPreferences.getString("vpn.issue", "");
        item = new CertListItem("签发者", issue.length() > 0 ? issue: "信息不全");
        list.add(item);

        String city = sharedPreferences.getString("vpn.notBefore", "");
        item = new CertListItem("于以下日期之前无效", city.length() > 0 ? city : "信息不全");
        list.add(item);

        String company = sharedPreferences.getString("vpn.notAfter", "");
        item = new CertListItem("于以下日期之后无效", company.length() > 0 ? company: "信息不全");
        list.add(item);

    /*    String department = sharedPreferences.getString("vpn.department", "");
        item = new CertListItem("OU(机构)", department.length() > 0 ? department
                : "信息不全");
        list.add(item);*/

        return list;
    }

    @Override
    public int rootViewRes() {
        return R.layout.cert;
    }

    @Override
    public TitleBar initTitlebar() {
        TitleBar titleBar = TitlebarFactory.createCustomBackTitlebar(this,"查看证书");
        return titleBar;
    }
}
