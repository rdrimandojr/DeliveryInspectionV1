package ph.gov.philrice.rcepdeliveryinspection.dbase.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblInspection;

@Dao
public interface TblInspectionDAO {
    @Insert
    void insertInspection(TblInspection tblInspection);

    @Query("delete from tbl_inspection")
    void nukeTable();

    @Query("delete from tbl_inspection where batchTicketNumber =:batchTicketNumber")
    void reset(String batchTicketNumber);

    @Query("select * from tbl_inspection where batchTicketNumber =:batchTicketNumber order by inspectionId desc limit 1")
    List<TblInspection> getInspectionByBatch(String batchTicketNumber);

    @Query("select * from tbl_inspection where batchTicketNumber =:batchTicketNumber order by inspectionId desc limit 1")
    TblInspection getInspectionByBatch2(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_inspection where sendLocal = 1 and batchTicketNumber =:batchTicketNumber")
        //if result > 0 ? send data local server : already sent local server
    int sendLocal(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_inspection where sendLocal = 1")
        //if result > 0 ? send data local server : already sent local server
    int sendLocalAll();

    @Query("select count(batchTicketNumber) from tbl_inspection where sendCentral= 1 and batchTicketNumber =:batchTicketNumber")
        //if result > 0 ? send data central server : already sent central server
    int sendCentral(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_inspection where sendCentral = 1")
        //if result > 0 ? send data local server : already sent local server
    int sendCentralAll();

    @Query("update tbl_inspection set sendLocal = 0 where batchTicketNumber =:batchTicketNumber")
        //flags current batch to 0 if successfully sent all data to local server
    void successSendingLocal(String batchTicketNumber);

    @Query("update tbl_inspection set sendCentral = 0 where batchTicketNumber =:batchTicketNumber")
        //flags current batch to 0 if successfully sent all data to central server
    void successSendingCentral(String batchTicketNumber);

    @Query("update tbl_inspection set accountingImage =:accountingImage, accountingImage_path=:accountingImage_path,dr_date=:dr_date,dr_number=:dr_number where batchTicketNumber =:batchTicketNumber")
        //flags current batch to 0 if successfully sent all data to central server
    void updateAccountingAttachment(String batchTicketNumber, String accountingImage,
                                    String accountingImage_path,
                                    String dr_date, String dr_number);

    @Query("delete from tbl_inspection where batchTicketNumber =:batchTicketNumber")
    void removeBatchData(String batchTicketNumber);

    @Query("select count(batchTicketNumber) from tbl_inspection where batchTicketNumber =:batchTicketNumber")
    int hasInspectionData(String batchTicketNumber);
}
