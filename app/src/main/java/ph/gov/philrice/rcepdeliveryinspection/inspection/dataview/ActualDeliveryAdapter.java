package ph.gov.philrice.rcepdeliveryinspection.inspection.dataview;

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

public class ActualDeliveryAdapter extends RecyclerView.Adapter<ActualDeliveryAdapter.ViewHolder> {

    private static final String TAG = "ActualDeliveryAdapter";
    private Context c;
    public ArrayList<ActualDeliveryData> actualDeliveryData;
    private ItemClicked itemClickedListener;

    public ActualDeliveryAdapter(Context ctx, ArrayList<ActualDeliveryData> actualDeliveryData) {
        this.c = ctx;
        this.actualDeliveryData = actualDeliveryData;
    }

    //interface
    public interface ItemClicked {
        void onDelete(int position/*value to return*/);
    }

    public void setitemClickedListener(ActualDeliveryAdapter.ItemClicked itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_actualdelivery_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ActualDeliveryAdapter.ViewHolder holder, int pos) {
        final int mpos = pos;
        //String mBagWeight = "@\t" + sa.get(pos).getBagWeight() + "(kg)";
   /*     String series = actualDeliveryData.get(pos).getBatchSeries().equals("") ? "no series" :
                actualDeliveryData.get(pos).getBatchSeries();*/


        String newseries = actualDeliveryData.get(pos).getQRValStart().equals("") ? "no series" :
                actualDeliveryData.get(pos).getQRStart() + "-" +
                        actualDeliveryData.get(pos).getQREnd();


        String mseries = "QR : \t" + actualDeliveryData.get(mpos).getQRValStart();
        String var = actualDeliveryData.get(pos).getSeedVariety() + "(" +
                actualDeliveryData.get(pos).getSeedTag() + ")";
        String msackCode = actualDeliveryData.get(pos).getSack_code().equals("") ? "none" :
                actualDeliveryData.get(pos).getSack_code();

        holder.tv_variety.setText(var);
        String mBagCount =
                "@ " + String.valueOf(actualDeliveryData.get(pos).getTotalBagCount()) + " bags";
        holder.tv_bagCount.setText(mBagCount);
        holder.tv_series.setText(mseries);

        String hasRLA = actualDeliveryData.get(pos).getHasRLA() == 1 ? "Copy on-hand" :
                "Copy not yet available";

        holder.tv_hasRLA.setText("RLA status : " + hasRLA);
        holder.tv_sackCode.setText("Sack code : " + msackCode);

        holder.imgv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onDelete(mpos);
                }
            }
        });

        holder.tv_remarks.setText(actualDeliveryData.get(pos).getRemarks());
    }


    @Override
    public int getItemCount() {
        return actualDeliveryData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_variety, tv_bagCount, tv_remarks, tv_series, tv_hasRLA, tv_sackCode;
        ImageView imgv_remove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_sackCode = itemView.findViewById(R.id.tv_sackCode);
            this.tv_hasRLA = itemView.findViewById(R.id.tv_hasRLA);
            this.tv_series = itemView.findViewById(R.id.tv_series);
            this.tv_variety = itemView.findViewById(R.id.tv_variety);
            this.tv_bagCount = itemView.findViewById(R.id.tv_bagCount);
            this.imgv_remove = itemView.findViewById(R.id.imgv_remove);
            this.tv_remarks = itemView.findViewById(R.id.tv_remarks);
        }
    }

}
