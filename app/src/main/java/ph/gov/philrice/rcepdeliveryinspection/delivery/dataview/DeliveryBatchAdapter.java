package ph.gov.philrice.rcepdeliveryinspection.delivery.dataview;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ph.gov.philrice.rcepdeliveryinspection.R;

public class DeliveryBatchAdapter extends RecyclerView.Adapter<DeliveryBatchAdapter.ViewHolder> {

    private static final String TAG = "DeliveryBatchAdapter";
     Context c;
    public ArrayList<DeliveryBatchData> deliveryBatchData;
    private ItemClicked itemClickedListener;

    public DeliveryBatchAdapter(Context ctx, ArrayList<DeliveryBatchData> deliveryBatchData) {
        this.c = ctx;
        this.deliveryBatchData = deliveryBatchData;
    }

    //interface
    public interface ItemClicked {
        void onDelete(int tmpDeliveryBatchDataId/*value to return*/);
    }

    public void setitemClickedListener(ItemClicked itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_delivery_batch_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull DeliveryBatchAdapter.ViewHolder holder,  int pos) {
        //assigning value to variable
        String mBags = deliveryBatchData.get(pos).getTotalBagCount() + " bags";
        String mSeedTag;

        if (deliveryBatchData.get(pos).getBatchSeries().equals("")) {
            mSeedTag = deliveryBatchData.get(pos).getSeedTag();
        } else {
            mSeedTag = deliveryBatchData.get(pos).getSeedTag() + "(s." + deliveryBatchData.get(pos).getBatchSeries() + ")";
        }

        //set text to views
        holder.tv_seedTag.setText(mSeedTag);
        holder.tv_variety.setText(deliveryBatchData.get(pos).getSeedVariety());
        holder.tv_totalBags.setText(mBags);
        //adding listener
        holder.imgv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onDelete(deliveryBatchData.get(pos).getTmpDeliveryBatchDataId());
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return deliveryBatchData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_seedTag, tv_variety, tv_totalBags;
        ImageView imgv_remove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_seedTag = itemView.findViewById(R.id.tv_seedTag);
            this.tv_variety = itemView.findViewById(R.id.tv_variety);
            this.tv_totalBags = itemView.findViewById(R.id.tv_totalBags);
            this.imgv_remove = itemView.findViewById(R.id.imgv_remove);
        }
    }

}
