package divdendusa.apps.mjk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder>{
    private ArrayList<Item> mItems;
    Context mContext;
    public RecyclerViewAdapter(ArrayList itemList) {
        mItems = itemList;
    }
    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        mContext = parent.getContext();
        RecyclerViewHolder holder = new RecyclerViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, final int position) {
        holder.mSeq.setText(mItems.get(position).seq);  //Log.i("position",""+mItems.get(position).seq+"position"+position);
        holder.mStockSymbol.setText(mItems.get(position).stockSymbol);    //Log.i("position",""+mItems.get(position).name+"position"+position);
        holder.mCompanyName.setText(mItems.get(position).companyName);  //Log.i("position",""+mItems.get(position).price+"position"+position);
        holder.mDividendYield.setText(mItems.get(position).dividendYield);  //Log.i("position",""+mItems.get(position).crmonth+"position"+position);
        holder.mClosingPrice.setText(mItems.get(position).closingPrice);    //Log.i("position",""+mItems.get(position).dividend+"position"+position);
        holder.mAnnualizedDividend.setText(mItems.get(position).annualizedDividend);  //Log.i("position",""+mItems.get(position).yield+"position"+position);
        holder.mExdivDate.setText(mItems.get(position).exdivDate);  //Log.i("position",""+mItems.get(position).dpratio+"position"+position);
        holder.mPayDate.setText(mItems.get(position).payDate);  //Log.i("position",""+mItems.get(position).roe+"position"+position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext, String.format("%d 선택", position + 1), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
