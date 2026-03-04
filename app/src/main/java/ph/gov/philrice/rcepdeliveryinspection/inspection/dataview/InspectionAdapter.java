package ph.gov.philrice.rcepdeliveryinspection.inspection.dataview;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import ph.gov.philrice.rcepdeliveryinspection.R;


public class InspectionAdapter extends RecyclerView.Adapter<InspectionAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "InspectionAdapter";
    private Context c;
    public ArrayList<InspectionData> inspectionData;
    private ArrayList<InspectionData> filteredInspectionData;
    private InspectionFilter filter;
    private ItemClicked itemClickedListener;

    public InspectionAdapter(Context ctx, ArrayList<InspectionData> inspectionData) {
        this.c = ctx;
        this.inspectionData = inspectionData;
        this.filteredInspectionData = inspectionData;
    }

    //interface
    public interface ItemClicked {
        void onInspect(String ticketNumber/*value to return*/);

        void onViewDetails(String ticketNumber/*value to return*/);

        void onConfirmInspection(String ticketNumber/*value to return*/);
    }

    public void setitemClickedListener(ItemClicked itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_inspection_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull InspectionAdapter.ViewHolder holder,  int pos) {
        holder.tv_ticketNumber.setText(inspectionData.get(pos).getTicketNumber());
        holder.tv_seedTag.setText(inspectionData.get(pos).getSeedTag());
        holder.tv_variety.setText(inspectionData.get(pos).getSeedVariety());
        holder.tv_deliveryDate.setText(inspectionData.get(pos).getDeliveryDate());
        holder.tv_deliveryAddress.setText(inspectionData.get(pos).getDeliverTo());
        holder.tv_totWeight.setText(inspectionData.get(pos).getTotalWeight());
        holder.tv_weightPerPack.setText(inspectionData.get(pos).getWeightPerBag());
        //totalbags computation
        Float totWeight = Float.valueOf(inspectionData.get(pos).getTotalWeight());
        Float bagWeight = Float.valueOf(inspectionData.get(pos).getWeightPerBag());
        Float totBags = totWeight / bagWeight;

        holder.tv_expectedBag.setText(String.valueOf(Math.round(totBags)));

        String mStatus = inspectionData.get(pos).getStatus();
        switch (mStatus) {
            case "0":
              /*  if (Fun.hasInpsection(c, inspectionData.get(pos).getTicketNumber()) == 1) {
                    holder.tv_status.setText(R.string.pending);
                    holder.tv_inspect.setVisibility(View.GONE);
                    holder.tv_confirmInspection.setVisibility(View.VISIBLE);
                    holder.tv_viewDetails.setVisibility(View.GONE);
                } else {
                    holder.tv_status.setText(R.string.pending);
                    holder.tv_inspect.setVisibility(View.VISIBLE);
                    holder.tv_confirmInspection.setVisibility(View.GONE);
                    holder.tv_viewDetails.setVisibility(View.GONE);
                }*/
                break;
            case "1":
                holder.tv_status.setText(R.string.passed);
                holder.tv_confirmInspection.setVisibility(View.GONE);
                holder.tv_viewDetails.setVisibility(View.GONE);
                holder.tv_inspect.setVisibility(View.GONE);
                break;
            case "2":
                holder.tv_status.setText(R.string.rejected);
                holder.tv_confirmInspection.setVisibility(View.GONE);
                holder.tv_viewDetails.setVisibility(View.GONE);
                holder.tv_inspect.setVisibility(View.GONE);
                break;
            default:
                holder.tv_inspect.setVisibility(View.GONE);
                holder.tv_confirmInspection.setVisibility(View.GONE);
                holder.tv_viewDetails.setVisibility(View.GONE);
                holder.tv_status.setText(R.string.unknown_status);
        }
        //setting clicklisteners
        holder.tv_inspect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onInspect(inspectionData.get(pos).getTicketNumber());
                }
            }
        });
        holder.tv_confirmInspection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onConfirmInspection(inspectionData.get(pos).getTicketNumber());
                }
            }
        });
        holder.tv_viewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onViewDetails(inspectionData.get(pos).getTicketNumber());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return inspectionData.size();
    }

    @Override
    public Filter getFilter() {

        if (filter == null) {
            filter = new InspectionFilter(filteredInspectionData, this);
        }
        return filter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_ticketNumber, tv_seedTag, tv_variety, tv_deliveryDate, tv_deliveryAddress, tv_status, tv_inspect, tv_confirmInspection, tv_viewDetails, tv_totWeight, tv_weightPerPack, tv_expectedBag;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_expectedBag = itemView.findViewById(R.id.tv_expectedBag);
            this.tv_totWeight = itemView.findViewById(R.id.tv_totWeight);
            this.tv_weightPerPack = itemView.findViewById(R.id.tv_weightPerPack);
            this.tv_inspect = itemView.findViewById(R.id.tv_inspect);
            this.tv_seedTag = itemView.findViewById(R.id.tv_seedTag);
            this.tv_ticketNumber = itemView.findViewById(R.id.tv_ticketNumber);
            this.tv_variety = itemView.findViewById(R.id.tv_variety);
            this.tv_deliveryDate = itemView.findViewById(R.id.tv_deliveryDate);
            this.tv_deliveryAddress = itemView.findViewById(R.id.tv_deliveryAddress);
            this.tv_status = itemView.findViewById(R.id.tv_status);
            this.tv_confirmInspection = itemView.findViewById(R.id.tv_confirmInspection);
            this.tv_viewDetails = itemView.findViewById(R.id.tv_viewDetails);
        }
    }
}
