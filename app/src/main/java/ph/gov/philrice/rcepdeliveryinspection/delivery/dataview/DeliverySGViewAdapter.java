package ph.gov.philrice.rcepdeliveryinspection.delivery.dataview;

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

import ph.gov.philrice.rcepdeliveryinspection.Fun;
import ph.gov.philrice.rcepdeliveryinspection.R;

public class DeliverySGViewAdapter extends RecyclerView.Adapter<DeliverySGViewAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "DeliverySGViewAdapter";
    private Context c;
    public ArrayList<DeliverySGViewData> deliverySGViewData;
    private ItemClicked itemClickedListener;
    private ArrayList<DeliverySGViewData> filteredDeliverySGViewData;
    private DeliverySGViewDataFilter filter;

    public DeliverySGViewAdapter(Context ctx, ArrayList<DeliverySGViewData> deliverySGViewData) {
        this.c = ctx;
        this.deliverySGViewData = deliverySGViewData;
        this.filteredDeliverySGViewData = deliverySGViewData;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new DeliverySGViewDataFilter(filteredDeliverySGViewData, this);
        }
        return filter;
    }

    //interface
    public interface ItemClicked {
        void onDispatched(String batchTicketNumber/*value to return*/);

        void onCancel(String batchTicketNumber);
    }

    public void setitemClickedListener(ItemClicked itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_deliverysgview_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull DeliverySGViewAdapter.ViewHolder holder,  int pos) {
        //variables
        String mbatchTicketNumber = deliverySGViewData.get(pos).getBatchTicketNumber();
        String mvariety = deliverySGViewData.get(pos).getVariety();
        int mtotalBags = deliverySGViewData.get(pos).getTotalBags();
        int mactualTotal = deliverySGViewData.get(pos).getActualTotal();
        String mdeliveryDate = deliverySGViewData.get(pos).getDeliveryDate();
        String mdropoffPoint = deliverySGViewData.get(pos).getProvince() + ", " + deliverySGViewData.get(pos).getMunicipality() + "-> " + deliverySGViewData.get(pos).getDropoffPoint();
        int mstatus = deliverySGViewData.get(pos).getStatus();
        //x if no status in server
        String masOf = deliverySGViewData.get(pos).getAsOf().equals("x") ? "unkown status date" : Fun.formatDate(deliverySGViewData.get(pos).getAsOf());
        //set text to views
        holder.tv_batchTicketNumber.setText(mbatchTicketNumber);
        String totalSummary = mactualTotal + "/" + mtotalBags;
        holder.tv_totalBags.setText(totalSummary);
        holder.tv_variety.setText(mvariety);
        holder.tv_deliveryDate.setText(mdeliveryDate);
        holder.tv_deliveryAddress.setText(mdropoffPoint);

        switch (mstatus) {
            case 0:
                //PENDING
                String pending = "Pending delivery as of " + masOf;
                holder.tv_status.setText(pending);
                holder.tv_colorStat.setBackgroundResource(R.color.pending);
                //buttons to show
                holder.tv_btnDelivered.setVisibility(View.VISIBLE);
                holder.tv_btnCancel.setVisibility(View.VISIBLE);
                break;
            case 1:
                //PASSED
                String passed = "Inspected as of " + masOf;
                holder.tv_status.setText(passed);
                //passed but incomplete
                if (mtotalBags > mactualTotal) {
                    holder.tv_colorStat.setBackgroundResource(R.color.prri_orange);
                }
                //passed and complete
                if (mtotalBags == mactualTotal) {
                    holder.tv_colorStat.setBackgroundResource(R.color.passed);
                }
                //buttons to hide
                holder.tv_btnDelivered.setVisibility(View.GONE);
                holder.tv_btnCancel.setVisibility(View.GONE);
                break;
            case 2:
                //REJECTED
                String rejected = "Inspected as of " + masOf;
                holder.tv_status.setText(rejected);
                holder.tv_colorStat.setBackgroundResource(R.color.rejected);
                //buttons to hide
                holder.tv_btnDelivered.setVisibility(View.GONE);
                holder.tv_btnCancel.setVisibility(View.GONE);
                break;
            case 3:
                String intransit = "In transit as of " + masOf;
                holder.tv_colorStat.setBackgroundResource(R.color.blue);
                holder.tv_status.setText(intransit);
                //buttons to show
                holder.tv_btnCancel.setVisibility(View.GONE);
                //buttons to hide
                holder.tv_btnDelivered.setVisibility(View.GONE);
                break;
            case 4:
                //CANCELLED
                String cancel = "Cancelled as of " + masOf;
                holder.tv_colorStat.setBackgroundResource(R.color.white);
                holder.tv_status.setText(cancel);
                //buttons to hide
                holder.tv_btnDelivered.setVisibility(View.GONE);
                holder.tv_btnCancel.setVisibility(View.GONE);
                break;
            default:
                String unknown = "Unknown Status";
                holder.tv_colorStat.setBackgroundResource(R.color.black);
                holder.tv_status.setText(unknown);
                //buttons to hide
                holder.tv_btnDelivered.setVisibility(View.GONE);
                holder.tv_btnCancel.setVisibility(View.GONE);
        }

        holder.tv_btnDelivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onDispatched(deliverySGViewData.get(pos).getBatchTicketNumber());
                }
            }
        });

        holder.tv_btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onCancel(deliverySGViewData.get(pos).getBatchTicketNumber());
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return deliverySGViewData.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_batchTicketNumber, tv_colorStat, tv_variety, tv_deliveryDate, tv_deliveryAddress, tv_status, tv_btnDelivered, tv_totalBags, tv_btnCancel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_totalBags = itemView.findViewById(R.id.tv_totalBags);
            this.tv_batchTicketNumber = itemView.findViewById(R.id.tv_batchTicketNumber);
            this.tv_colorStat = itemView.findViewById(R.id.tv_colorStat);
            this.tv_variety = itemView.findViewById(R.id.tv_variety);
            this.tv_deliveryDate = itemView.findViewById(R.id.tv_deliveryDate);
            this.tv_deliveryAddress = itemView.findViewById(R.id.tv_deliveryAddress);
            this.tv_status = itemView.findViewById(R.id.tv_status);
            this.tv_btnDelivered = itemView.findViewById(R.id.tv_btnDelivered);
            this.tv_btnCancel = itemView.findViewById(R.id.tv_btnCancel);
        }
    }


}
