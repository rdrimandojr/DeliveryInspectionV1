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

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "LogsTransferAdapter";
    private Context c;
    public ArrayList<DeliveryData> deliveryData;
    private ArrayList<DeliveryData> filteredDeliveryData;
    private DeliveryFilter filter;


    public DeliveryAdapter(Context ctx, ArrayList<DeliveryData> deliveryData) {
        this.c = ctx;
        this.deliveryData = deliveryData;
        this.filteredDeliveryData = deliveryData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_delivery_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryAdapter.ViewHolder holder,  int pos) {

        holder.tv_seedTag.setText(deliveryData.get(pos).getSeedTag());
        holder.tv_ticketNumber.setText(deliveryData.get(pos).getTicketNumber());
        holder.tv_variety.setText(deliveryData.get(pos).getSeedVariety());
        holder.tv_deliveryDate.setText(Fun.formatDate(deliveryData.get(pos).getDeliveryDate()));
        holder.tv_deliveryAddress.setText(deliveryData.get(pos).getProvince() + ">" + deliveryData.get(pos).getMunicipality() + ">" + deliveryData.get(pos).getDeliverTo());
        switch (deliveryData.get(pos).getStatus()) {
            case "0":
                holder.tv_status.setText(R.string.pending);
                holder.tv_colorStat.setBackgroundResource(R.color.pending);
                break;
            case "1":
                //PASSED
                holder.tv_status.setText(R.string.passed);
                holder.tv_colorStat.setBackgroundResource(R.color.passed);
                break;
            case "2":
                //REJECTED
                holder.tv_status.setText(R.string.rejected);
                holder.tv_colorStat.setBackgroundResource(R.color.rejected);
                break;
            default:
                holder.tv_colorStat.setBackgroundResource(R.color.black);
                holder.tv_status.setText(R.string.unknown_status);
        }

    }


    @Override
    public int getItemCount() {
        return deliveryData.size();
    }

    @Override
    public Filter getFilter() {

        if (filter == null) {
            filter = new DeliveryFilter(filteredDeliveryData, this);
        }
        return filter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_seedTag, tv_ticketNumber, tv_variety, tv_deliveryDate, tv_deliveryAddress, tv_status, tv_colorStat;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_colorStat = itemView.findViewById(R.id.tv_colorStat);
            this.tv_seedTag = itemView.findViewById(R.id.tv_seedTag);
            this.tv_ticketNumber = itemView.findViewById(R.id.tv_ticketNumber);
            this.tv_variety = itemView.findViewById(R.id.tv_variety);
            this.tv_deliveryDate = itemView.findViewById(R.id.tv_deliveryDate);
            this.tv_deliveryAddress = itemView.findViewById(R.id.tv_deliveryAddress);
            this.tv_status = itemView.findViewById(R.id.tv_status);
        }
    }

}
