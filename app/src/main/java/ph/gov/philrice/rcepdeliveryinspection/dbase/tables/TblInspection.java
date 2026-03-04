package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbl_inspection")
public class TblInspection {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int inspectionId;
    private String batchTicketNumber;
    private int screeningPassed;
    private String screeningRemarks;
    private int visualPassed;
    private String visualFindings;
    private String visualRemarks;
    private String visualInspectionImage;
    private int samplingPassed;
    private String samplingImage;
    private String samplingImage_path;
    private String batchDeliveryImage;
    private String batchDeliveryImage_path;
    private String dateInspected;
    private String dateCreated;
    private int send;
    //
    private String prv;
    private String moa_number;
    private String app_version;
    private String batchSeries;
    private int sendLocal;
    private int sendCentral;
    private int misBuffer;

    private String accountingImage;
    private String accountingImage_path;
    private String dr_date;
    private String dr_number;

    private String actual_inspection_date;//y-m-d format
    private String actual_delivery_date;//y-m-d format

    public TblInspection(String batchTicketNumber, int screeningPassed, String screeningRemarks,
                         int visualPassed, String visualFindings, String visualRemarks,
                         String visualInspectionImage, int samplingPassed, String samplingImage,
                         String samplingImage_path, String batchDeliveryImage,
                         String batchDeliveryImage_path, String dateInspected,
                         String dateCreated, int send, String prv, String moa_number,
                         String app_version, String batchSeries, int sendLocal, int sendCentral,
                         int misBuffer, String accountingImage, String accountingImage_path,
                         String dr_date, String dr_number, String actual_inspection_date,
                         String actual_delivery_date) {
        this.dr_date = dr_date;
        this.dr_number = dr_number;
        this.accountingImage = accountingImage;
        this.accountingImage_path = accountingImage_path;
        this.batchTicketNumber = batchTicketNumber;
        this.screeningPassed = screeningPassed;
        this.screeningRemarks = screeningRemarks;
        this.visualPassed = visualPassed;
        this.visualFindings = visualFindings;
        this.visualRemarks = visualRemarks;
        this.visualInspectionImage = visualInspectionImage;
        this.samplingPassed = samplingPassed;
        this.samplingImage = samplingImage;
        this.samplingImage_path = samplingImage_path;
        this.batchDeliveryImage = batchDeliveryImage;
        this.batchDeliveryImage_path = batchDeliveryImage_path;
        this.dateInspected = dateInspected;
        this.dateCreated = dateCreated;
        this.send = send;
        this.prv = prv;
        this.moa_number = moa_number;
        this.app_version = app_version;
        this.batchSeries = batchSeries;
        this.sendLocal = sendLocal;
        this.sendCentral = sendCentral;
        this.misBuffer = misBuffer;
        this.actual_inspection_date = actual_inspection_date;
        this.actual_delivery_date = actual_delivery_date;
    }

    public String getActual_delivery_date() {
        return actual_delivery_date;
    }

    public void setActual_delivery_date(String actual_delivery_date) {
        this.actual_delivery_date = actual_delivery_date;
    }

    public String getActual_inspection_date() {
        return actual_inspection_date;
    }

    public void setActual_inspection_date(String actual_inspection_date) {
        this.actual_inspection_date = actual_inspection_date;
    }

    public String getDr_date() {
        return dr_date;
    }

    public void setDr_date(String dr_date) {
        this.dr_date = dr_date;
    }

    public String getDr_number() {
        return dr_number;
    }

    public void setDr_number(String dr_number) {
        this.dr_number = dr_number;
    }

    public String getAccountingImage() {
        return accountingImage;
    }

    public void setAccountingImage(String accountingImage) {
        this.accountingImage = accountingImage;
    }

    public String getAccountingImage_path() {
        return accountingImage_path;
    }

    public void setAccountingImage_path(String accountingImage_path) {
        this.accountingImage_path = accountingImage_path;
    }

    public String getSamplingImage_path() {
        return samplingImage_path;
    }

    public void setSamplingImage_path(String samplingImage_path) {
        this.samplingImage_path = samplingImage_path;
    }

    public String getBatchDeliveryImage_path() {
        return batchDeliveryImage_path;
    }

    public void setBatchDeliveryImage_path(String batchDeliveryImage_path) {
        this.batchDeliveryImage_path = batchDeliveryImage_path;
    }

    public int getMisBuffer() {
        return misBuffer;
    }

    public void setMisBuffer(int misBuffer) {
        this.misBuffer = misBuffer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(int inspectionId) {
        this.inspectionId = inspectionId;
    }

    public String getBatchTicketNumber() {
        return batchTicketNumber;
    }

    public void setBatchTicketNumber(String batchTicketNumber) {
        this.batchTicketNumber = batchTicketNumber;
    }

    public int getScreeningPassed() {
        return screeningPassed;
    }

    public void setScreeningPassed(int screeningPassed) {
        this.screeningPassed = screeningPassed;
    }

    public String getScreeningRemarks() {
        return screeningRemarks;
    }

    public void setScreeningRemarks(String screeningRemarks) {
        this.screeningRemarks = screeningRemarks;
    }

    public int getVisualPassed() {
        return visualPassed;
    }

    public void setVisualPassed(int visualPassed) {
        this.visualPassed = visualPassed;
    }

    public String getVisualFindings() {
        return visualFindings;
    }

    public void setVisualFindings(String visualFindings) {
        this.visualFindings = visualFindings;
    }

    public String getVisualRemarks() {
        return visualRemarks;
    }

    public void setVisualRemarks(String visualRemarks) {
        this.visualRemarks = visualRemarks;
    }

    public String getVisualInspectionImage() {
        return visualInspectionImage;
    }

    public void setVisualInspectionImage(String visualInspectionImage) {
        this.visualInspectionImage = visualInspectionImage;
    }

    public int getSamplingPassed() {
        return samplingPassed;
    }

    public void setSamplingPassed(int samplingPassed) {
        this.samplingPassed = samplingPassed;
    }

    public String getSamplingImage() {
        return samplingImage;
    }

    public void setSamplingImage(String samplingImage) {
        this.samplingImage = samplingImage;
    }

    public String getBatchDeliveryImage() {
        return batchDeliveryImage;
    }

    public void setBatchDeliveryImage(String batchDeliveryImage) {
        this.batchDeliveryImage = batchDeliveryImage;
    }

    public String getDateInspected() {
        return dateInspected;
    }

    public void setDateInspected(String dateInspected) {
        this.dateInspected = dateInspected;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getSend() {
        return send;
    }

    public void setSend(int send) {
        this.send = send;
    }

    public String getPrv() {
        return prv;
    }

    public void setPrv(String prv) {
        this.prv = prv;
    }

    public String getMoa_number() {
        return moa_number;
    }

    public void setMoa_number(String moa_number) {
        this.moa_number = moa_number;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getBatchSeries() {
        return batchSeries;
    }

    public void setBatchSeries(String batchSeries) {
        this.batchSeries = batchSeries;
    }

    public int getSendLocal() {
        return sendLocal;
    }

    public void setSendLocal(int sendLocal) {
        this.sendLocal = sendLocal;
    }

    public int getSendCentral() {
        return sendCentral;
    }

    public void setSendCentral(int sendCentral) {
        this.sendCentral = sendCentral;
    }
}
