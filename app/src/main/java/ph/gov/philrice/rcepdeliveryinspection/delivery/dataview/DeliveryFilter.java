package ph.gov.philrice.rcepdeliveryinspection.delivery.dataview;

import android.widget.Filter;

import java.util.ArrayList;

public class DeliveryFilter extends Filter {

    private DeliveryAdapter deliveryAdapter;
    private ArrayList<DeliveryData> filterList;

    public DeliveryFilter(ArrayList<DeliveryData> filterList, DeliveryAdapter deliveryAdapter) {
        this.deliveryAdapter = deliveryAdapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            //CHANGE TO UPPER
            constraint = constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<DeliveryData> mDeliveryData = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getSeedVariety().toUpperCase().contains(constraint) || filterList.get(i).getSeedTag().toUpperCase().contains(constraint)) {
                    mDeliveryData.add(filterList.get(i));
                }
            }
            results.count = mDeliveryData.size();
            results.values = mDeliveryData;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults results) {
        deliveryAdapter.deliveryData = (ArrayList<DeliveryData>) results.values;
        //REFRESH
        deliveryAdapter.notifyDataSetChanged();
    }

}


