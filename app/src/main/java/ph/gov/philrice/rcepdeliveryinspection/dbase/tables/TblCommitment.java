package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbl_commitment")
public class TblCommitment {

    @PrimaryKey(autoGenerate = true)
    int id;

    int commitmentId;
    int commitment_value;
    String commitment_variety;
    String moa_number;

    public TblCommitment(int commitmentId, int commitment_value, String commitment_variety, String moa_number) {
        this.commitmentId = commitmentId;
        this.commitment_value = commitment_value;
        this.commitment_variety = commitment_variety;
        this.moa_number = moa_number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommitmentId() {
        return commitmentId;
    }

    public void setCommitmentId(int commitmentId) {
        this.commitmentId = commitmentId;
    }

    public int getCommitment_value() {
        return commitment_value;
    }

    public void setCommitment_value(int commitment_value) {
        this.commitment_value = commitment_value;
    }

    public String getCommitment_variety() {
        return commitment_variety;
    }

    public void setCommitment_variety(String commitment_variety) {
        this.commitment_variety = commitment_variety;
    }

    public String getMoa_number() {
        return moa_number;
    }

    public void setMoa_number(String moa_number) {
        this.moa_number = moa_number;
    }
}
