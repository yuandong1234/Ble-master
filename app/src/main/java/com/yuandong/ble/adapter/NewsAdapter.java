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

import com.bumptech.glide.Glide;
import com.yuandong.ble.R;
import com.yuandong.ble.entity.Image;
import com.yuandong.ble.retrofit.entity.News;

import java.util.ArrayList;

/**
 * Created by yuandong on 2017/4/20.
 */

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder>  {

    private ArrayList<News> list;
    private Context context;
    private LayoutInflater inflater;

    public NewsAdapter(Context context, ArrayList<News> list) {
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }


    public void addItem(ArrayList<News> data){
      if(data!=null){
          list.addAll(data);
          notifyDataSetChanged();
      }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_item_news,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        News bean=list.get(position);
        holder.title.setText(bean.title);
        holder.time.setText(bean.date);
        holder.from.setText(bean.author_name);
        Glide.with(context).load(bean.thumbnail_pic_s).into(holder.image_01);
        Glide.with(context).load(bean.thumbnail_pic_s02).into(holder.image_02);
        Glide.with(context).load(bean.thumbnail_pic_s03).into(holder.image_03);
    }

    @Override
    public int getItemCount() {
        if(list!=null&&list.size()>0){
            return list.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image_01;
        ImageView image_02;
        ImageView image_03;
        TextView time;
        TextView from;
        public ViewHolder(View itemView) {
            super(itemView);
            title=(TextView)itemView.findViewById(R.id.title);
            image_01= (ImageView)itemView.findViewById(R.id.pic_01);
            image_02= (ImageView)itemView.findViewById(R.id.pic_02);
            image_03= (ImageView)itemView.findViewById(R.id.pic_03);
            time= (TextView)itemView.findViewById(R.id.time);
            from= (TextView)itemView.findViewById(R.id.from);
        }
    }

}
