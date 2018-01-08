package ws1718.gdb.sis;

import edu.whs.dba.ApplicationException;
import edu.whs.dba.DataAccessObject;
import edu.whs.dba.entity.Modul;
import edu.whs.dba.entity.Praktikumsteilnahme;
import edu.whs.dba.entity.Student;
import edu.whs.dba.entity.Studienrichtung;
import ws1718.gdb.sis.entity.eModul;
import ws1718.gdb.sis.entity.ePraktikumsteilnahme;
import ws1718.gdb.sis.entity.eStudent;
import ws1718.gdb.sis.entity.eStudienrichtung;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.jdbc.JDBCPieDataset;

/**
 * @author hnzo
 */
class implDAO implements DataAccessObject {

    private Connection con = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    private final static String CONURL = "jdbc:derby:D:/OneDrive/Studium/GDB/WS17-18/gdb-Aufgabe6-Bibliotheken/gdb-praktikum";

    implDAO() {
        try {
            con = DriverManager.getConnection(CONURL);
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
            rs = stmt.executeQuery("SELECT DISTINCT K.LFDNR, K.NAME, K.KKUERZEL\n"
                    + "FROM APP.KATEGORIE K, APP.MODUL M, APP.STUDIENRICHTUNG S, APP.VERLAUFSPLAN V\n"
                    + "WHERE K.KKUERZEL = M.KKUERZEL AND M.MKUERZEL = V.MKUERZEL AND V.SKUERZEL = S.SKUERZEL "
                    + "AND S.SKUERZEL =" + "'" + s.getKuerzel() + "'");
            while (rs.next()) {
                kname.add(rs.getString(2));
                kkuerzel.add(rs.getString(3));
            }

            for (int i = 0; i < kname.size(); i++) {
                ArrayList<String> dritteZeile = new ArrayList<>();
                dritteZeile.add(kname.get(i));
                int stunden = 0;
                for (int b = 1; b <= 6; b++) {

                    rs = stmt.executeQuery("SELECT DISTINCT M.MKUERZEL, M.VL, M.UB, M.PR, M.CREDITS\n"
                            + "FROM APP.KATEGORIE K, APP.MODUL M, APP.STUDIENRICHTUNG S, APP.VERLAUFSPLAN V\n"
                            + "WHERE M.MKUERZEL = V.MKUERZEL AND V.SKUERZEL = S.SKUERZEL AND S.SKUERZEL =" + "'" + s.getKuerzel() + "'"
                            + "AND M.KKUERZEL =" + "'" + kkuerzel.get(i) + "'" + "AND V.SEM=" + b);

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
    public void addStudent(String matrikelNr, String name, String vorname, String adresse, Studienrichtung studienrichtung) throws ApplicationException {

        eStudent stud = new eStudent(matrikelNr, name, vorname, adresse, studienrichtung);
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO APP.STUDENT(MATRIKEL, NAME, VORNAME, ADRESSE, SKUERZEL)"
                    + " VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, stud.getMatrikel());
            ps.setString(2, stud.getName());
            ps.setString(3, stud.getVorname());
            ps.setString(4, stud.getAdresse());
            ps.setString(5, stud.getStudienrichtung().getKuerzel());
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
    }

    @Override
    public Collection<Student> getAllStudent() {
        Collection<Student> students = new ArrayList<>();

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM APP.STUDENT, APP.STUDIENRICHTUNG WHERE STUDENT.SKUERZEL = STUDIENRICHTUNG.SKUERZEL");

            while (rs.next()) {
                students.add(new eStudent(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), new eStudienrichtung(rs.getString(5), rs.getString(7))));
            }
            con.commit();
        } catch (SQLException e) {
            System.err.println("Die Studenten konnten nicht ausgegeben werden.");
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
        return students;
    }

    @Override
    public boolean enroll(String matrikelnr, String name, String vorname, String adresse, Studienrichtung skuerzel, Modul modul, String semester) throws ApplicationException {
        boolean erg = true;

        String modulTest = "SELECT MKUERZEL FROM APP.VERLAUFSPLAN WHERE SKUERZEL = ? AND MKUERZEL = ?";
        String prTest = "SELECT PR FROM APP.MODUL WHERE MKUERZEL = ? AND PR > 0 ";
        String anmeldungTest = "SELECT * FROM APP.PRAKTIKUMSTEILNAHME WHERE MATRIKEL = ? AND MKUERZEL = ? AND SEMESTER = ?";
        String richtungTest = "SELECT * FROM APP.STUDIENRICHTUNG  WHERE SKUERZEL = ?";
//        String matrikelTest = "SELECT * FROM STUDENT WHERE MATRIKEL = ?";
        String insert = "INSERT INTO APP.PRAKTIKUMSTEILNAHME(MATRIKEL, MKUERZEL, SEMESTER, TESTAT) VALUES (?,?,?,?)";

        try {
            try {
                addStudent(matrikelnr, name, vorname, adresse, skuerzel);
            } catch (ApplicationException e) {
                System.out.println("Student bereits vorhanden");
            }
            PreparedStatement ps = con.prepareStatement(modulTest);
            ps.setString(1, skuerzel.getKuerzel());
            ps.setString(2, modul.getKuerzel());
            rs = ps.executeQuery();
            if (!rs.next()) {
                erg = false;
                throw new ApplicationException("Das übergebene Modul ist nicht Bestandteil der Studienrichtung!");
            }

            ps = con.prepareStatement(prTest);
            ps.setString(1, modul.getKuerzel());
            rs = ps.executeQuery();
            if (!rs.next()) {
                erg = false;
                throw new ApplicationException("Das übergebene Modul sieht kein Praktikum vor.");
            }

            ps = con.prepareStatement(anmeldungTest);
            ps.setString(1, matrikelnr);
            ps.setString(2, modul.getKuerzel());
            ps.setString(3, semester);
            rs = ps.executeQuery();
            if (rs.next()) {
                erg = false;
                throw new ApplicationException("Der Teilnehmer ist schon in der Datenbank vorhanden!");
            }

            ps = con.prepareStatement(richtungTest);
            ps.setString(1, skuerzel.getKuerzel());
            rs = ps.executeQuery();
            if (!rs.next()) {
                erg = false;
                throw new ApplicationException("Es wurde keine oder eine nicht erfasste Studienrichtung angegeben!");
            }

            if (erg) {
                PreparedStatement psInsert = con.prepareStatement(insert);
                psInsert.setString(1, matrikelnr);
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

        return erg;
    }

    @Override
    public void updateBescheinigungen(Collection<Praktikumsteilnahme> clctn) {
        PreparedStatement ps = null;
        try {
            for (Praktikumsteilnahme pt : clctn) {
                ps = con.prepareStatement("UPDATE APP.PRAKTIKUMSTEILNAHME SET TESTAT = TRUE "
                        + "WHERE MATRIKEL = ? AND MKUERZEL = ? AND SEMESTER = ?");
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
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignore) {
            }
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
        if (modul != null && !semester.isEmpty()) {
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM APP.PRAKTIKUMSTEILNAHME PR, APP.STUDENT S, APP.STUDIENRICHTUNG SR "
                        + "WHERE PR.MATRIKEL = S.MATRIKEL AND S.SKUERZEL = SR.SKUERZEL AND PR.MKUERZEL = ? AND PR.SEMESTER = ?");
                ps.setString(1, modul.getKuerzel());
                ps.setString(2, semester);
                rs = ps.executeQuery();

                while (rs.next()) {

                    praktika.add(new ePraktikumsteilnahme(
                            new eStudent(rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8),
                                    new eStudienrichtung(rs.getString(10), rs.getString(11))),
                            modul, semester, rs.getBoolean(4)));
                }
                con.commit();
            } catch (SQLException e) {
                System.err.println("Die Praktikumsteilnahme-Liste konnte nicht geladen werden.");
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
        }
        return praktika;
    }

    /**
     *
     * Para1: Integer für ausgewähltes Diagramm Para2: Studienrichtung Object
     * Para3: String-Objekt für das Semesters
     */
    @Override
    public JPanel getChart(int i, Object o, Object o1) throws ApplicationException {
        JFreeChart jc = null;

        try {
            switch (i) {
                case VISUALISIERUNG_ANTEIL_BESCHEIGUNGEN: {
                    if (!(o instanceof String)) {
                        throw new ApplicationException("Es wurde kein String-Objekt übergeben.");
                    } else if (o1 != null) {
                        throw new ApplicationException("Das Objekt ist nicht 'null'");
                    }
                    String sem = (String) o;
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    for (Studienrichtung s : getAllStudienrichtung()) {
                        PreparedStatement ps = con.prepareStatement("" +
                                "SELECT DISTINCT m.mkuerzel " +
                                "FROM verlaufsplan v, modul m, praktikumsteilnahme p " +
                                "WHERE v.skuerzel = '?' " +
                                "AND p.SEMESTER = '?' " +
                                "AND m.PR > 0 " +
                                "AND v.mkuerzel = m.MKUERZEL " +
                                "AND m.MKUERZEL = p.MKUERZEL");

                        ps.setString(1, s.getKuerzel());
                        ps.setString(2, sem);

                        ResultSet rs = ps.executeQuery();

                        while (rs.next()) {
                            int anmeldungen = rs.getInt("anzahl");
                            String modul = rs.getString("MKUERZEL");
                            PreparedStatement ps2 = con.prepareStatement("SELECT m.mkuerzel, count(*) AS granted "
                                    + "FROM modul m, praktikumsteilnahme p, student s "
                                    + "WHERE p.mkuerzel = m.mkuerzel "
                                    + "AND p.matrikel = s.matrikel "
                                    + "AND testat = TRUE "
                                    + "AND p.mkuerzel = ? "
                                    + "AND p.semester = ? "
                                    + "AND s.skuerzel = ?"
                                    + " GROUP BY m.mkuerzel");
                            ps2.setString(1, modul);
                            ps2.setString(2, sem);
                            ps2.setString(3, s.getKuerzel());

                            ResultSet rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                int granted = rs2.getInt("granted");

                                double gesamt = (granted == 0) ? 0 : ((double) granted / (double) anmeldungen) * 100;
                                dataset.addValue(gesamt, modul, s.getKuerzel());
                            }
                        }
                        rs.close();


                    }
                        jc = ChartFactory.createBarChart(
                                "(" + sem + ")", // chart title
                                "Praktikumsmodule nach Studienrichtung", // domain axis label
                                "Teilnehmer mit Bescheinigungen in %", // range axis label
                                dataset, // data
                                PlotOrientation.VERTICAL, // orientation
                                true, // include legend
                                true, // tooltips?
                                false // URLs?
                        );

                    con.commit();
                }
                break;
                case VISUALISIERUNG_AUFTEILUNG_ANMELDUNGEN: {
                    if (!(o instanceof Studienrichtung)) {
                        throw new ApplicationException("Es wurde kein Studienrichtung-Onjekt übergeben.");
                    }
                    if (!(o1 instanceof String)) {
                        throw new ApplicationException("Es wurde kein String-Objekt übergeben.");
                    }

                    Studienrichtung sr = (Studienrichtung) o;
                    String sem = (String) o1;

                    String sql = "SELECT MKUERZEL, count(*) AS anzahl FROM app.STUDENT s,"
                            + "app.PRAKTIKUMSTEILNAHME p WHERE p.SEMESTER = '" + sem + "'"
                            + " AND s.SKUERZEL = '" + sr.getKuerzel() + "' AND p.MATRIKEL = s.MATRIKEL GROUP BY MKUERZEL";

                    JDBCPieDataset pieDS = new JDBCPieDataset(con, sql);
                    jc = ChartFactory.createPieChart("" + sr.toString() + " (" + sem + ")", pieDS, true, true, false);
                    ((PiePlot) jc.getPlot()).setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} Anmeldungen"));
                    pieDS.close();
                }
                break;
                case VISUALISIERUNG_ENTWICKLUNG_ANMELDUNGEN: {
                    if (!(o instanceof Modul)) {
                        throw new ApplicationException("Kein Objekt vom Typ Modul!");
                    } else if (o1 != null) {
                        throw new ApplicationException("Objekt muss null sein!");
                    }

                    Modul modul = (Modul) o;
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                    String sqlAnmeldungen = "select semester, count(*) as anmeldungen"
                            + " from praktikumsteilnahme p, student s"
                            + " where s.matrikel = p.matrikel"
                            + " and p.mkuerzel = ?"
                            + " group by semester";

                    PreparedStatement psAnmeldung = this.con.prepareStatement(sqlAnmeldungen);
                    psAnmeldung.setString(1, modul.getKuerzel());
                    ResultSet rsA = psAnmeldung.executeQuery();

                    while (rsA.next()) {
                        dataset.addValue(rsA.getInt("anmeldungen"),
                                "Anmeldungen", rsA.getString("semester"));

                        dataset.addValue(0, "Testatvergaben",
                                rsA.getString("semester"));
                    }
                    rsA.close();

                    PreparedStatement psTestate = this.con.prepareStatement("select semester, count(*) as bestanden"
                            + " from praktikumsteilnahme p, student s"
                            + " where s.matrikel = p.matrikel"
                            + " and p.mkuerzel = ?"
                            + " and p.testat = true"
                            + " group by semester");
                    psTestate.setString(1, modul.getKuerzel());
                    ResultSet rsT = psTestate.executeQuery();

                    while (rsT.next()) {
                        dataset.addValue(rsT.getInt("bestanden"), "Testatvergaben",
                                rsT.getString("semester"));
                    }
                    rsT.close();
                    jc = ChartFactory.createLineChart(modul.getName()
                                    + " (" + modul.getKuerzel() + ")",
                            "Semester", "Studierende", dataset,
                            PlotOrientation.VERTICAL,
                            true, true, false);
                    con.commit();


                }
                break;
                case VISUALISIERUNG_ANMELDUNGEN_BESCHEINIGUNGEN: {
                    if (!(o instanceof Studienrichtung)) {
                        throw new ApplicationException("Kein Objekt vom Typ Studienrichtung!");
                    } else if (!(o1 instanceof String)) {
                        throw new ApplicationException("Bitte ein String Objekt wählen!");
                    }

                    Studienrichtung stR = (Studienrichtung) o;
                    String sem = (String) o1;
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                    String teilnahme = "select p.MKUERZEL, count(*) as anmeldungen"
                            + " from praktikumsteilnahme p, modul m, student s"
                            + " where s.matrikel = p.matrikel"
                            + " and p.SEMESTER = ?"
                            + " and m.PR > 0"
                            + " and s.SKUERZEL = ?"
                            + " and m.MKUERZEL = p.MKUERZEL"
                            + " group by p.MKUERZEL";
                    PreparedStatement psTeilnahme = this.con.prepareStatement(teilnahme);
                    psTeilnahme.setString(1, sem);
                    psTeilnahme.setString(2, stR.getKuerzel());
                    ResultSet rsTeilnahme = psTeilnahme.executeQuery();

                    while (rsTeilnahme.next()) {

                        String modul = rsTeilnahme.getString("mkuerzel");
                        int anzahl = rsTeilnahme.getInt("anmeldungen");

                        dataset.addValue(anzahl, "Anmeldungen", modul);

                        PreparedStatement psTestate = this.con.prepareStatement("select p.MKUERZEL, count(*) as bestanden"
                                + " from praktikumsteilnahme p, student s"
                                + " where s.matrikel = p.matrikel"
                                + " and p.SEMESTER = ?"
                                + " and s.SKUERZEL = ?"
                                + " and p.MKUERZEL = ?"
                                + " and p.testat = true"
                                + " group by p.MKUERZEL");

                        psTestate.setString(1, sem);
                        psTestate.setString(2, stR.getKuerzel());
                        psTestate.setString(3, modul);
                        ResultSet rsTestate = psTestate.executeQuery();

                        while (rsTestate.next()) {
                            int bestanden = rsTestate.getInt("bestanden");
                            dataset.addValue(bestanden, "Testatvergaben", modul);
                        }
                        rsTestate.close();

                    }
                    rsTeilnahme.close();

                    jc = ChartFactory.createBarChart(stR.getName()
                                    + " " + stR.getKuerzel() + " - " + sem,
                            "Module",
                            "Studierende",
                            dataset,
                            PlotOrientation.VERTICAL,
                            true,
                            true,
                            false);
                    con.commit();
                }
                break;
                default: {
                    throw new ApplicationException("Chart Ansicht nicht vorhanden");
                }
            }
        } catch (SQLException e) {
            System.err.println("Es konnten keine statistischen Daten der DB entnommen werden.");
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



        return new ChartPanel(jc);
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
