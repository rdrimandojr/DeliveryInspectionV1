package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbl_delivery_status_app_local")
public class TblDeliveryStatusAppLocal {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String batchTicketNumber;
    private int status;
    private String dateCreated;
    private int attemptSendLocal;
    private int attemptSendCentral;


    public TblDeliveryStatusAppLocal(String batchTicketNumber, int status, String dateCreated, int attemptSendLocal, int attemptSendCentral) {
        this.batchTicketNumber = batchTicketNumber;
        this.status = status;
        this.dateCreated = dateCreated;
        this.attemptSendLocal = attemptSendLocal;
        this.attemptSendCentral = attemptSendCentral;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBatchTicketNumber() {
        return batchTicketNumber;
    }

    public void setBatchTicketNumber(String batchTicketNumber) {
        this.batchTicketNumber = batchTicketNumber;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getAttemptSendLocal() {
        return attemptSendLocal;
    }

    public void setAttemptSendLocal(int attemptSendLocal) {
        this.attemptSendLocal = attemptSendLocal;
    }

    public int getAttemptSendCentral() {
        return attemptSendCentral;
    }

    public void setAttemptSendCentral(int attemptSendCentral) {
        this.attemptSendCentral = attemptSendCentral;
    }
}
