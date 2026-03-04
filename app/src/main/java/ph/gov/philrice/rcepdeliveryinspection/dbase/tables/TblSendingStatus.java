package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbl_sending_status")
public class TblSendingStatus {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String batchTicketNumber;
    private int isLocal;
    /*0 - liveServer status ; 1 - local server status*/
    private int status;
    /*0-none ; 1-tried sending ; 2-complete sent*/

    public TblSendingStatus(String batchTicketNumber, int isLocal, int status) {
        this.batchTicketNumber = batchTicketNumber;
        this.isLocal = isLocal;
        this.status = status;
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

    public int getIsLocal() {
        return isLocal;
    }

    public void setIsLocal(int isLocal) {
        this.isLocal = isLocal;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
