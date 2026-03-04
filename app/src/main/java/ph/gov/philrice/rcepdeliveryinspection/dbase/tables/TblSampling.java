package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbl_sampling")
public class TblSampling {
    @PrimaryKey(autoGenerate = true)
    private int samplingId;
    private String batchTicketNumber;
    private String seedTag;
    private float bagWeight;
    private String dateSampled;
    private int send;
    //
    private String prv;
    private String moa_number;
    private String app_version;
    private String batchSeries;
    private int sendLocal;
    private int sendCentral;


    public TblSampling(String batchTicketNumber, String seedTag, float bagWeight, String dateSampled, int send, String prv, String moa_number, String app_version, String batchSeries, int sendLocal, int sendCentral) {
        this.batchTicketNumber = batchTicketNumber;
        this.seedTag = seedTag;
        this.bagWeight = bagWeight;
        this.dateSampled = dateSampled;
        this.send = send;
        this.prv = prv;
        this.moa_number = moa_number;
        this.app_version = app_version;
        this.batchSeries = batchSeries;
        this.sendLocal = sendLocal;
        this.sendCentral = sendCentral;
    }

    public int getSamplingId() {
        return samplingId;
    }

    public void setSamplingId(int samplingId) {
        this.samplingId = samplingId;
    }

    public String getBatchTicketNumber() {
        return batchTicketNumber;
    }

    public void setBatchTicketNumber(String batchTicketNumber) {
        this.batchTicketNumber = batchTicketNumber;
    }

    public String getSeedTag() {
        return seedTag;
    }

    public void setSeedTag(String seedTag) {
        this.seedTag = seedTag;
    }

    public float getBagWeight() {
        return bagWeight;
    }

    public void setBagWeight(float bagWeight) {
        this.bagWeight = bagWeight;
    }

    public String getDateSampled() {
        return dateSampled;
    }

    public void setDateSampled(String dateSampled) {
        this.dateSampled = dateSampled;
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
