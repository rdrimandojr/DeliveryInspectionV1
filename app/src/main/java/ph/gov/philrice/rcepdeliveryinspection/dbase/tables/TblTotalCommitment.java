package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbl_total_commitment")
public class TblTotalCommitment {
    @PrimaryKey(autoGenerate = true)
    int id;

    int totalCommitmentId;
    int total_value;
    String moa_number;

    public TblTotalCommitment(int totalCommitmentId, int total_value, String moa_number) {
        this.totalCommitmentId = totalCommitmentId;
        this.total_value = total_value;
        this.moa_number = moa_number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTotalCommitmentId() {
        return totalCommitmentId;
    }

    public void setTotalCommitmentId(int totalCommitmentId) {
        this.totalCommitmentId = totalCommitmentId;
    }

    public int getTotal_value() {
        return total_value;
    }

    public void setTotal_value(int total_value) {
        this.total_value = total_value;
    }

    public String getMoa_number() {
        return moa_number;
    }

    public void setMoa_number(String moa_number) {
        this.moa_number = moa_number;
    }
}
