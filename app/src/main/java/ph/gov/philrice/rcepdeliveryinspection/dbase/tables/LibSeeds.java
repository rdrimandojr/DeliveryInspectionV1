package ph.gov.philrice.rcepdeliveryinspection.dbase.tables;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lib_seeds")
public class LibSeeds {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String variety;

    public LibSeeds(String variety) {
        this.variety = variety;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }
}
