package ph.gov.philrice.rcepdeliveryinspection.objects;

public class SeedGrower {
    int sg_id;
    String coop_accred;
    String name;

    public SeedGrower(int sg_id, String coop_accred, String name) {
        this.sg_id = sg_id;
        this.coop_accred = coop_accred;
        this.name = name;
    }

    public int getSg_id() {
        return sg_id;
    }

    public void setSg_id(int sg_id) {
        this.sg_id = sg_id;
    }

    public String getCoop_accred() {
        return coop_accred;
    }

    public void setCoop_accred(String coop_accred) {
        this.coop_accred = coop_accred;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
