package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbl_actual_delivery")
public class TblActualDelivery {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int actualDeliveryId;
    private String batchTicketNumber;
    private String region;
    private String province;
    private String municipality;
    private String dropOffPoint;
    private String seedVariety;
    private int totalBagCount;
    private String dateCreated;
    private int send;
    private String seedTag;
    //
    private String prv_dropoff_id;
    private String prv;
    private String moa_number;
    private String app_version;
    private String batchSeries;
    private int sendLocal;
    private int sendCentral;
    private String remarks;
    private int isRejected;
    private int isDownloaded;
    private int hasRLA;
    private String sack_code;
    private int misBuffer;
    private String QRValStart;
    private String QRValEnd;
    private int QRStart;
    private int QREnd;


    public TblActualDelivery(String batchTicketNumber, String region, String province,
                             String municipality, String dropOffPoint, String seedVariety,
                             int totalBagCount, String dateCreated, int send, String seedTag,
                             String prv_dropoff_id, String prv, String moa_number,
                             String app_version, String batchSeries, int sendLocal, int sendCentral,
                             String remarks, int isRejected, int isDownloaded, int hasRLA,
                             String sack_code, int misBuffer, String QRValStart, String QRValEnd,
                             int QRStart, int QREnd) {
        this.batchTicketNumber = batchTicketNumber;
        this.region = region;
        this.province = province;
        this.municipality = municipality;
        this.dropOffPoint = dropOffPoint;
        this.seedVariety = seedVariety;
        this.totalBagCount = totalBagCount;
        this.dateCreated = dateCreated;
        this.send = send;
        this.seedTag = seedTag;
        this.prv_dropoff_id = prv_dropoff_id;
        this.prv = prv;
        this.moa_number = moa_number;
        this.app_version = app_version;
        this.batchSeries = batchSeries;
        this.sendLocal = sendLocal;
        this.sendCentral = sendCentral;
        this.remarks = remarks;
        this.isRejected = isRejected;
        this.isDownloaded = isDownloaded;
        this.hasRLA = hasRLA;
        this.sack_code = sack_code;
        this.misBuffer = misBuffer;
        this.QRValStart = QRValStart;
        this.QRValEnd = QRValEnd;
        this.QRStart = QRStart;
        this.QREnd = QREnd;
    }

    public String getQRValStart() {
        return QRValStart;
    }

    public void setQRValStart(String QRValStart) {
        this.QRValStart = QRValStart;
    }

    public String getQRValEnd() {
        return QRValEnd;
    }

    public void setQRValEnd(String QRValEnd) {
        this.QRValEnd = QRValEnd;
    }

    public int getQRStart() {
        return QRStart;
    }

    public void setQRStart(int QRStart) {
        this.QRStart = QRStart;
    }

    public int getQREnd() {
        return QREnd;
    }

    public void setQREnd(int QREnd) {
        this.QREnd = QREnd;
    }

    public int getMisBuffer() {
        return misBuffer;
    }

    public void setMisBuffer(int misBuffer) {
        this.misBuffer = misBuffer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActualDeliveryId() {
        return actualDeliveryId;
    }

    public void setActualDeliveryId(int actualDeliveryId) {
        this.actualDeliveryId = actualDeliveryId;
    }

    public String getBatchTicketNumber() {
        return batchTicketNumber;
    }

    public void setBatchTicketNumber(String batchTicketNumber) {
        this.batchTicketNumber = batchTicketNumber;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getDropOffPoint() {
        return dropOffPoint;
    }

    public void setDropOffPoint(String dropOffPoint) {
        this.dropOffPoint = dropOffPoint;
    }

    public String getSeedVariety() {
        return seedVariety;
    }

    public void setSeedVariety(String seedVariety) {
        this.seedVariety = seedVariety;
    }

    public int getTotalBagCount() {
        return totalBagCount;
    }

    public void setTotalBagCount(int totalBagCount) {
        this.totalBagCount = totalBagCount;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getSend() {
        return send;
    }

    public void setSend(int send) {
        this.send = send;
    }

    public String getSeedTag() {
        return seedTag;
    }

    public void setSeedTag(String seedTag) {
        this.seedTag = seedTag;
    }

    public String getPrv_dropoff_id() {
        return prv_dropoff_id;
    }

    public void setPrv_dropoff_id(String prv_dropoff_id) {
        this.prv_dropoff_id = prv_dropoff_id;
    }

    public String getPrv() {
        return prv;
    }

    public void setPrv(String prv) {
        this.prv = prv;
    }

    public String getMoa_number() {
        return moa_number;
    }

    public void setMoa_number(String moa_number) {
        this.moa_number = moa_number;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getBatchSeries() {
        return batchSeries;
    }

    public void setBatchSeries(String batchSeries) {
        this.batchSeries = batchSeries;
    }

    public int getSendLocal() {
        return sendLocal;
    }

    public void setSendLocal(int sendLocal) {
        this.sendLocal = sendLocal;
    }

    public int getSendCentral() {
        return sendCentral;
    }

    public void setSendCentral(int sendCentral) {
        this.sendCentral = sendCentral;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getIsRejected() {
        return isRejected;
    }

    public void setIsRejected(int isRejected) {
        this.isRejected = isRejected;
    }

    public int getIsDownloaded() {
        return isDownloaded;
    }

    public void setIsDownloaded(int isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public int getHasRLA() {
        return hasRLA;
    }

    public void setHasRLA(int hasRLA) {
        this.hasRLA = hasRLA;
    }

    public String getSack_code() {
        return sack_code;
    }

    public void setSack_code(String sack_code) {
        this.sack_code = sack_code;
    }
}
