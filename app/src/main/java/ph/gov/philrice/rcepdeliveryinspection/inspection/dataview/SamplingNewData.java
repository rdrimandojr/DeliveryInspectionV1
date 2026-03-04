package ph.gov.philrice.rcepdeliveryinspection.inspection.dataview;

import androidx.annotation.NonNull;

public class SamplingNewData {
    private String batchTicketNumber;
    private String seedTag;
    private float bagWeight;
    private String dateSampled;
    private int send;
    //
    private String app_version;
    private int sendLocal;
    private int sendCentral;

    public SamplingNewData(String batchTicketNumber, String seedTag, float bagWeight, String dateSampled, int send, String app_version, int sendLocal, int sendCentral) {
        this.batchTicketNumber = batchTicketNumber;
        this.seedTag = seedTag;
        this.bagWeight = bagWeight;
        this.dateSampled = dateSampled;
        this.send = send;
        this.app_version = app_version;
        this.sendLocal = sendLocal;
        this.sendCentral = sendCentral;
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

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
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

    @NonNull
    @Override
    public String toString() {
        return "SamplingNewData{" +
                "batchTicketNumber='" + batchTicketNumber + '\'' +
                ", seedTag='" + seedTag + '\'' +
                ", bagWeight=" + bagWeight +
                ", dateSampled='" + dateSampled + '\'' +
                ", send=" + send +
                ", app_version='" + app_version + '\'' +
                ", sendLocal=" + sendLocal +
                ", sendCentral=" + sendCentral +
                '}';
    }


}


