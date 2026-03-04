package ph.gov.philrice.rcepdeliveryinspection.inspection.dataview;

import android.widget.Filter;

import java.util.ArrayList;

public class InspectionSIViewFilter extends Filter {

    private InspectionSIViewAdapter siViewAdapter;
    private ArrayList<InspectionSIViewData> filterList;

    public InspectionSIViewFilter(ArrayList<InspectionSIViewData> filterList, InspectionSIViewAdapter siViewAdapter) {
        this.siViewAdapter = siViewAdapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            //CHANGE TO UPPER
            constraint = constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<InspectionSIViewData> mSIViewData = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getBatchTicketNumber().toUpperCase().contains(constraint) || filterList.get(i).getVariety().toUpperCase().contains(constraint)) {
                    mSIViewData.add(filterList.get(i));
                }
            }
            results.count = mSIViewData.size();
            results.values = mSIViewData;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults results) {
        siViewAdapter.siViewData = (ArrayList<InspectionSIViewData>) results.values;
        //REFRESH
        siViewAdapter.notifyDataSetChanged();
    }
}
