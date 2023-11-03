package arthessia.featurebox.objects;

public class PlayerCount {

    private String id;
    private Integer countZombies;

    public PlayerCount(
            String id,
            Integer countZombies) {

        this.id = id;
        this.countZombies = countZombies;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCountZombies() {
        return countZombies;
    }

    public void setCountZombies(Integer countZombies) {
        this.countZombies = countZombies;
    }
}
