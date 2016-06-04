package com.ec.android.test.pulltozoomviewdemo;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.LayoutInflater;
import android.view.View;

import com.ecloud.pulltozoomview.ECPullToZoomScrollViewToolbarEx;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        //
        initToolbar();
        //
        loadViewForCode();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(R.drawable.icon_back);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadViewForCode() {
        final ECPullToZoomScrollViewToolbarEx scrollView = (ECPullToZoomScrollViewToolbarEx) findViewById(R.id.scroll_view);
        //
        View zoomView = LayoutInflater.from(this).inflate(R.layout.activity_test_zoom, null, false);
        final View contentView = LayoutInflater.from(this).inflate(R.layout.activity_test_content, null, false);
        //
        scrollView.setZoomView(zoomView);
        scrollView.setScrollContentView(contentView);
        //
        scrollView.setToolbar(mToolbar);
        scrollView.setToolbarInitBackgroundResource(android.R.color.transparent);
        ///
//        scrollView.setToolbarChangeBackgroundResource(R.drawable.top_bg);
        //高斯模糊
        Bitmap bitmap = ImageUtils.decodeSampledBitmapFromResource(getResources(), R.drawable.top_bg, 100, 100);

        bitmap = FastBlur.doBlur(bitmap, 100, false);

        BitmapDrawable changeDrawable = new BitmapDrawable(getResources(), bitmap);

        scrollView.setToolbarChangeBackgroundDrawable(changeDrawable);
        //
        /*
        scrollView.setOnScrollViewChangedOutSideListener(new ECPullToZoomScrollViewToolbarEx.OnScrollViewChangedOutSizeListener() {
            @Override
            public void onScrollChanged(int left, int top, int oldLeft, int oldTop) {

            }
        });
        */
        /*
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {palette.get
                Palette.Swatch vibrant = palette.getVibrantSwatch();
                if (vibrant != null) {
                    // If we have a vibrant color
                    // update the title TextView
                    ColorDrawable colorDrawable = new ColorDrawable(vibrant.getRgb());

                    scrollView.setToolbarChangeBackgroundDrawable(colorDrawable);
                }
            }
        });
        */
    }

    /**
     * Radius最大只能25f，效果不明显
     * RenderScript 高斯模糊
     *
     * @param bitmap
     * @return
     */
    public Bitmap blurBitmap(Bitmap bitmap) {
        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        //Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(getApplicationContext());

        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        //Set the radius of the blur
        blurScript.setRadius(25.0F);

        //Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        //Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        //recycle the original bitmap
        bitmap.recycle();

        //After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return outBitmap;
    }

}
