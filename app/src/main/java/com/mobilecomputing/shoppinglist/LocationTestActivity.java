package com.mobilecomputing.shoppinglist;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.EditText;

public class LocationTestActivity extends Activity {
    // 定义LocationManager对象
    private LocationManager locationManager;
    private EditText show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps);
        show = (EditText) findViewById(R.id.main_et_show);
        // 获取系统LocationManager服务
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 从GPS获取最近的定位信息
        Location location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // 将location里的位置信息显示在EditText中
        updateView(location);
        // 设置每2秒获取一次GPS的定位信息
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000, 8, new LocationListener() {

                    @Override
                    public void onLocationChanged(Location location) {
                        // 当GPS定位信息发生改变时，更新位置
                        updateView(location);
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        updateView(null);
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        // 当GPS LocationProvider可用时，更新位置
                        updateView(locationManager
                                .getLastKnownLocation(provider));

                    }

                    @Override
                    public void onStatusChanged(String provider, int status,
                                                Bundle extras) {
                    }
                });
    }

    private void updateView(Location location) {
        if (location != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("实时的位置信息：\n经度：");
            sb.append(location.getLongitude());
            sb.append("\n纬度：");
            sb.append(location.getLatitude());
            sb.append("\n高度：");
            sb.append(location.getAltitude());
            sb.append("\n速度：");
            sb.append(location.getSpeed());
            sb.append("\n方向：");
            sb.append(location.getBearing());
            sb.append("\n精度：");
            sb.append(location.getAccuracy());
            show.setText(sb.toString());
        } else {
            // 如果传入的Location对象为空则清空EditText
            show.setText("");
        }
    }

}