package com.liangyang.photopickerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.foamtrace.photopicker.ImageCaptureManager;
import com.foamtrace.photopicker.PhotoPickerActivity;
import com.foamtrace.photopicker.PhotoPreviewActivity;
import com.foamtrace.photopicker.SelectModel;
import com.foamtrace.photopicker.intent.PhotoPickerIntent;
import com.foamtrace.photopicker.intent.PhotoPreviewIntent;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 图库选择器--DemoTest
 */
public class MainActivity extends AppCompatActivity {

    private Button btnMuilt; // 多选
    private Button btnSingle; // 单选
    private Button btnCarema; // 拍照

    private static final int REQUEST_CAMERA_CODE = 11;
    private static final int REQUEST_PREVIEW_CODE = 22;
    private ArrayList<String> imagePaths = null;// 照片地址
    private GridView gridView;
    private int columnWidth;//列宽

    private ImageCaptureManager captureManager; // 相机拍照处理类
    private GridAdapter gridAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化视图
        btnMuilt = (Button) findViewById(R.id.btnMuilt);//多选
        btnSingle = (Button) findViewById(R.id.btnSingle);//单选
        btnCarema = (Button) findViewById(R.id.btnCarema);//拍照
        gridView = (GridView) findViewById(R.id.gridView);//照片墙

        //获取屏幕参数
        int cols = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().densityDpi;
        cols = cols < 3 ? 3 : cols;
        gridView.setNumColumns(cols);

        // Item Width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;//屏幕宽度
        int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);//列间距2dp
        columnWidth = (screenWidth - columnSpace * (cols-1)) / cols;//列宽

        /**
         * preview：预览图(gridView的点击监听事件)
         */
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoPreviewIntent photoPreviewIntent = new PhotoPreviewIntent(MainActivity.this);
                //当前选中照片的下标
                photoPreviewIntent.setCurrentItem(position);
                //已选中的照片地址
                photoPreviewIntent.setPhotoPaths(imagePaths);
                startActivityForResult(photoPreviewIntent,REQUEST_PREVIEW_CODE);
            }
        });

        /**
         * 单选Button的点击监听事件-只选一张照片功能
         */
        btnSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoPickerIntent photoPickerIntent = new PhotoPickerIntent(MainActivity.this);
                photoPickerIntent.setSelectModel(SelectModel.SINGLE);//选择模式：单选
                photoPickerIntent.setShowCarema(true);//是否显示拍照， 默认false
                startActivityForResult(photoPickerIntent,REQUEST_CAMERA_CODE);
            }
        });

        /**
         * 多选Button的点击监听事件-多选照片功能
         */
        btnMuilt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoPickerIntent photoPickerIntent = new PhotoPickerIntent(MainActivity.this);
                photoPickerIntent.setSelectModel(SelectModel.MULTI);//选择模式：多选
                photoPickerIntent.setShowCarema(true);//是否显示拍照，默认为false
                photoPickerIntent.setMaxTotal(6);//最多选择照片数量
                photoPickerIntent.setSelectedPaths(imagePaths);// 已选中的照片地址， 用于回显选中状态
                startActivityForResult(photoPickerIntent,REQUEST_CAMERA_CODE);
            }
        });

        /**
         * 拍照Button的点击监听事件-拍照功能
         */
        btnCarema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (captureManager == null){
                        captureManager = new ImageCaptureManager(MainActivity.this);
                    }
                    Intent intent = captureManager.dispatchTakePictureIntent();
                    startActivityForResult(intent,ImageCaptureManager.REQUEST_TAKE_PHOTO);
                } catch (IOException e) {
                    //无法启动系统相机
                    Toast.makeText(MainActivity.this, com.foamtrace.photopicker.R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * ActivityResult
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch (requestCode){
                //选择照片
                case REQUEST_CAMERA_CODE:
                    loadAdpater(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT));
                    break;

                //预览
                case REQUEST_PREVIEW_CODE:
                    loadAdpater(data.getStringArrayListExtra(PhotoPreviewActivity.EXTRA_RESULT));
                    break;

                //调用相机拍照
                case ImageCaptureManager.REQUEST_TAKE_PHOTO:
                    if (captureManager.getCurrentPhotoPath() != null){
                        captureManager.galleryAddPic();
                        ArrayList<String> paths = new ArrayList<>();
                        paths.add(captureManager.getCurrentPhotoPath());//照片地址
                        loadAdpater(paths);
                    }
                    break;
            }
        }
    }

    /**
     * 处理返回照片地址
     * @param paths
     */
    private void loadAdpater(ArrayList<String> paths){
        if(imagePaths == null){
            imagePaths = new ArrayList<>();
        }
        imagePaths.clear();
        imagePaths.addAll(paths);

        try{
            JSONArray obj = new JSONArray(imagePaths);
            Log.e("--", obj.toString());
        }catch (Exception e){
            e.printStackTrace();
        }

        if(gridAdapter == null){
            gridAdapter = new GridAdapter(imagePaths);
            gridView.setAdapter(gridAdapter);
        }else {
            gridAdapter.notifyDataSetChanged();
        }
    }

    /**
     * GridView的适配器
     */
    private class GridAdapter extends BaseAdapter{
        private ArrayList<String> listUrls;

        public GridAdapter(ArrayList<String> listUrls) {
            this.listUrls = listUrls;
        }

        @Override
        public int getCount() {
            return listUrls.size();
        }

        @Override
        public String getItem(int position) {
            return listUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.item_image, null);
                imageView = (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(imageView);
                // 重置ImageView宽高
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(columnWidth, columnWidth);
                imageView.setLayoutParams(params);
            }else {
                imageView = (ImageView) convertView.getTag();
            }
            //图片加载库
            Glide.with(MainActivity.this)
                    .load(new File(getItem(position)))
                    .placeholder(R.mipmap.default_error)
                    .error(R.mipmap.default_error)
                    .centerCrop()
                    .crossFade()
                    .into(imageView);
            return convertView;
        }
    }



}
