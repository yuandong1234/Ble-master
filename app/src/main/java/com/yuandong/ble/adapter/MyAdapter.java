package com.yuandong.ble.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yuandong.ble.R;
import com.yuandong.ble.entity.Image;

import java.util.ArrayList;

/**
 * Created by yuandong on 2017/4/20.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>  {

    private ArrayList<Image> pics;
    private Context context;
    private LayoutInflater inflater;

    public MyAdapter(Context context, ArrayList<Image> pics) {
        this.context = context;
        this.pics = pics;
        this.inflater = LayoutInflater.from(context);
    }


    public void addItem(ArrayList<Image> data){
      if(data!=null){
          pics.addAll(data);
          notifyDataSetChanged();
      }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_item_recyclerview, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Image bean=pics.get(position);
        holder.image.setImageResource(bean.image);
        holder.value.setText(bean.name);
        holder.frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"点击了第 "+position+" 张图片", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if(pics!=null&&pics.size()>0){
            return pics.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout frame;
        ImageView image;
        TextView value;
        public ViewHolder(View itemView) {
            super(itemView);
            frame=(LinearLayout)itemView.findViewById(R.id.ll_frame);
            image= (ImageView)itemView.findViewById(R.id.image);
            value= (TextView)itemView.findViewById(R.id.value);
        }
    }

}
