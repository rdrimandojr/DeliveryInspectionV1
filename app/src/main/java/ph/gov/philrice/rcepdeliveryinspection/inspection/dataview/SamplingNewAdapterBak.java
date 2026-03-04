package ph.gov.philrice.rcepdeliveryinspection.inspection.dataview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ph.gov.philrice.rcepdeliveryinspection.R;

public class SamplingNewAdapterBak extends RecyclerView.Adapter<SamplingNewAdapterBak.ViewHolder> {

    private static final String TAG = "SamplingNewAdapter";
    private Context c;
    public ArrayList<SamplingNewData> samplingNewData;
    private ItemClicked itemClickedListener;

    public SamplingNewAdapterBak(Context ctx, ArrayList<SamplingNewData> samplingNewData) {
        this.c = ctx;
        this.samplingNewData = samplingNewData;
    }

    //interface
    public interface ItemClicked {
        void onDelete(int position/*value to return*/);
    }

    public void setitemClickedListener(SamplingNewAdapterBak.ItemClicked itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_samplingnew_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull SamplingNewAdapterBak.ViewHolder holder, int pos) {
        String mBagWeight = "@\t" + samplingNewData.get(pos).getBagWeight() + "(kg)";

        holder.tv_seedTag.setText(String.valueOf(samplingNewData.get(pos).getSeedTag()));
        holder.tv_weight.setText(mBagWeight);

        holder.imgv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onDelete(pos);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return samplingNewData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_seedTag, tv_weight;
        ImageView imgv_remove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_seedTag = itemView.findViewById(R.id.tv_seedTag);
            this.tv_weight = itemView.findViewById(R.id.tv_weight);
            this.imgv_remove = itemView.findViewById(R.id.imgv_remove);
        }
    }
}
