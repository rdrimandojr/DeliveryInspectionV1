package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TmpDeliveryBatchData;

@Dao
public interface TmpDeliveryBatchDataDAO {

    @Insert
    void insert(TmpDeliveryBatchData tmpDeliveryBatchData);

    @Query("select * from tmp_delivery_batch_data order by tmpDeliveryBatchDataId desc")
    List<TmpDeliveryBatchData> getAll();

    @Query("delete from tmp_delivery_batch_data")
    void nukeTable();

    @Query("delete from tmp_delivery_batch_data where tmpDeliveryBatchDataId =:tmpDeliveryBatchDataId")
    void removeSelected(int tmpDeliveryBatchDataId);

    @Query("select sum(totalBagCount) from tmp_delivery_batch_data")
    int getTempDeliveryBatchTotal();

    @Query("select count(seedTag) from tmp_delivery_batch_data where seedTag =:seedTag")
    int seedTagCount(String seedTag);

    @Query("select sum(totalBagCount) from tmp_delivery_batch_data where seedVariety =:seedVariety and batchTicketNumber =:batchTicketNumber")
    int getCurrentBatchVarietyTotalInput(String seedVariety, String batchTicketNumber);

}
