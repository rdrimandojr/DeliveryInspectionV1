package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbl_temp_sampling")
public class TblTempSampling {
    @PrimaryKey(autoGenerate = true)
    private int samplingId;
    private String batchTicketNumber;
    private String seedTag;
    private float bagWeight;
    private String dateSampled;
    private int send;

    public TblTempSampling(String batchTicketNumber, String seedTag, float bagWeight, String dateSampled, int send) {
        this.batchTicketNumber = batchTicketNumber;
        this.seedTag = seedTag;
        this.bagWeight = bagWeight;
        this.dateSampled = dateSampled;
        this.send = send;
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
}
