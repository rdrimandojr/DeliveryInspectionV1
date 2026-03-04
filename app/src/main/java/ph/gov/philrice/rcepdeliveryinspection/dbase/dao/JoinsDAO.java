package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.inspection.dataview.SGDeliveryData;

@Dao
public interface JoinsDAO {

    @Query("select batchTicketNumber,group_concat(distinct(seedVariety)) as variety,sum(totalBagCount) as totalBags,deliveryDate,province,municipality,dropOffpoint as dropoffPoint,sum(actualTotal) as actualTotal from tbl_delivery group by batchTicketNumber order by deliveryId desc")
    List<SGDeliveryData> getDeliveryDetails();

    @Query("select batchTicketNumber,group_concat(distinct(seedVariety)) as variety,sum(totalBagCount) as totalBags,deliveryDate,province,municipality,dropOffpoint as dropoffPoint,sum(actualTotal) as actualTotal from tbl_delivery_inspection group by batchTicketNumber order by deliveryId desc")
    List<SGDeliveryData> getDeliveryInspectionDetails();

}
