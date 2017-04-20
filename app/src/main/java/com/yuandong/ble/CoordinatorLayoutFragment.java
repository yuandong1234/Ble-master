package com.yuandong.ble;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.app.base.fragment.BaseFragment;
import com.yuandong.ble.adapter.MyAdapter;
import com.yuandong.ble.entity.Image;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by yuandong on 2017/4/19.
 */

public class CoordinatorLayoutFragment extends BaseFragment {
    public static final String ARGUMENT = "argument";
    private String title;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter adapter;
    private ArrayList<Image> pics;

    /**
     * 传入需要的参数，设置给arguments
     * @param argument
     * @return
     */
    public static CoordinatorLayoutFragment newInstance(String argument) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, argument);
        CoordinatorLayoutFragment contentFragment = new CoordinatorLayoutFragment();
        contentFragment.setArguments(bundle);
        return contentFragment;
    }
    @Override
    protected void initParams(Bundle params) {
        title=params.getString(ARGUMENT);
    }

    @Override
    protected int bindLayout() {
        return R.layout.layout_fragment_coordinatorlayout;
    }

    @Override
    protected void initView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        //创建默认的线性LayoutManager
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        recyclerView.setHasFixedSize(true);
        //创建并设置Adapter
        pics = new ArrayList<>();
        adapter = new MyAdapter(getActivity(), pics);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        ArrayList<Image> data = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Image image = new Image();
            image.image = R.mipmap.pic_01;
            image.name = "图片"+new Random().nextInt();
            data.add(image);
        }
        adapter.addItem(data);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }
}
