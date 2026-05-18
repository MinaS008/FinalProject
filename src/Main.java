import javax.swing.SwingUtilities;
import Utilities.*;
import UI.*;
import Persistence.*;
import Model.*;
import Controller.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.init();
            frame.show();
        });
    }
}