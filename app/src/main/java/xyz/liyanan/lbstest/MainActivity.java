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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //创建一个LocationClient实例
    public LocationClient mlocationClient;
    private TextView positionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //接收一个Context参数
        mlocationClient = new LocationClient(getApplicationContext());
        //调用registerLocationListener注册一个定位监听器，当获取到位置信息时，回调这个定位监听器
        mlocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_main);
        positionText = (TextView) findViewById(R.id.positon_text_view);
        //ACCESS_FINE_LOCATION，READ_PHONE_STATE，WRITE_EXTERNAL_STORAGE，ACCESS_COARSE_LOCATION需在onCreate函数中声明。
        //空的List集合中添加没有被授权的权限，然后转换为Array
        List<String> permissionList = new ArrayList<>();
        //ACCESS_FINE_LOCATION，ACCESS_COARSE_LOCATION属于同一个权限组，只需要申请一个就好了
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //ActivityCompat.requestPermissions一次性申请权限
        if(!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            requestLocation();
        }
    }

    //
    private void requestLocation() {
        initLocation();
        mlocationClient.start();
    }
    //调用LocationClientOption的setScanSpan函数实现每2s更新一次当前位置
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        //只允许使用GPS，默认为Hight_Accuracy(室外可变为)，SaveBattery则是只能用网络定位
        //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setScanSpan(2000);
        //表示需要获取当前的地址信息
        option.setIsNeedAddress(true);
        mlocationClient.setLocOption(option);
    }
    //当活动被销毁时一定要用到LocationClient的stop（）方法不然会在后台消耗大量的电量
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mlocationClient.stop();
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
        public void onReceiveLocation(BDLocation location){
            StringBuilder currentPosiontion = new StringBuilder();
            //获取纬度
            currentPosiontion.append("纬度").append(location.getLatitude()).append("\n");
            //获取经度
            currentPosiontion.append("经度").append(location.getLongitude()).append("\n");
            //获取国家
            currentPosiontion.append("国家").append(location.getCountry()).append("\n");
            //获取省
            currentPosiontion.append("省").append(location.getProvince()).append("\n");
            //获取市
            currentPosiontion.append("市").append(location.getCity()).append("\n");
            //获取区
            currentPosiontion.append("区").append(location.getDistrict()).append("\n");
            //获取街道
            currentPosiontion.append("街道").append(location.getStreet()).append("\n");
            currentPosiontion.append("定位方式");
            //获取定位方式
            if(location.getLocType()==BDLocation.TypeGpsLocation){
                currentPosiontion.append("GPS");
            }else if(location.getLocType()==BDLocation.TypeNetWorkLocation){
                currentPosiontion.append("网络");
            }
            positionText.setText(currentPosiontion);
        }
    }


}
