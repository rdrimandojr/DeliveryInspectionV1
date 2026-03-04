package ph.gov.philrice.rcepdeliveryinspection.dbase.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.DeliveryDataDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.InspectionDataDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.JoinsDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.LibDropoffPointDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.LibSeedsDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblActualDeliveryDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblCommitmentDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblCurrentDeliveryCountDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblDeliveryDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblDeliveryInspectionDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblDeliveryStatusAppLocalDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblDeliveryStatusDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblInspectionDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblSamplingDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblSendingStatusDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblTempSamplingDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TblTotalCommitmentDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TmpDeliveryBatchDataDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.dao.TmpSgDeliveryTotalDAO;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.LibDropoffPoint;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.LibSeeds;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblActualDelivery;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblCommitment;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblCurrentDeliveryCount;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDelivery;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryInspection;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryStatus;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblDeliveryStatusAppLocal;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblInspection;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblSampling;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblSendingStatus;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblTempSampling;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TblTotalCommitment;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TmpDeliveryBatchData;
import ph.gov.philrice.rcepdeliveryinspection.dbase.tables.TmpSgDeliveryTotal;

@Database(entities = {
        LibSeeds.class, TblDelivery.class, TblInspection.class, TblSampling.class,
        LibDropoffPoint.class, TblDeliveryStatus.class, TblActualDelivery.class,
        TblDeliveryStatusAppLocal.class, TblTempSampling.class, TblSendingStatus.class,
        TblCommitment.class, TblTotalCommitment.class, TmpDeliveryBatchData.class,
        TblCurrentDeliveryCount.class, TblDeliveryInspection.class, TmpSgDeliveryTotal.class},
        version = 10,
        exportSchema = false)
public abstract class RCEPDatabase extends RoomDatabase {

    public abstract TmpSgDeliveryTotalDAO tmpSgDeliveryTotalDAO();

    public abstract TblDeliveryInspectionDAO tblDeliveryInspectionDAO();

    public abstract TblCurrentDeliveryCountDAO tblCurrentDeliveryCountDAO();

    public abstract TmpDeliveryBatchDataDAO tmpDeliveryBatchDataDAO();

    public abstract LibSeedsDAO libSeedsDAO();

    public abstract TblDeliveryDAO tblDeliveryDAO();

    public abstract TblInspectionDAO tblInspectionDAO();

    public abstract TblSamplingDAO tblSamplingDAO();

    public abstract LibDropoffPointDAO libDropoffPointDAO();

    public abstract TblDeliveryStatusDAO tblDeliveryStatusDAO();

    public abstract TblActualDeliveryDAO tblActualDeliveryDAO();

    public abstract TblDeliveryStatusAppLocalDAO tblDeliveryStatusAppLocalDAO();

    public abstract TblTempSamplingDAO tblTempSamplingDAO();

    public abstract TblSendingStatusDAO tblSendingStatusDAO();

    public abstract TblCommitmentDAO tblCommitmentDAO();

    public abstract TblTotalCommitmentDAO tblTotalCommitmentDAO();

    ////////////////////////////////////////////////

    public abstract DeliveryDataDAO deliveryDataDAO();

    public abstract InspectionDataDAO inspectionDataDAO();

    public abstract JoinsDAO joinsDAO();

    //SINGLETON
    private static RCEPDatabase INSTANCE;

    public static RCEPDatabase getAppDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RCEPDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    synchronized (RCEPDatabase.class) {
                        if (INSTANCE == null) {
                            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    RCEPDatabase.class, "rcep_delivery_inspection")
                                    .allowMainThreadQueries()
                                    .fallbackToDestructiveMigration()
                                    .build();
                        }
                    }
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
