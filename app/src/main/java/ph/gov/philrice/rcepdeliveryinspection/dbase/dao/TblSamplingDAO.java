package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblSampling;

@Dao
public interface TblSamplingDAO {
    @Insert
    void insertSampling(TblSampling tblSampling);

    @Query("delete from tbl_sampling")
    void nukeTable();

    @Query("delete from tbl_sampling where batchTicketNumber =:batchTicketNumber")
    void reset(String batchTicketNumber);

    @Query("select * from tbl_sampling where batchTicketNumber =:batchTicketNumber")
    List<TblSampling> getSamplingByBatch(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_sampling where sendLocal = 1 and batchTicketNumber =:batchTicketNumber")
        //if result > 0 ? send data local server : already sent local server
    int sendLocal(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_sampling where sendLocal = 1")
        //if result > 0 ? send data local server : already sent local server
    int sendLocalAll();

    @Query("select count(batchTicketNumber) from tbl_sampling where sendCentral= 1 and batchTicketNumber =:batchTicketNumber")
        //if result > 0 ? send data central server : already sent central server
    int sendCentral(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_sampling where sendCentral = 1")
        //if result > 0 ? send data local server : already sent local server
    int sendCentralAll();

    @Query("update tbl_sampling set sendLocal = 0 where batchTicketNumber =:batchTicketNumber")
        //flags current batch to 0 if successfully sent all data to local server
    void successSendingLocal(String batchTicketNumber);

    @Query("update tbl_sampling set sendCentral = 0 where batchTicketNumber =:batchTicketNumber")
        //flags current batch to 0 if successfully sent all data to central server
    void successSendingCentral(String batchTicketNumber);

    @Query("delete from tbl_sampling where batchTicketNumber =:batchTicketNumber")
    void removeBatchData(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_sampling where batchTicketNumber =:batchTicketNumber and seedTag =:seedTag and dateSampled =:dateSampled")
    int hasSamplingData(String batchTicketNumber,String seedTag, String dateSampled);
}
