package ws1718.gdb.sis;

import edu.whs.gdb.ApplicationException;
import edu.whs.gdb.DataAccessObject;
import edu.whs.gdb.entity.Modul;
import edu.whs.gdb.entity.Praktikumsteilnahme;
import edu.whs.gdb.entity.Student;
import edu.whs.gdb.entity.Studienrichtung;
import ws1718.gdb.sis.entity.eModul;
import ws1718.gdb.sis.entity.ePraktikumsteilnahme;
import ws1718.gdb.sis.entity.eStudent;
import ws1718.gdb.sis.entity.eStudienrichtung;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;

/**
 * @author hnzo
 */
class implDAO implements DataAccessObject {

    private Connection con = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    private final static String conURL = "jdbc:derby:D:/OneDrive/Studium/GDB/WS17-18/gdb-Aufgabe6-Bibliotheken/gdb-praktikum";

    implDAO() {
        try {
            con = DriverManager.getConnection(conURL);
            con.setAutoCommit(false);
        } catch (SQLException ex) {
            System.out.println("Connection Failed");
            ex.printStackTrace();
        }
    }

    private void rollback() {
        if (con != null) {
            try {
                System.out.println("Transaction being rolled back.");
                con.rollback();
            } catch (SQLException exc) {
                System.err.println("SQL Exception occured: " + exc.getMessage());
            }
        }
    }

    @Override
    public List<List<String>> getStudienverlaufsplan(Studienrichtung s) throws ApplicationException {
        List<List<String>> verlaufsplan = new ArrayList<>();
        ArrayList<String> ersteZeile = new ArrayList<>();
        ArrayList<String> zweiteZeile = new ArrayList<>();
        ArrayList<String> vierteZeile = new ArrayList<>();
        int[] swsSumme = new int[6];


        //ERSTE ZEILE IM STUDIENVERLAUFSPLAN
        ersteZeile.add("Studienverlaufsplan\n" + s.toString() + "\t");
        for (int i = 0; i < 6; i++) {
            ersteZeile.add((i + 1) + ". Semester");
        }
        ersteZeile.add(" ");
        verlaufsplan.add(ersteZeile);

        //ZWEITE ZEILE IM STUDIENVERLAUFSPLAN
        zweiteZeile.add("Kategorie");
        for (int i = 0; i < 6; i++) {
            zweiteZeile.add("Mod    V    Ü    P    Cr");
        }
        zweiteZeile.add("Summe");
        verlaufsplan.add(zweiteZeile);
        //DRITTE ZEILE IM STUDIENVERLAUFSPLAN

        ArrayList<String> kname = new ArrayList<>();
        ArrayList<String> kkuerzel = new ArrayList<>();

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT DISTINCT K.LFDNR, K.NAME, K.KKUERZEL\n" +
                    "FROM APP.KATEGORIE K, APP.MODUL M, APP.STUDIENRICHTUNG S, APP.VERLAUFSPLAN V\n" +
                    "WHERE K.KKUERZEL = M.KKUERZEL AND M.MKUERZEL = V.MKUERZEL AND V.SKUERZEL = S.SKUERZEL " +
                    "AND S.SKUERZEL =" + "'" + s.getKuerzel() + "'");
            while (rs.next()) {
                kname.add(rs.getString(2));
                kkuerzel.add(rs.getString(3));
            }

