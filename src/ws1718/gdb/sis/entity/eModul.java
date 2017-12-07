/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws1718.gdb.sis.entity;

import edu.whs.gdb.entity.Modul;
import java.util.Objects;

/**
 *
 * @author hnzo
 */
public class eModul implements Modul {
    
    private final String kuerzel;
    private final String name;
    private final int vorlesung, uebung, praktikum, credits;
    
    public eModul(String kuerzel, String name,int vorlesung,int uebung,int praktikum,int credits) {
        this.kuerzel = kuerzel;
        this.name = name;
        this.vorlesung = vorlesung;
        this.uebung = uebung;
        this.praktikum = praktikum;
        this.credits = credits;
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
    public int getVorlesung() {
        return vorlesung;
    }

    @Override
    public int getUebung() {
        return uebung;
    }

    @Override
    public int getPraktikum() {
        return praktikum;
    }

    @Override
    public int getCredits() {
        return credits;
    }

    @Override
    public boolean equals(Object obj) {
        boolean state = false;
        if (obj instanceof Modul) {
            if(this.name.equals(((Modul)obj).getName()) && this.kuerzel.equals(((Modul) obj).getKuerzel())) {
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

    @Override
    public String toString() {
        return name + " (" + kuerzel + ")";
    }

    
}
