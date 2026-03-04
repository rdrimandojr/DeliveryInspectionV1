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

public class SamplingAdapter extends RecyclerView.Adapter<SamplingAdapter.ViewHolder> {

    private static final String TAG = "InspectionAdapter";
    private Context c;
    public ArrayList<SamplingData> samplingData;
    private ArrayList<SamplingData> filteredInspectionData;
    private InspectionFilter filter;
    private ItemClicked itemClickedListener;

    public SamplingAdapter(Context ctx, ArrayList<SamplingData> samplingData) {
        this.c = ctx;
        this.samplingData = samplingData;
    }

    //interface
    public interface ItemClicked {
        void onDelete(int position/*value to return*/);
    }

    public void setitemClickedListener(ItemClicked itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_sampling_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull SamplingAdapter.ViewHolder holder,  int pos) {
        String mBagWeight = "@\t" + samplingData.get(pos).getBagWeight() + "(kg)";

        holder.tv_bagSequence.setText(String.valueOf(samplingData.get(pos).getBagSequenceNumber()));
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
        return samplingData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_bagSequence, tv_weight;
        ImageView imgv_remove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_bagSequence = itemView.findViewById(R.id.tv_bagSequence);
            this.tv_weight = itemView.findViewById(R.id.tv_weight);
            this.imgv_remove = itemView.findViewById(R.id.imgv_remove);
        }
    }

}
