package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblCurrentDeliveryCount;

@Dao
public interface TblCurrentDeliveryCountDAO {

    @Insert
    void insert(TblCurrentDeliveryCount tblCurrentDeliveryCount);

    @Query("select * from tbl_current_delivery_count order by province asc, municipality asc")
    List<TblCurrentDeliveryCount> getAll();

    @Query("delete from tbl_current_delivery_count")
    void nukeTable();

    @Query("select sum(current_committed) from tbl_current_delivery_count")
    int getCurrentDeliveryTotal();
}
