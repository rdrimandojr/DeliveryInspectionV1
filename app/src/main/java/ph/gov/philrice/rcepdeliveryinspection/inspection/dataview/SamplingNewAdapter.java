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
import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.R;

public class SamplingNewAdapter extends RecyclerView.Adapter<SamplingNewAdapter.ViewHolder> {

    private static final String TAG = "SamplingNewAdapter";
    private Context c;

    // Full data set and filtered list
    private ArrayList<SamplingNewData> originalList;
    private ArrayList<SamplingNewData> filteredList;

    private ItemClicked itemClickedListener;
    private String currentFilter = "All";

    public SamplingNewAdapter(Context ctx, ArrayList<SamplingNewData> samplingNewData) {
        this.c = ctx;
        this.originalList = new ArrayList<>(samplingNewData);
        this.filteredList = new ArrayList<>(samplingNewData);
    }

    // Interface
    public interface ItemClicked {
        void onDelete(int position);
    }

    public void setitemClickedListener(SamplingNewAdapter.ItemClicked itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_samplingnew_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SamplingNewAdapter.ViewHolder holder, int pos) {
        SamplingNewData data = filteredList.get(pos);

        String mBagWeight = "@\t" + data.getBagWeight() + "(kg)";
        holder.tv_seedTag.setText(data.getSeedTag());
        holder.tv_weight.setText(mBagWeight);

        holder.imgv_remove.setOnClickListener(view -> {
            if (itemClickedListener != null) {
                itemClickedListener.onDelete(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }


    public int getOriginalListSize() {
        return originalList.size();
    }

    // Public methods to modify dataset

    public void addItem(SamplingNewData data) {
        originalList.add(data);
        if (currentFilter.equalsIgnoreCase("All") ||
                data.getSeedTag().equalsIgnoreCase(currentFilter)) {
            filteredList.add(data);
            notifyItemInserted(filteredList.size() - 1);
        }
    }

    public void removeItem(int position) {
        SamplingNewData toRemove = filteredList.get(position);
        filteredList.remove(position);
        notifyItemRemoved(position);

        // Remove from originalList as well
        originalList.remove(toRemove);
    }

    public void updateData(ArrayList<SamplingNewData> newData) {
        originalList = new ArrayList<>(newData);
        applyFilter(currentFilter);
    }

    public void applyFilter(String seedTag) {
        currentFilter = seedTag;
        if (seedTag.equalsIgnoreCase("")) {
            filteredList = new ArrayList<>(originalList);
        } else {
            ArrayList<SamplingNewData> temp = new ArrayList<>();
            for (SamplingNewData item : originalList) {
                if (item.getSeedTag().equalsIgnoreCase(seedTag)) {
                    temp.add(item);
                }
            }
            filteredList = temp;
        }
        notifyDataSetChanged();
    }

    public List<SamplingNewData> getFilteredList() {
        return filteredList;
    }

    public List<SamplingNewData> getOriginalList() {
        return originalList;
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_seedTag, tv_weight;
        ImageView imgv_remove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tv_seedTag = itemView.findViewById(R.id.tv_seedTag);
            this.tv_weight = itemView.findViewById(R.id.tv_weight);
            this.imgv_remove = itemView.findViewById(R.id.imgv_remove);
        }
    }
}
