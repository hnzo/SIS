/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws1718.gdb.sis.entity;

import edu.whs.gdb.entity.Modul;
import edu.whs.gdb.entity.Praktikumsteilnahme;
import edu.whs.gdb.entity.Student;
import java.util.Objects;

/**
 *
 * @author hnzo
 */
public class ePraktikumsteilnahme implements Praktikumsteilnahme {
    private eStudent student;
    private eModul modul;
    private String semester;
    private boolean testat;

    public ePraktikumsteilnahme(eStudent student, eModul modul, String semester, boolean testat) {
        this.student = student;
        this.modul = modul;
        this.semester = semester;
        this.testat = testat;
    }

    @Override
    public Student getStudent() {
        return student;
    }

    @Override
    public Modul getModul() {
        return modul;
    }

    @Override
    public String getSemester() {
        return semester;
    }

    @Override
    public boolean isTestat() {
        return testat;
    }

    @Override
    public void setTestat(boolean bln) {
        this.testat = bln;
    }

    @Override
    public boolean equals(Object obj) {
        boolean state = false;
        if(obj instanceof Praktikumsteilnahme) {
            if(this.student.equals(((Praktikumsteilnahme) obj).getStudent()) &&
                    this.modul.equals(((Praktikumsteilnahme) obj).getModul())) {
                state = true;
            }
        }
        return state;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 29 + Objects.hashCode(student);
        hash = hash * 29 + Objects.hashCode(modul);
        hash = hash * 29 + Objects.hashCode(semester);
        
        return hash; 
    }

    @Override
    public String toString() {
        return student.toString() + ", nimmt an "+ modul.toString() + " teil.";
    }
    
    
    
}
