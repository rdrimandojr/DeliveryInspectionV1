package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tbl_current_delivery_count")
public class TblCurrentDeliveryCount {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String coopAccreditation;
    private String moa_number;
    private String region;
    private String province;
    private String municipality;
    private String seedVariety;
    private int current_committed;

    public TblCurrentDeliveryCount(String coopAccreditation, String moa_number, String region, String province, String municipality, String seedVariety, int current_committed) {
        this.coopAccreditation = coopAccreditation;
        this.moa_number = moa_number;
        this.region = region;
        this.province = province;
        this.municipality = municipality;
        this.seedVariety = seedVariety;
        this.current_committed = current_committed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCoopAccreditation() {
        return coopAccreditation;
    }

    public void setCoopAccreditation(String coopAccreditation) {
        this.coopAccreditation = coopAccreditation;
    }

    public String getMoa_number() {
        return moa_number;
    }

    public void setMoa_number(String moa_number) {
        this.moa_number = moa_number;
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

    public String getSeedVariety() {
        return seedVariety;
    }

    public void setSeedVariety(String seedVariety) {
        this.seedVariety = seedVariety;
    }

    public int getCurrent_committed() {
        return current_committed;
    }

    public void setCurrent_committed(int current_committed) {
        this.current_committed = current_committed;
    }
}
