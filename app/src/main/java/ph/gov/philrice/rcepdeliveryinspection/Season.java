package ph.gov.philrice.rcepdeliveryinspection;

public class Season {
    private int seasonId;
    private String season;

    public Season(int seasonId, String season) {
        this.seasonId = seasonId;
        this.season = season;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }
}
