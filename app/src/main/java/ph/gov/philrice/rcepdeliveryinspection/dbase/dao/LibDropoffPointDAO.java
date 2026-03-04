package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.LibDropoffPoint;

@Dao
public interface LibDropoffPointDAO {
    @Insert
    void insertDropoffPoint(LibDropoffPoint libDropoffPoint);

    @Query("delete from lib_dropoff_point")
    void nukeTable();

    @Query("select count(dropoffPointId) from lib_dropoff_point where dropoffPointId =:dropoffPointId")
    int checkDropoffPoint(int dropoffPointId);

    @Query("select distinct(region) from lib_dropoff_point order by region asc")
    List<String> getDropoffRegions();

    @Query("select distinct(province) from lib_dropoff_point where region =:region order by province asc")
    List<String> getDropoffProvinces(String region);

    @Query("select distinct(municipality) from lib_dropoff_point where region =:region and province =:province order by municipality")
    List<String> getDropoffMunicipalities(String region, String province);

    @Query("select distinct(dropOffPoint) from lib_dropoff_point where dropOffPoint!='' and region =:region and province =:province and municipality =:municipality order by dropOffPoint")
    List<String> getDropoffPoints(String region, String province, String municipality);

  /*  @Query("select `dropoffPointId`,`region`,`province`,`municipality`,`dropOffPoint` from lib_dropoff_point where region =:region and province =:province and municipality =:municipality order by dropOffPoint")
    List<DropoffPoint> getDropoffPoints(String region, String province, String municipality);*/



}
