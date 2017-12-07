/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws1718.gdb.sis.entity;

import edu.whs.gdb.entity.Student;
import java.util.Objects;

/**
 *
 * @author hnzo
 */
public class eStudent implements Student{

    private String matrikelNr;
    private String name;
    private String vorname;
    private String adresse;
    private String studienrichtung;
       
    
    @Override
    public String getMatrikel() {
        return matrikelNr;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVorname() {
        return vorname;
    }

    @Override
    public String getAdresse() {
        return adresse;
    }

    @Override
    public String getStudienrichtungKuerzel() {
        return studienrichtung;
    }

    @Override
    public boolean equals(Object obj) {
        boolean state = false;
        if(obj instanceof Student) {
            if(this.matrikelNr.equals(((Student) obj).getMatrikel())) {
                state = true;
            }
        }
        
        
        return state;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 29 + Objects.hashCode(matrikelNr);
        hash = hash * 29 + Objects.hashCode(name);
        hash = hash * 29 + Objects.hashCode(vorname);
        
        return hash;
    }

    @Override
    public String toString() {
        return matrikelNr + " | " + name + ", " + vorname;
    }
    
    
}
