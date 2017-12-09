package ws1718.gdb.sis;

import edu.whs.gdb.ApplicationException;
import edu.whs.gdb.DataAccessObject;
import edu.whs.gdb.entity.Modul;
import edu.whs.gdb.entity.Praktikumsteilnahme;
import edu.whs.gdb.entity.Student;
import edu.whs.gdb.entity.Studienrichtung;
import ws1718.gdb.sis.entity.eModul;
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
    private Statement stmt;
    private ResultSet rs;

    implDAO() {
        try {
            String conURL = "jdbc:derby:D:/OneDrive/Studium/GDB/WS17-18/gdb-Aufgabe6-Bibliotheken/gdb-praktikum";
            con = DriverManager.getConnection(conURL);
            con.setAutoCommit(false);
            stmt = con.createStatement();
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
                System.out.println("Connection terminated.");
                con.close();

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
    public void addStudent(String string, String string1, String string2, String string3, String string4) throws ApplicationException {
        //Hier kommt das neue Feature hin.
    }

    @Override
    public Collection<Student> getAllStudent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean enroll(String string, String string1, String string2, String string3, String string4, Modul modul, String string5) throws ApplicationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setTestate(Collection<Praktikumsteilnahme> clctn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JPanel getChart(int i, Object o, Object o1) throws ApplicationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Studienrichtung> getAllStudienrichtung() {
        Collection<Studienrichtung> fach = new ArrayList<>();

        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM APP.STUDIENRICHTUNG");
            while (rs.next()) {
                fach.add(new eStudienrichtung(rs.getString(1), rs.getString(2)));
            }
            con.commit();
        } catch (SQLException ex) {
            System.err.println("Eine Studienrichtung konnte nicht geladen werden");
            ex.printStackTrace();
            rollback();
        }

        return fach;
    }

    @Override
    public Collection<Modul> getAllModul() {
        Collection<Modul> alleModule = new ArrayList<>();

        try {
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
        }

        return alleModule;
    }

    @Override
    public Collection<Praktikumsteilnahme> getAllPraktikumsteilnahme(Modul modul, String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws ApplicationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
