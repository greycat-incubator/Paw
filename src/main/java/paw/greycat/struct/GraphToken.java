package paw.greycat.struct;

public class GraphToken {
    final String cacheSub;
    final int id;


    public GraphToken( String cacheSub, int id) {
        this.cacheSub = cacheSub;
        this.id = id;

    }

    public String getCacheSub() {
        return cacheSub;
    }

    public int getId() {
        return id;
    }

}
