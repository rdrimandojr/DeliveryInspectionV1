package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TmpSgDeliveryTotal;

@Dao
public interface TmpSgDeliveryTotalDAO {
    @Insert
    void insert(TmpSgDeliveryTotal tmpSgDeliveryTotal);

    @Query("delete from tmp_sgdelivery_total")
    void nukeTable();

    @Query("select sgDeliveryTotal from tmp_sgdelivery_total order by tmpSgDeliveryTotalId desc limit 1")
    int getCurrentDeliveryTotal();
}
