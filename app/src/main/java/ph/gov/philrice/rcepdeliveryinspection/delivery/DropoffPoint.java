package ph.gov.philrice.rcepdeliveryinspection.delivery;

public class DropoffPoint {
    private int dropoffPointId;
    private String region;
    private String province;
    private String municipality;
    private String dropOffPoint;

    public DropoffPoint(int dropoffPointId, String region, String province, String municipality, String dropOffPoint) {
        this.dropoffPointId = dropoffPointId;
        this.region = region;
        this.province = province;
        this.municipality = municipality;
        this.dropOffPoint = dropOffPoint;
    }

    public int getDropoffPointId() {
        return dropoffPointId;
    }

    public void setDropoffPointId(int dropoffPointId) {
        this.dropoffPointId = dropoffPointId;
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
}