            for (int i = 0; i < kname.size(); i++) {
                ArrayList<String> dritteZeile = new ArrayList<>();
                dritteZeile.add(kname.get(i));
                int stunden = 0;
                for (int b = 1; b <= 6; b++) {

                    rs = stmt.executeQuery("SELECT DISTINCT M.MKUERZEL, M.VL, M.UB, M.PR, M.CREDITS\n" +
                            "FROM APP.KATEGORIE K, APP.MODUL M, APP.STUDIENRICHTUNG S, APP.VERLAUFSPLAN V\n" +
                            "WHERE M.MKUERZEL = V.MKUERZEL AND V.SKUERZEL = S.SKUERZEL AND S.SKUERZEL =" + "'" + s.getKuerzel() + "'" +
                            "AND M.KKUERZEL =" + "'" + kkuerzel.get(i) + "'" + "AND V.SEM=" + b);

                    String info = "";
                    while (rs.next()) {
                        StringBuilder sb = new StringBuilder();
                        swsSumme[b - 1] = swsSumme[b - 1] + rs.getInt(2) + rs.getInt(3) + rs.getInt(4);
                        info = sb.append(info).append(rs.getString(1)).append("    ").
                                append(rs.getInt(2)).append("    ").append(rs.getInt(3)).append("    ").
                                append(rs.getInt(4)).append("    ").append(rs.getInt(5)).append("\n").toString();
                        stunden = stunden + rs.getInt(2) + rs.getInt(3) + rs.getInt(4);
                    }
                    dritteZeile.add(info);

                }
                dritteZeile.add("" + stunden);
                verlaufsplan.add(dritteZeile);
            }
            con.commit();
        } catch (SQLException e) {
            System.err.println("Die Kategorien konnten nicht geladen werden.");
            e.printStackTrace();
            rollback();
        } finally {
            try{
                if( rs != null ) {
                    rs.close();
                }
            } catch( SQLException ignore ) {}
            try{
                if( stmt!= null ) {
                    stmt.close();
                }
            } catch( SQLException ignore ) {}

        }

        vierteZeile.add("Summe SWS");
        int Summe = 0;

        for (int i = 0; i <= 5; i++) {
            vierteZeile.add("" + swsSumme[i]);
            Summe = Summe + swsSumme[i];
        }

        vierteZeile.add("" + Summe);
        verlaufsplan.add(vierteZeile);


        return verlaufsplan;
    }

    @Override
    public void addStudent(String matrikelNr, String name, String vorname, String adresse, String skuerzel) throws ApplicationException {

        eStudent stud = new eStudent(matrikelNr, name, vorname, adresse, skuerzel);
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO APP.STUDENT(MATRIKEL, NAME, VORNAME, ADRESSE, SKUERZEL)" +
                    " VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, stud.getMatrikel());
            ps.setString(2, stud.getName());
            ps.setString(3, stud.getVorname());
            ps.setString(4, stud.getAdresse());
            ps.setString(5, stud.getStudienrichtungKuerzel());
            ps.executeUpdate();
            con.commit();
            ps.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ApplicationException("Diese Matrikelnummer ist schon vergeben.");
        } catch (SQLException e) {
            System.err.println("Student konnte nicht hinzugefügt werden.");
            e.printStackTrace();
            rollback();
        } finally {
            try{
                if( rs != null ) {
                    rs.close();
                }
            } catch( SQLException ignore ) {}
            try{
                if( stmt!= null ) {
                    stmt.close();
                }
            } catch( SQLException ignore ) {}

        }
    }

    @Override
    public Collection<Student> getAllStudent() {
        Collection<Student> students = new ArrayList<>();

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM APP.STUDENT");

            while (rs.next()) {
                students.add(new eStudent(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
            }
            con.commit();
        } catch (SQLException e) {
            System.err.println("Die Studenten konnten nicht ausgegeben werden.");
            e.printStackTrace();
            rollback();
        }finally {
            try{
                if( rs != null ) {
                    rs.close();
                }
            } catch( SQLException ignore ) {}
            try{
                if( stmt!= null ) {
                    stmt.close();
                }
            } catch( SQLException ignore ) {}

        }
        return students;
    }

    @Override
    public boolean enroll(String matrikelnr, String name, String vorname, String adresse, String skuerzel, Modul modul, String semester) throws ApplicationException {
        boolean erg = true;

        String modulTest = "SELECT MKUERZEL FROM APP.VERLAUFSPLAN WHERE SKUERZEL = ? AND MKUERZEL = ?";
        String prTest = "SELECT PR FROM APP.MODUL WHERE MKUERZEL = ? AND PR > 0 ";
        String anmeldungTest = "SELECT * FROM APP.PRAKTIKUMSTEILNAHME WHERE MATRIKEL = ? AND MKUERZEL = ? AND SEMESTER = ?";
        String richtungTest = "SELECT * FROM APP.STUDIENRICHTUNG  WHERE SKUERZEL = ?";
//        String matrikelTest = "SELECT * FROM STUDENT WHERE MATRIKEL = ?";
        String insert = "INSERT INTO APP.PRAKTIKUMSTEILNAHME(MATRIKEL, MKUERZEL, SEMESTER, TESTAT) VALUES (?,?,?,?)";

        try{
            try {
                addStudent(matrikelnr, name, vorname, adresse, skuerzel);
            } catch (ApplicationException e) {
                System.out.println("Student vorhanden");
            }
            PreparedStatement ps = con.prepareStatement(modulTest);
            ps.setString(1, skuerzel);
            ps.setString(2, modul.getKuerzel());
            rs = ps.executeQuery();
            if(!rs.next()) {
                erg = false;
                throw new ApplicationException("Das übergebene Modul ist nicht Bestandteil der Studienrichtung!");
            }

            ps = con.prepareStatement(prTest);
            ps.setString(1, modul.getKuerzel());
            rs = ps.executeQuery();
            if(!rs.next()) {
                erg = false;
                throw new ApplicationException("Das übergebene Modul sieht kein Praktikum vor.");
            }

            ps = con.prepareStatement(anmeldungTest);
            ps.setString(1, matrikelnr);
            ps.setString(2, modul.getKuerzel());
            ps.setString(3, semester);
            rs = ps.executeQuery();
            if(rs.next()) {
                erg = false;
                throw new ApplicationException("Der Teilnehmer ist schon in der Datenbank vorhanden!");
            }

            ps = con.prepareStatement(richtungTest);
            ps.setString(1, skuerzel);
            rs = ps.executeQuery();
            if(!rs.next()) {
                erg = false;
                throw new ApplicationException("Es wurde keine oder eine nicht erfasste Studienrichtung angegeben!");
            }

            if(erg) {
                PreparedStatement psInsert = con.prepareStatement(insert);
                psInsert.setString(1,matrikelnr);
                psInsert.setString(2, modul.getKuerzel());
                psInsert.setString(3, semester);
                psInsert.setBoolean(4, false);

                psInsert.executeUpdate();
                psInsert.close();
            }
            con.commit();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            rollback();
        } finally {
            try{
                if( rs != null ) {
                    rs.close();
                }
            } catch( SQLException ignore ) {}
            try{
                if( stmt!= null ) {
                    stmt.close();
                }
            } catch( SQLException ignore ) {}
        }

        return erg;
    }

    @Override
    public void setTestate(Collection<Praktikumsteilnahme> clctn) {
        PreparedStatement ps = null;
        try {
            for(Praktikumsteilnahme pt : clctn) {
                ps = con.prepareStatement("UPDATE APP.PRAKTIKUMSTEILNAHME SET TESTAT = TRUE " +
                        "WHERE MATRIKEL = ? AND MKUERZEL = ? AND SEMESTER = ?");
                ps.setString(1, pt.getStudent().getMatrikel());
                ps.setString(2, pt.getModul().getKuerzel());
                ps.setString(3, pt.getSemester());
                ps.executeUpdate();
            }
            con.commit();


        } catch (SQLException e) {
            System.err.println("Die Testate konnten nicht gesetzt werden.");
            e.printStackTrace();
            rollback();
        } finally {
            try{
                if( rs != null ) {
                    rs.close();
                }
            } catch( SQLException ignore ) {}
            try{
                if( stmt!= null ) {
                    stmt.close();
                }
            } catch( SQLException ignore ) {}
            try{
                if( ps!= null ) {
                    ps.close();
                }
            } catch( SQLException ignore ) {}
        }
    }

    @Override
    public Collection<Studienrichtung> getAllStudienrichtung() {
        Collection<Studienrichtung> fach = new ArrayList<>();

        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM APP.STUDIENRICHTUNG");
            while (rs.next()) {
                fach.add(new eStudienrichtung(rs.getString(1), rs.getString(2)));
            }
            con.commit();
        } catch (SQLException ex) {
            System.err.println("Eine Studienrichtung konnte nicht geladen werden");
            ex.printStackTrace();
            rollback();
        } finally {
            try{
                if( rs != null ) {
                    rs.close();
                }
            } catch( SQLException ignore ) {}
            try{
                if( stmt!= null ) {
                    stmt.close();
                }
            } catch( SQLException ignore ) {}

        }

        return fach;
    }

    @Override
    public Collection<Modul> getAllModul() {
        Collection<Modul> alleModule = new ArrayList<>();

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM APP.MODUL");
            while (rs.next()) {
                alleModule.add(new eModul(rs.getString(1), rs.getString(2), rs.getInt(3),
                        rs.getInt(4), rs.getInt(5), rs.getInt(6)));
            }
            con.commit();
        } catch (SQLException e) {
            System.out.println("Die Module konnten nicht geladen werden");
            e.printStackTrace();
            rollback();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignore) {
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ignore) {
            }
        }

        return alleModule;
    }

    @Override
    public Collection<Praktikumsteilnahme> getAllPraktikumsteilnahme(Modul modul, String semester) {
        Collection<Praktikumsteilnahme> praktika = new ArrayList<>();
        if(modul != null && !semester.isEmpty()) {
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM APP.PRAKTIKUMSTEILNAHME PR, APP.STUDENT S " +
                        "WHERE PR.MATRIKEL = S.MATRIKEL AND PR.MKUERZEL = ? AND PR.SEMESTER = ?");
                ps.setString(1, modul.getKuerzel());
                ps.setString(2, semester);
                rs = ps.executeQuery();

                while (rs.next()) {

                    praktika.add(new ePraktikumsteilnahme(
                            new eStudent(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)),
                            modul, semester, rs.getBoolean(6)));
                }
                con.commit();
            } catch (SQLException e) {
                System.err.println("Die Praktikumsteilnahme-Liste konnte nicht geladen werden.");
                e.printStackTrace();
                rollback();
            } finally {
                try{
                    if( rs != null ) {
                        rs.close();
                    }
                } catch( SQLException ignore ) {}
                try{
                    if( stmt!= null ) {
                        stmt.close();
                    }
                } catch( SQLException ignore ) {}

            }
        }
        return praktika;
    }

    @Override
    public JPanel getChart(int i, Object o, Object o1) throws ApplicationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws ApplicationException {
        try {
            con.close();
        } catch (SQLException e) {
            System.err.println("Die DB-Verbindung konnte nicht geschlossen werden.");
            e.printStackTrace();
        }
    }

}
