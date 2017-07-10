/**
 * Created by Valsorya94 on 10.07.2017.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;
import java.util.*;
import java.io.*;

public class GameOfLife extends JFrame {

    final String NAME_OF_GAME = "Conway's Game of Life";
    final String SAVE_FILE_EXT = ".life";
    final int LIFE_SIZE = 50;
    final int POINT_RADIUS = 10;
    final int FIELD_SIZE = LIFE_SIZE * POINT_RADIUS + 7;
    final int BTN_PANEL_HEIGHT = 58 + 4;
    final int START_LOCATION = 200;
    boolean[][] lifeGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    boolean[][] nextGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    boolean[][] tmp;
    int countGeneration = 0;
    int showDelay = 500;
    int showDelayStep = 50;
    volatile boolean goNextGeneration = false; // fixed the problem in 64-bit JVM added volatile
    boolean useColors = false;
    boolean showGrid = false;
    Random random = new Random();
    Dimension btnDimension = new Dimension(30, 26);
    JFrame frame;
    Canvas canvasPanel;

    // icons for buttons
    final ImageIcon icoFill = new ImageIcon(GameOfLife.class.getResource("img/btnFill.png"));
    final ImageIcon icoNew = new ImageIcon(GameOfLife.class.getResource("img/btnNew.png"));
    final ImageIcon icoOpen = new ImageIcon(GameOfLife.class.getResource("img/btnOpen.png"));
    final ImageIcon icoSave = new ImageIcon(GameOfLife.class.getResource("img/btnSave.png"));
    final ImageIcon icoStep = new ImageIcon(GameOfLife.class.getResource("img/btnStep.png"));
    final ImageIcon icoGo = new ImageIcon(GameOfLife.class.getResource("img/btnGo.png"));
    final ImageIcon icoStop = new ImageIcon(GameOfLife.class.getResource("img/btnStop.png"));
    final ImageIcon icoFaster = new ImageIcon(GameOfLife.class.getResource("img/btnFaster.png"));
    final ImageIcon icoSlower = new ImageIcon(GameOfLife.class.getResource("img/btnSlower.png"));
    final ImageIcon icoColor = new ImageIcon(GameOfLife.class.getResource("img/btnColor.png"));
    final ImageIcon icoNoColor = new ImageIcon(GameOfLife.class.getResource("img/btnNoColor.png"));
    final ImageIcon icoGrid = new ImageIcon(GameOfLife.class.getResource("img/btnGrid.png"));

    public static void main(String[] args) {
        new GameOfLife().go();
    }

    void go() {
        frame = new JFrame(NAME_OF_GAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FIELD_SIZE, FIELD_SIZE + BTN_PANEL_HEIGHT);
        frame.setLocation(START_LOCATION, START_LOCATION);
        frame.setResizable(false);

        // randomly fill cells
        JButton fillButton = new JButton();
        fillButton.setIcon(icoFill);
        fillButton.setPreferredSize(btnDimension);
        fillButton.setToolTipText("Fill randomly");
        fillButton.addActionListener(new FillButtonListener());

        // clear fields
        JButton newButton = new JButton();
        newButton.setIcon(icoNew);
        newButton.setPreferredSize(btnDimension);
        newButton.setToolTipText("Clear field");
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int x = 0; x < LIFE_SIZE; x++) {
                    Arrays.fill(lifeGeneration[x], false);
                }
                canvasPanel.repaint();
            }
        });

        // open saved file
        JButton openButton = new JButton();
        openButton.setIcon(icoOpen);
        openButton.setPreferredSize(btnDimension);
        openButton.setToolTipText("Open saved file");
        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser open = new JFileChooser(".");
                open.setFileSelectionMode(JFileChooser.FILES_ONLY);
                open.addChoosableFileFilter(new FileFilter() {
                    public String getDescription() {
                        return "Saved GameOfLife files (*" + SAVE_FILE_EXT + ")";
                    }

                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        } else {
                            return f.getName().toLowerCase().endsWith(SAVE_FILE_EXT);
                        }
                    }
                });
                open.setAcceptAllFileFilterUsed(true);
                int result = open.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        FileInputStream fileIn = new FileInputStream(new File(open.getSelectedFile().getAbsolutePath()));
                        ObjectInputStream is = new ObjectInputStream(fileIn);
                        lifeGeneration = (boolean[][]) is.readObject();
                        canvasPanel.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Incorrect file format.", "Error openning file", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // save field as file
        JButton saveButton = new JButton();
        saveButton.setIcon(icoSave);
        saveButton.setPreferredSize(btnDimension);
        saveButton.setToolTipText("Save field as file");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser save = new JFileChooser(".");
                save.setFileSelectionMode(JFileChooser.FILES_ONLY);
                //save.setFileFilter(new FileNameExtensionFilter("Game Of Life files (*." + SAVE_FILE_EXT + ")", SAVE_FILE_EXT)); // for compatibility with Java 1.5
                int result = save.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        FileOutputStream fileStream = new FileOutputStream(new File(save.getSelectedFile().getAbsolutePath() + (save.getSelectedFile().getAbsolutePath().endsWith(SAVE_FILE_EXT) ? "" : SAVE_FILE_EXT)));
                        ObjectOutputStream os = new ObjectOutputStream(fileStream);
                        os.writeObject(lifeGeneration);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
