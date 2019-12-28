package divdendusa.apps.mjk;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class RecyclerViewHolder extends RecyclerView.ViewHolder {
    public TextView mSeq;
    public TextView mStockSymbol;
    public TextView mCompanyName;
    public TextView mDividendYield;
    public TextView mClosingPrice;
    public TextView mAnnualizedDividend;
    public TextView mExdivDate;
    public TextView mPayDate;
    public RecyclerViewHolder(View itemView) {
        super(itemView);
        mSeq = (TextView) itemView.findViewById(R.id._SEQ);
        mStockSymbol = (TextView) itemView.findViewById(R.id.STOCKSYMBOL);
        mCompanyName = (TextView) itemView.findViewById(R.id.COMPANYNAME);
        mDividendYield = (TextView) itemView.findViewById(R.id.DIVIDENDYIELD);
        mClosingPrice = (TextView) itemView.findViewById(R.id.CLOSINGPRICE);
        mAnnualizedDividend = (TextView) itemView.findViewById(R.id.ANNUALIZEDDIVIDEND);
        mExdivDate = (TextView) itemView.findViewById(R.id.EXDIVDATE);
        mPayDate = (TextView) itemView.findViewById(R.id.PAYDATE);

    }
}
