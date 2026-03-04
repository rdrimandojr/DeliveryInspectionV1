package ph.gov.philrice.rcepdeliveryinspection.inspection.dataview;

import android.widget.Filter;

import java.util.ArrayList;

public class InspectionFilter extends Filter {


    private InspectionAdapter inspectionAdapter;
    private ArrayList<InspectionData> filterList;

    public InspectionFilter(ArrayList<InspectionData> filterList, InspectionAdapter inspectionAdapter) {
        this.inspectionAdapter = inspectionAdapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            //CHANGE TO UPPER
            constraint = constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<InspectionData> mInspectionData = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getSeedVariety().toUpperCase().contains(constraint)
                        || filterList.get(i).getSeedTag().toUpperCase().contains(constraint)
                        || filterList.get(i).getTicketNumber().toUpperCase().contains(constraint)) {
                    mInspectionData.add(filterList.get(i));
                }
            }
            results.count = mInspectionData.size();
            results.values = mInspectionData;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults results) {
        inspectionAdapter.inspectionData = (ArrayList<InspectionData>) results.values;
        //REFRESH
        inspectionAdapter.notifyDataSetChanged();
    }
}
