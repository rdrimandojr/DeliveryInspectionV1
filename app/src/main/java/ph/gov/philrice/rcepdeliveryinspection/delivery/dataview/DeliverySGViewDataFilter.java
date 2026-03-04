package ph.gov.philrice.rcepdeliveryinspection.delivery.dataview;

import android.widget.Filter;

import java.util.ArrayList;

public class DeliverySGViewDataFilter extends Filter {

    private DeliverySGViewAdapter deliverySGViewAdapter;
    private ArrayList<DeliverySGViewData> filterList;

    public DeliverySGViewDataFilter(ArrayList<DeliverySGViewData> filterList, DeliverySGViewAdapter deliverySGViewAdapter) {
        this.deliverySGViewAdapter = deliverySGViewAdapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            //CHANGE TO UPPER
            constraint = constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<DeliverySGViewData> mDeliverySGViewData = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getVariety().toUpperCase().contains(constraint) || filterList.get(i).getMunicipality().toUpperCase().contains(constraint)) {
                    mDeliverySGViewData.add(filterList.get(i));
                }
            }
            results.count = mDeliverySGViewData.size();
            results.values = mDeliverySGViewData;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults results) {
        deliverySGViewAdapter.deliverySGViewData = (ArrayList<DeliverySGViewData>) results.values;
        //REFRESH
        deliverySGViewAdapter.notifyDataSetChanged();
    }
}
