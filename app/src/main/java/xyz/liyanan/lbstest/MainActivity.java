package xyz.liyanan.lbstest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //创建一个LocationClient实例
    public LocationClient mlocationClient;
    private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //接收一个Context参数
        mlocationClient = new LocationClient(getApplicationContext());

        //调用registerLocationListener注册一个定位监听器，当获取到位置信息时，回调这个定位监听器
        mlocationClient.registerLocationListener(new MyLocationListener());

        //调用SDKInitializer的initialize函数进行初始化操作，获取全局的context，在setContentView方法前不然会报错
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.bmapView);
        //BaiduMap类作为地图的总控制器，调用mapView.getMap()获取baiduMap的实例
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        positionText = (TextView) findViewById(R.id.positon_text_view);

        //ACCESS_FINE_LOCATION，READ_PHONE_STATE，WRITE_EXTERNAL_STORAGE，ACCESS_COARSE_LOCATION需在onCreate函数中声明。
        //空的List集合中添加没有被授权的权限，然后转换为Array
        List<String> permissionList = new ArrayList<>();
        //ACCESS_FINE_LOCATION，ACCESS_COARSE_LOCATION属于同一个权限组，只需要申请一个就好了
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //ActivityCompat.requestPermissions一次性申请权限
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }

    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            //将经纬度封装在LatLng中
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            //将LatLng对象传入MapStatusUpdate
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
            baiduMap.animateMapStatus(update);
            //缩放经度是18，百度支持3～19
            update = MapStatusUpdateFactory.zoomTo(18f);
            baiduMap.animateMapStatus(update);
            //防止多次调用animateMapStatus
            isFirstLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        //将location的的经纬度封装在Builder中，
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        //将MyLocationData设置到setMyLocationData中。
        baiduMap.setMyLocationData(locationData);
    }


    private void requestLocation() {
        initLocation();
        mlocationClient.start();
    }

    //调用LocationClientOption的setScanSpan函数实现每2s更新一次当前位置
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //只允许使用GPS，默认为Hight_Accuracy(室外可变为)，SaveBattery则是只能用网络定位
        //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setScanSpan(2000);
        //表示需要获取当前的地址信息
        option.setIsNeedAddress(true);
        mlocationClient.setLocOption(option);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    //当活动被销毁时一定要用到LocationClient的stop（）方法不然会在后台消耗大量的电量
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mlocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    //循环每一个权限，有一个被拒绝则关闭当前程序
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    //当权限全部被通过的时候才能开始地理位置定位
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
            }
        }
    }


}
