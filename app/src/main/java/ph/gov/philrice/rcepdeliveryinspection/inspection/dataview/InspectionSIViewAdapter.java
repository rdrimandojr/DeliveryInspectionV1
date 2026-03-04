package ph.gov.philrice.rcepdeliveryinspection.inspection.dataview;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ph.gov.philrice.rcepdeliveryinspection.Fun;
import ph.gov.philrice.rcepdeliveryinspection.R;

public class InspectionSIViewAdapter
        extends RecyclerView.Adapter<InspectionSIViewAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "DeliverySGViewAdapter";
    private Context c;
    public ArrayList<InspectionSIViewData> siViewData;
    private ArrayList<InspectionSIViewData> filteredSIViewData;
    private ItemClicked itemClickedListener;
    private InspectionSIViewFilter filter;

    public InspectionSIViewAdapter(Context ctx, ArrayList<InspectionSIViewData> siViewData) {
        this.c = ctx;
        this.siViewData = siViewData;
        this.filteredSIViewData = siViewData;
    }

    //interface
    public interface ItemClicked {
        void onPassed(String batchTicketNumber/*value to return*/);

        // void onReject(String batchTicketNumber/*value to return*/);

        void onSendLocal(String batchTicketNumber/*value to return*/);

        void onSendCentral(String batchTicketNumber/*value to return*/);

        /*for sending downloaded data from central server to local server*/
        void onSendLocal2(String batchTicketNumber/*value to return*/);

        void onDownloadData(String batchTicketNumber/*value to return*/);

        void onReset(String batchTicketNumber, int mHasLocalStatus, int mAppLocalStatus,
                     int mstatus/*online*/);


    }

    public void setitemClickedListener(ItemClicked itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.rv_inspectionsiview_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull InspectionSIViewAdapter.ViewHolder holder, int pos) {

        final int finalPos = pos;

        InspectionSIViewData data = siViewData.get(finalPos);

        String mbatchTicketNumber =
                data.getBatchTicketNumber(); //siViewData.get(pos).getBatchTicketNumber();
        String mvariety = data.getVariety(); // siViewData.get(pos).getVariety();
        int mtotalBags = data.getTotalBags(); //siViewData.get(pos).getTotalBags();
        String mdeliveryDate = data.getDeliveryDate(); //siViewData.get(pos).getDeliveryDate();
        String mdropoffPoint = data.getProvince() + ", " + data.getMunicipality() +
                "-> " + data.getDropoffPoint();
        //siViewData.get(pos).getProvince() + ", " + siViewData.get(pos).getMunicipality() + "-> " + siViewData.get(pos).getDropoffPoint();
        int mstatus =
                data.getStatus(); //siViewData.get(pos).getStatus();//status from central server
        //x if no status in server
        String masOf = data.getAsOf().equals("x") ? "unknown status date" :
                Fun.formatDate(siViewData.get(pos)
                        .getAsOf());//siViewData.get(pos).getAsOf().equals("x") ? "unknown status date" :Fun.formatDate(siViewData.get(pos).getAsOf());
        int mLocalStatus = Fun.hasLocalInspection(c, mbatchTicketNumber);
        int mLocalSendingStatus = Fun.localSendingStatus(c, mbatchTicketNumber);

        int mHasLocalStatus = Fun.hasLocalStatus(c, mbatchTicketNumber);
        int mAppLocalStatus = Fun.getLocalStatus(c, mbatchTicketNumber);

        int isDownloaded = Fun.isDownloaded(c, mbatchTicketNumber);

        //set text to views
        holder.tv_batchTicketNumber.setText(mbatchTicketNumber);
        holder.tv_totalBags.setText(String.valueOf(mtotalBags) + " (" +
                data.getSeed_distribution_mode() + ")");
        holder.tv_variety.setText(mvariety);
        holder.tv_deliveryDate.setText(mdeliveryDate);
        holder.tv_deliveryAddress.setText(mdropoffPoint);

        if (mHasLocalStatus > 0) {
            //may status sa app or inspected na
            switch (mAppLocalStatus) {
                case 1:
                    if (isDownloaded == 1) {
                        //PASSED LOCAL WITH PASSED STATUS CENTRAL
                        //set status color and message
                        holder.tv_colorStat.setBackgroundResource(R.color.passed);
                        String passed = "Inspected as of " + masOf;
                        holder.tv_status.setText(passed);
                        //show and hide containers
                        holder.ll_rejectInspect_container.setVisibility(View.GONE);
                        holder.ll_sendLocalCentral_container.setVisibility(View.GONE);
                        holder.ll_downloadDataSendLocal_container.setVisibility(View.VISIBLE);
                        //buttons appearance
//                        holder.tv_btnDownload.setVisibility(View.GONE);
//                        holder.tv_btnSendLocal2.setVisibility(View.VISIBLE);

                        if (Fun.completedBatchLocal(c, mbatchTicketNumber) == 1) {//complete sending
//                            holder.tv_btnSendLocal2.setEnabled(false);
                            holder.tv_btnSendLocal2.setText(R.string.sent_local);
//                            holder.tv_btnSendLocal2.setBackgroundResource(R.color.pending);
                        }
                    } else {
                        //PASSED LOCAL
                        String passed = "Inspected as of " + masOf;
                        holder.tv_status.setText(passed);
                        holder.tv_colorStat.setBackgroundResource(R.color.passed);
                  /*      holder.tv_btnInspect.setVisibility(View.GONE);
                        holder.tv_btnReject.setVisibility(View.GONE);*/

                        //show and hide containers
                        holder.ll_rejectInspect_container.setVisibility(View.GONE);
                        holder.ll_sendLocalCentral_container.setVisibility(View.VISIBLE);
                        holder.ll_downloadDataSendLocal_container.setVisibility(View.GONE);
                        //buttons appearance
//                        holder.tv_btnSendLocal.setVisibility(View.VISIBLE);
                        holder.tv_btnSendCentral.setVisibility(View.VISIBLE);

                        //send local button
                        if (Fun.completedBatchLocal(c, mbatchTicketNumber) == 1) {//complete sending
//                            holder.tv_btnSendLocal.setEnabled(false);
                            holder.tv_btnSendLocal.setText(R.string.sent_local);
                            holder.tv_btnSendLocal.setBackgroundResource(R.color.pending);
                        }
                        //send central button
                        if (Fun.completedBatchCentral(c, mbatchTicketNumber) ==
                                1) {//complete sending
                            holder.tv_btnSendCentral.setEnabled(false);
                            holder.tv_btnSendCentral.setText(R.string.sent_central);
                            holder.tv_btnSendCentral.setBackgroundResource(R.color.pending);
                        }

                    }

                   /* if (mstatus == 1) {
                        //PASSED LOCAL WITH PASSED STATUS CENTRAL
                        //set status color and message
                        holder.tv_colorStat.setBackgroundResource(R.color.passed);
                        String passed = "Inspected as of " + masOf;
                        holder.tv_status.setText(passed);
                        //show and hide containers
                        holder.ll_rejectInspect_container.setVisibility(View.GONE);
                        holder.ll_sendLocalCentral_container.setVisibility(View.GONE);
                        holder.ll_downloadDataSendLocal_container.setVisibility(View.VISIBLE);
                        //buttons appearance
                        holder.tv_btnDownload.setVisibility(View.GONE);
                        holder.tv_btnSendLocal2.setVisibility(View.VISIBLE);

                        if (Fun.completedBatchLocal(c, mbatchTicketNumber) == 1) {//complete sending
                            holder.tv_btnSendLocal2.setEnabled(false);
                            holder.tv_btnSendLocal2.setText(R.string.sent_local);
                            holder.tv_btnSendLocal2.setBackgroundResource(R.color.pending);
                        }

                    } else {
                        //PASSED LOCAL
                        String passed = "Inspected as of " + masOf;
                        holder.tv_status.setText(passed);
                        holder.tv_colorStat.setBackgroundResource(R.color.passed);
                  *//*      holder.tv_btnInspect.setVisibility(View.GONE);
                        holder.tv_btnReject.setVisibility(View.GONE);*//*

                        //show and hide containers
                        holder.ll_rejectInspect_container.setVisibility(View.GONE);
                        holder.ll_sendLocalCentral_container.setVisibility(View.VISIBLE);
                        holder.ll_downloadDataSendLocal_container.setVisibility(View.GONE);
                        //buttons appearance
                        holder.tv_btnSendLocal.setVisibility(View.VISIBLE);
                        holder.tv_btnSendCentral.setVisibility(View.VISIBLE);

                        //send local button
                        if (Fun.completedBatchLocal(c, mbatchTicketNumber) == 1) {//complete sending
                            holder.tv_btnSendLocal.setEnabled(false);
                            holder.tv_btnSendLocal.setText(R.string.sent_local);
                            holder.tv_btnSendLocal.setBackgroundResource(R.color.pending);
                        }
                        //send central button
                        if (Fun.completedBatchCentral(c, mbatchTicketNumber) == 1) {//complete sending
                            holder.tv_btnSendCentral.setEnabled(false);
                            holder.tv_btnSendCentral.setText(R.string.sent_central);
                            holder.tv_btnSendCentral.setBackgroundResource(R.color.pending);
                        }
                    }*/
                    break;
                case 2:
                    //REJECTED
                    String rejected = "Inspected as of " + masOf;
                    holder.tv_status.setText(rejected);
                    holder.tv_colorStat.setBackgroundResource(R.color.rejected);
                    /*holder.tv_btnInspect.setVisibility(View.GONE);
                    holder.tv_btnReject.setVisibility(View.GONE);*/

                    //show and hide containers
                    holder.ll_rejectInspect_container.setVisibility(View.GONE);
                    holder.ll_sendLocalCentral_container.setVisibility(View.VISIBLE);
                    holder.ll_downloadDataSendLocal_container.setVisibility(View.GONE);
                    //buttons appearance
                    /*holder.tv_btnInspect.setVisibility(View.VISIBLE);
                    holder.tv_btnReject.setVisibility(View.VISIBLE);*/

                    if (Fun.completedBatchLocal(c, mbatchTicketNumber) == 1) {//complete sending
//                        holder.tv_btnSendLocal.setEnabled(false);
                        holder.tv_btnSendLocal.setText(R.string.sent_local);
                        holder.tv_btnSendLocal.setBackgroundResource(R.color.pending);
                    }
                    //send central button
                    if (Fun.completedBatchCentral(c, mbatchTicketNumber) == 1) {//complete sending
                        holder.tv_btnSendCentral.setEnabled(false);
                        holder.tv_btnSendCentral.setText(R.string.sent_central);
                        holder.tv_btnSendCentral.setBackgroundResource(R.color.pending);
                    }
                    break;
            }
        } else {
            //status from server
            switch (mstatus) {
                case 0:
                    String pending = "Pending delivery as of " + masOf;
                    holder.tv_status.setText(pending);
                    holder.tv_colorStat.setBackgroundResource(R.color.pending);

                    //show and hide containers
                    holder.ll_rejectInspect_container.setVisibility(View.VISIBLE);
                    holder.ll_sendLocalCentral_container.setVisibility(View.GONE);
                    holder.ll_downloadDataSendLocal_container.setVisibility(View.GONE);
                    //buttons appearance
                    holder.tv_btnInspect.setVisibility(View.VISIBLE);
                    holder.tv_btnReject.setVisibility(View.GONE);



                    /*holder.tv_btnInspect.setVisibility(View.VISIBLE);
                    holder.tv_btnReject.setVisibility(View.VISIBLE);
                    holder.tv_btnSendLocal.setVisibility(View.GONE);
                    holder.tv_btnSendCentral.setVisibility(View.GONE);
                    holder.tv_btnDownload.setVisibility(View.GONE);
                    holder.tv_btnSendLocal2.setVisibility(View.GONE);*/
                    break;
                case 1:
                    //PASSED
                    String passed = "Inspected as of " + masOf;
                    holder.tv_status.setText(passed);
                    holder.tv_colorStat.setBackgroundResource(R.color.passed);
                    //show and hide container
                    holder.ll_rejectInspect_container.setVisibility(View.GONE);
                    holder.ll_sendLocalCentral_container.setVisibility(View.GONE);
                    holder.ll_downloadDataSendLocal_container.setVisibility(View.VISIBLE);
                    //buttons appearance
//                    holder.tv_btnSendLocal2.setVisibility(View.GONE);
//                    holder.tv_btnDownload.setVisibility(View.VISIBLE);

                   /* holder.tv_btnInspect.setVisibility(View.GONE);
                    holder.tv_btnReject.setVisibility(View.GONE);
                    holder.tv_btnSendLocal.setVisibility(View.GONE);
                    holder.tv_btnSendCentral.setVisibility(View.GONE);
                    holder.tv_btnDownload.setVisibility(View.VISIBLE);
                    holder.tv_btnSendLocal2.setVisibility(View.GONE);*/
                    break;
                case 2:
                    //REJECTED
                    String rejected = "Inspected as of " + masOf;
                    holder.tv_status.setText(rejected);
                    holder.tv_colorStat.setBackgroundResource(R.color.rejected);
                    //show and hide container
                    holder.ll_rejectInspect_container.setVisibility(View.GONE);
                    holder.ll_sendLocalCentral_container.setVisibility(View.GONE);
                    holder.ll_downloadDataSendLocal_container.setVisibility(View.GONE);


                    /*holder.tv_btnInspect.setVisibility(View.GONE);
                    holder.tv_btnReject.setVisibility(View.GONE);
                    holder.tv_btnSendLocal.setVisibility(View.GONE);
                    holder.tv_btnSendCentral.setVisibility(View.GONE);
                    holder.tv_btnDownload.setVisibility(View.GONE);
                    holder.tv_btnSendLocal2.setVisibility(View.GONE);*/
                    break;
                case 3:
                    String intransit = "In transit as of " + masOf;
                    holder.tv_colorStat.setBackgroundResource(R.color.blue);
                    holder.tv_status.setText(intransit);

                    //show and hide containers
                    holder.ll_rejectInspect_container.setVisibility(View.VISIBLE);
                    holder.ll_sendLocalCentral_container.setVisibility(View.GONE);
                    holder.ll_downloadDataSendLocal_container.setVisibility(View.GONE);
                    //buttons appearance
                    holder.tv_btnInspect.setVisibility(View.VISIBLE);
                    holder.tv_btnReject.setVisibility(View.GONE);


                   /* holder.tv_btnInspect.setVisibility(View.VISIBLE);
                    holder.tv_btnReject.setVisibility(View.VISIBLE);
                    holder.tv_btnSendLocal.setVisibility(View.GONE);
                    holder.tv_btnSendCentral.setVisibility(View.GONE);
                    holder.tv_btnDownload.setVisibility(View.GONE);
                    holder.tv_btnSendLocal2.setVisibility(View.GONE);*/
                    break;
                case 4:
                    String cancelled = "Cancelled as of " + masOf;
                    holder.tv_colorStat.setBackgroundResource(R.color.white);
                    holder.tv_status.setText(cancelled);
                    //show and hide containers
                    holder.ll_rejectInspect_container.setVisibility(View.GONE);
                    holder.ll_sendLocalCentral_container.setVisibility(View.GONE);
                    holder.ll_downloadDataSendLocal_container.setVisibility(View.GONE);

                    break;

                default:
                    //default status
                    String unknown = "Unknown Status";
                    holder.tv_colorStat.setBackgroundResource(R.color.black);
                    holder.tv_status.setText(unknown);
                    //show and hide containers
                    holder.ll_rejectInspect_container.setVisibility(View.VISIBLE);
                    holder.ll_sendLocalCentral_container.setVisibility(View.GONE);
                    holder.ll_downloadDataSendLocal_container.setVisibility(View.GONE);
                  /*  holder.tv_btnInspect.setVisibility(View.GONE);
                    holder.tv_btnReject.setVisibility(View.GONE);
                    holder.tv_btnSendLocal.setVisibility(View.GONE);
                    holder.tv_btnSendCentral.setVisibility(View.GONE);
                    holder.tv_btnDownload.setVisibility(View.GONE);
                    holder.tv_btnSendLocal2.setVisibility(View.GONE);*/
            }
        }

        //long click press were disabled
        holder.tv_batchTicketNumber.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (itemClickedListener != null) {
                    itemClickedListener.onReset(siViewData.get(finalPos).getBatchTicketNumber(),
                            mHasLocalStatus, mAppLocalStatus, mstatus);
                }


                return false;
            }
        });

        //Listener
        holder.tv_btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             /*   if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onReject(siViewData.get(pos).getBatchTicketNumber());
                }*/
            }
        });
        holder.tv_btnInspect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onPassed(siViewData.get(finalPos).getBatchTicketNumber());
                }
            }
        });
        holder.tv_btnSendLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onSendLocal(
                            siViewData.get(finalPos).getBatchTicketNumber());
                }
            }
        });
        holder.tv_btnSendCentral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onSendCentral(
                            siViewData.get(finalPos).getBatchTicketNumber());
                }
            }
        });

        holder.tv_btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onDownloadData(
                            siViewData.get(finalPos).getBatchTicketNumber());
                }
            }
        });

        holder.tv_btnSendLocal2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickedListener != null) {
                    //getting ticketNumber
                    itemClickedListener.onSendLocal2(
                            siViewData.get(finalPos).getBatchTicketNumber());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return siViewData.size();
    }


    @Override
    public Filter getFilter() {

        if (filter == null) {
            filter = new InspectionSIViewFilter(filteredSIViewData, this);
        }
        return filter;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_batchTicketNumber, tv_colorStat, tv_totalBags, tv_variety, tv_deliveryDate,
                tv_deliveryAddress, tv_status, tv_btnReject, tv_btnInspect, tv_btnSendLocal,
                tv_btnSendCentral, tv_btnDownload, tv_btnSendLocal2;

        LinearLayout ll_rejectInspect_container, ll_sendLocalCentral_container,
                ll_downloadDataSendLocal_container;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.ll_rejectInspect_container =
                    itemView.findViewById(R.id.ll_rejectInspect_container);
            this.ll_sendLocalCentral_container =
                    itemView.findViewById(R.id.ll_sendLocalCentral_container);
            this.ll_downloadDataSendLocal_container =
                    itemView.findViewById(R.id.ll_downloadDataSendLocal_container);
            this.tv_btnSendCentral = itemView.findViewById(R.id.tv_btnSendCentral);
            this.tv_totalBags = itemView.findViewById(R.id.tv_totalBags);
            this.tv_batchTicketNumber = itemView.findViewById(R.id.tv_batchTicketNumber);
            this.tv_colorStat = itemView.findViewById(R.id.tv_colorStat);
            this.tv_variety = itemView.findViewById(R.id.tv_variety);
            this.tv_deliveryDate = itemView.findViewById(R.id.tv_deliveryDate);
            this.tv_deliveryAddress = itemView.findViewById(R.id.tv_deliveryAddress);
            this.tv_status = itemView.findViewById(R.id.tv_status);
            this.tv_btnReject = itemView.findViewById(R.id.tv_btnReject);
            this.tv_btnInspect = itemView.findViewById(R.id.tv_btnInspect);
            this.tv_btnSendLocal = itemView.findViewById(R.id.tv_btnSendLocal);
            this.tv_btnDownload = itemView.findViewById(R.id.tv_btnDownload);
            this.tv_btnSendLocal2 = itemView.findViewById(R.id.tv_btnSendLocal2);
        }
    }
}
