package ws1718.gdb.sis.entity;

import edu.whs.dba.entity.Studienrichtung;
import java.util.Objects;

/**
 *
 * @author hnzo
 */
public class eStudienrichtung implements Studienrichtung {

    private final String kuerzel;
    private final String name;

    public eStudienrichtung(String kuerzel, String name) {
        this.kuerzel = kuerzel;
        this.name = name;
    }
      
    @Override
    public String getKuerzel() {
        return kuerzel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "" + name + " (" + kuerzel + ")";
    }

    @Override
    public boolean equals(Object obj) {
        boolean state = false;
        if(obj instanceof Studienrichtung) {
            if(this.name.equals(((Studienrichtung) obj).getName()) && 
                    this.kuerzel.equals(((Studienrichtung) obj).getKuerzel())) {
                state = true;
            }
        }
        
        return state;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(kuerzel);
        hash = 29 * hash + Objects.hashCode(name);
        
        return hash;
    }
    
    
    
    
}
