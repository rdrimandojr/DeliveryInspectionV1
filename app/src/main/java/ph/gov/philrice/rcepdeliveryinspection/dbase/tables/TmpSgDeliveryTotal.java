package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tmp_sgdelivery_total")
public class TmpSgDeliveryTotal {
    @PrimaryKey(autoGenerate = true)
    private int tmpSgDeliveryTotalId;
    private int sgDeliveryTotal;

    public TmpSgDeliveryTotal(int sgDeliveryTotal) {
        this.sgDeliveryTotal = sgDeliveryTotal;
    }

    public int getTmpSgDeliveryTotalId() {
        return tmpSgDeliveryTotalId;
    }

    public void setTmpSgDeliveryTotalId(int tmpSgDeliveryTotalId) {
        this.tmpSgDeliveryTotalId = tmpSgDeliveryTotalId;
    }

    public int getSgDeliveryTotal() {
        return sgDeliveryTotal;
    }

    public void setSgDeliveryTotal(int sgDeliveryTotal) {
        this.sgDeliveryTotal = sgDeliveryTotal;
    }
}




