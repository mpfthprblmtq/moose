package moose;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;

public class TableIcon extends JPanel {

    public TableIcon() {
        ImageIcon aboutIcon = new ImageIcon("dell.png");
        Icon addIcon = new ImageIcon("happco.png");
        Icon copyIcon = new ImageIcon("Hi-Parser.png");
        
        Image img = aboutIcon.getImage();
        Image new_img = img.getScaledInstance(75, 75, java.awt.Image.SCALE_SMOOTH);
        Icon newIcon = new ImageIcon(new_img);

        String[] columnNames = {"Picture", "Description"};
        Object[][] data
                = {
                    //{aboutIcon, "About"},
                    {newIcon, "About"},
                    {addIcon, "Add"},
                    {copyIcon, "Copy"},};

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            //  Returning the Class of each column will allow different
            //  renderers to be used based on Class
            public Class getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        //table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
        
        

    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Table Icon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new TableIcon());
        frame.setLocationByPlatform(true);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            createAndShowGUI();
        });
    }

}
