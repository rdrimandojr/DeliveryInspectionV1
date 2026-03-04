package ph.gov.philrice.rcepdeliveryinspection.objects;

public class CoopDropoffPoint {
    int dropoffPointId;
    String prv_dropoff_id;
    String coop_accreditation;
    String region;
    String province;
    String municipality;
    String dropOffPoint;
    String prv;
    String moa_number;
    int status;
    int instructed_delivery_volume;
    String delivery_date;
    String batchTicketNumber;
    int misBuffer;
    String seed_distribution_mode;

    public CoopDropoffPoint(int dropoffPointId, String prv_dropoff_id, String coop_accreditation,
                            String region, String province, String municipality,
                            String dropOffPoint, String prv, String moa_number, int status,
                            int instructed_delivery_volume, String delivery_date,
                            String batchTicketNumber, int misBuffer,String seed_distribution_mode) {
        this.dropoffPointId = dropoffPointId;
        this.prv_dropoff_id = prv_dropoff_id;
        this.coop_accreditation = coop_accreditation;
        this.region = region;
        this.province = province;
        this.municipality = municipality;
        this.dropOffPoint = dropOffPoint;
        this.prv = prv;
        this.moa_number = moa_number;
        this.status = status;
        this.instructed_delivery_volume = instructed_delivery_volume;
        this.delivery_date = delivery_date;
        this.batchTicketNumber = batchTicketNumber;
        this.misBuffer = misBuffer;
        this.seed_distribution_mode = seed_distribution_mode;
    }

    public int getMisBuffer() {
        return misBuffer;
    }

    public void setMisBuffer(int misBuffer) {
        this.misBuffer = misBuffer;
    }

    public String getBatchTicketNumber() {
        return batchTicketNumber;
    }

    public void setBatchTicketNumber(String batchTicketNumber) {
        this.batchTicketNumber = batchTicketNumber;
    }

    public String getDelivery_date() {
        return delivery_date;
    }

    public void setDelivery_date(String delivery_date) {
        this.delivery_date = delivery_date;
    }

    public int getDropoffPointId() {
        return dropoffPointId;
    }

    public void setDropoffPointId(int dropoffPointId) {
        this.dropoffPointId = dropoffPointId;
    }

    public String getPrv_dropoff_id() {
        return prv_dropoff_id;
    }

    public void setPrv_dropoff_id(String prv_dropoff_id) {
        this.prv_dropoff_id = prv_dropoff_id;
    }

    public String getCoop_accreditation() {
        return coop_accreditation;
    }

    public void setCoop_accreditation(String coop_accreditation) {
        this.coop_accreditation = coop_accreditation;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getInstructed_delivery_volume() {
        return instructed_delivery_volume;
    }

    public void setInstructed_delivery_volume(int instructed_delivery_volume) {
        this.instructed_delivery_volume = instructed_delivery_volume;
    }

    public String getSeed_distribution_mode() {
        return seed_distribution_mode;
    }

    public void setSeed_distribution_mode(String seed_distribution_mode) {
        this.seed_distribution_mode = seed_distribution_mode;
    }
}