package ws1718.gdb.sis;

import edu.whs.gdb.GUIFactory;
/**
 * Projektaufgabe in GDB: Studierenden Informationssystem (SIS).
 * Informationssystem zur Verwaltung von Stammdaten, Studienverlaufsplänen,
 * Praktikumsanmeldungen und co.
 * Visuelle Unterstützung durch Diagramme via JFreeChart Bibliothek.
 * 
 * @author hnzo
 */
class SIS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        implDAO dao = new implDAO();
        GUIFactory.createMainFrame("SIS v0.0", dao).setVisible(true);
        
    }
    
}
