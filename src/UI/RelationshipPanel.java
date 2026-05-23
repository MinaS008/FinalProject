package UI;
import Controller.WorldManager;
import Controller.NotificationManager;
import Helper.ThemeConstants;
import Model.CodexEntry;
import Model.RelationshipGraph;
import Model.RelationshipGraph.RelationshipLink;
import Model.World;
import Utilities.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

//Layout:
//North - header (back button, world title, add-link button)
//Center - GraphCanvas (interactive drawing surface)
//East - sidebar: selected-node details
//South - legend row

public class RelationshipPanel extends JPanel {
    //Constants
    private static final int nodeRadius = 28;
    private static final int sidebarWidth = 260;

    //Force-layout tuning
    private static final double repulsion = 12000.0;
    private static final double springLen = 160.0;
    private static final double springK = 0.04;
    private static final double damping = 0.85;
    private static final int simSteps = 60; //ticks per "settle" call

    //Fields
    private final MainFrame mainFrame;
    private final WorldManager worldManager;

    private World currentWorld;
    private GraphCanvas canvas;
    private JPanel sidebar;
    private JLabel worldTitleLabel;

    private JLabel sidebarNameLabel;
    private JLabel sidebarTypeLabel;
    private JPanel sidebarLinkList = new JPanel();

    public RelationshipPanel(MainFrame mainFrame, WorldManager worldManager) {
        this.mainFrame = mainFrame;
        this.worldManager = worldManager;

        setLayout(new BorderLayout());
        setBackground(ThemeConstants.colorBackground);
        setBorder(new EmptyBorder(
                ThemeConstants.padding * 2, ThemeConstants.padding * 2,
                ThemeConstants.padding * 2, ThemeConstants.padding * 2));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.EAST);
        add(buildLegend(), BorderLayout.SOUTH);

        canvas = new GraphCanvas();
        add(canvas, BorderLayout.CENTER);
    }

    public void loadWorld(World world) {
        this.currentWorld = world;
        worldTitleLabel.setText(world.getName() + " - Relationships");
        clearSidebar();
        canvas.rebuildNodes();
        canvas.settle();
        canvas.repaint();
    }

    public void refresh() {
        if (currentWorld == null) return;
        canvas.rebuildNodes();
        canvas.repaint();
    }

    //Header
    private JPanel buildHeader() {
        JButton backButton = buildStyledButton("← Back", ThemeConstants.colorSurface, ThemeConstants.colorSurfaceHover);
        backButton.setForeground(ThemeConstants.colorTextSecondary);
        backButton.addActionListener(e -> mainFrame.navigateToWorldView());

        worldTitleLabel = new JLabel("-");
        worldTitleLabel.setFont(ThemeConstants.fontTitle);
        worldTitleLabel.setForeground(ThemeConstants.colorAccent);

        JButton addLinkButton = buildStyledButton("+ Add Link", ThemeConstants.colorAccent, ThemeConstants.colorAccentDark);
        addLinkButton.setForeground(Color.WHITE);
        addLinkButton.addActionListener(e -> showAddLinkDialog());

        JPanel topRow = new JPanel(new BorderLayout(ThemeConstants.padding, 0));
        topRow.setOpaque(false);
        topRow.add(backButton, BorderLayout.WEST);
        topRow.add(worldTitleLabel, BorderLayout.CENTER);
        topRow.add(addLinkButton, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeConstants.colorBorder);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, ThemeConstants.padding, 0));
        wrapper.add(topRow, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ThemeConstants.colorSurface);
        sidebar.setBorder(new EmptyBorder(ThemeConstants.padding, ThemeConstants.padding, ThemeConstants.padding, ThemeConstants.padding));
        sidebar.setPreferredSize(new Dimension(sidebarWidth, 0));

        sidebarNameLabel = new JLabel("Select a node");
        sidebarNameLabel.setFont(ThemeConstants.fontBodyBold);
        sidebarNameLabel.setForeground(ThemeConstants.colorTextPrimary);
        sidebarNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebarTypeLabel = new JLabel(" ");
        sidebarTypeLabel.setFont(ThemeConstants.fontSmall);
        sidebarTypeLabel.setForeground(ThemeConstants.colorTextSecondary);
        sidebarTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(sidebarNameLabel);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(sidebarTypeLabel);
        sidebar.add(Box.createVerticalStrut(ThemeConstants.padding));
        sidebar.add(makeSidebarDivider());
        sidebar.add(Box.createVerticalStrut(ThemeConstants.padding));

        sidebarLinkList = new JPanel();
        sidebarLinkList.setLayout(new BoxLayout(sidebarLinkList, BoxLayout.Y_AXIS));
        sidebarLinkList.setOpaque(false);
        sidebarLinkList.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sidebarLinkList);
        sidebar.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(sidebar);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setPreferredSize(new Dimension(sidebarWidth, 0));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, ThemeConstants.padding, 0, 0));
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void clearSidebar() {
        sidebarNameLabel.setText("Select a node");
        sidebarTypeLabel.setText(" ");
        sidebarLinkList.removeAll();
        sidebarLinkList.revalidate();
        sidebarLinkList.repaint();
    }

    private void updateSidebar(Node node) {
        sidebarNameLabel.setText(node.entry.getName());
        sidebarTypeLabel.setText(node.entry.getType() + " . " + node.entry.getSummary());

        sidebarLinkList.removeAll();

        List<RelationshipLink> links = worldManager.getLinks(currentWorld.getID(), node.entry.getID());

        if (links.isEmpty()) {
            JLabel none = new JLabel("No connections yet.");
            none.setFont(ThemeConstants.fontSmall);
            none.setForeground(ThemeConstants.colorTextSecondary);
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebarLinkList.add(none);
        } else {
            JLabel heading = new JLabel("CONNECTIONS");
            heading.setFont(ThemeConstants.fontSmall);
            heading.setForeground(ThemeConstants.colorTextPlaceholder);
            heading.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebarLinkList.add(heading);
            sidebarLinkList.add(Box.createVerticalStrut(6));

            for (RelationshipLink link : links) {
                CodexEntry target = currentWorld.getEntryByID(link.getToID());
                if (target == null) continue;

                JPanel row = new JPanel(new BorderLayout(6, 0));
                row.setOpaque(false);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

                JLabel info = new JLabel("<html><b>" + link.getLabel() + "</b><br>"
                        + "<span style='color:#888'>" + target.getName() + "</span></html>");
                info.setFont(ThemeConstants.fontSmall);
                info.setForeground(ThemeConstants.colorTextPrimary);

                JButton del = new JButton("X");
                del.setFont(ThemeConstants.fontSmall);
                del.setForeground(ThemeConstants.colorDanger);
                del.setBackground(null);
                del.setBorder(null);
                del.setContentAreaFilled(false);
                del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                del.setToolTipText("Remove this link");
                del.addActionListener(e -> handleRemoveLink(node, link));

                row.add(info, BorderLayout.CENTER);
                row.add(del, BorderLayout.EAST);
                sidebarLinkList.add(row);
                sidebarLinkList.add(Box.createVerticalStrut(4));
            }
        }
        sidebarLinkList.revalidate();
        sidebarLinkList.repaint();
    }

    //Legend
    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        legend.setOpaque(false);
        legend.setBorder(new EmptyBorder(ThemeConstants.padding, 0, 0, 0));

        String[][] items = {
                {"Character", String.valueOf(new Color(80, 120, 180).getRGB())},
                {"Location", String.valueOf(new Color(70, 150, 100).getRGB())},
                {"Item", String.valueOf(new Color(160, 100, 50).getRGB())},
                {"Faction", String.valueOf(new Color(130, 70, 160).getRGB())},
                {"Lore", String.valueOf(new Color(160, 80, 80).getRGB())},
        };

        for (String[] item : items) {
            Color col = new Color(Integer.parseInt(item[1]));
            JLabel dot = new JLabel("●");
            dot.setForeground(col);
            dot.setFont(ThemeConstants.fontBody);
            JLabel name = new JLabel(item[0]);
            name.setFont(ThemeConstants.fontSmall);
            name.setForeground(ThemeConstants.colorTextSecondary);
            legend.add(dot);
            legend.add(name);
        }

        JLabel hint = new JLabel(" Drag nodes to rearrange  .  Click to select");
        hint.setFont(ThemeConstants.fontSmall);
        hint.setForeground(ThemeConstants.colorTextPlaceholder);
        legend.add(hint);

        return legend;
    }

    private void showAddLinkDialog() {
        List<CodexEntry> entries = currentWorld.getEntries();
        if (entries.size() < 2) {
            NotificationManager.showWarning(mainFrame, "You need at least 2 entries to create a link.");
            return;
        }

        String[] names = entries.stream().map(e -> e.getName() + " [" + e.getType() + "]").toArray(String[]::new);

        JComboBox<String> fromCombo = new JComboBox<>(names);
        JComboBox<String> toCombo = new JComboBox<>(names);
        toCombo.setSelectedIndex(1);

        JTextField labelField = buildDialogTextField("e.g. rivals, Allied with, Parent of...");

        styleCombo(fromCombo);
        styleCombo(toCombo);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeConstants.colorSurface);
        panel.setBorder(new EmptyBorder(ThemeConstants.padding, ThemeConstants.padding, ThemeConstants.padding, ThemeConstants.padding));

        panel.add(dialogLabel("From:"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(fromCombo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(dialogLabel("To:"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(toCombo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(dialogLabel("Relationship label *: "));
        panel.add(Box.createVerticalStrut(4));
        panel.add(labelField);

        int result = JOptionPane.showConfirmDialog(mainFrame,
                panel, "Add Relationship Link", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        int fromIdx = fromCombo.getSelectedIndex();
        int toIdx = toCombo.getSelectedIndex();

        if (fromIdx == toIdx) {
            NotificationManager.showWarning(mainFrame, "An entry cannot link to itself.");
            return;
        }

        String rawLabel = labelField.getText().trim();
        String placeholder = "e.g. Rivals, Allied with, Parent of...";
        String label = rawLabel.equals(placeholder) ? "" : rawLabel;

        Validator.ValidationResult vr = Validator.validateListItem(label, "Relationship label");
        if (!vr.isValid()) {
            NotificationManager.showWarning(mainFrame, vr.getMessage());
            return;
        }

        CodexEntry fromEntry = entries.get(fromIdx);
        CodexEntry toEntry = entries.get(toIdx);

        if (worldManager.getGraph(currentWorld.getID()).hasLink(fromEntry.getID(), toEntry.getID(), label)) {
            NotificationManager.showWarning(mainFrame, "That exact link already exists between these two entries");
            return;
        }

        worldManager.addLink(currentWorld.getID(), fromEntry.getID(), toEntry.getID(), label, fromEntry.getType());

        mainFrame.saveWorld(currentWorld);
        NotificationManager.showSuccess(mainFrame, "Link added: " + fromEntry.getName() + " ↔ " + toEntry.getName());

        canvas.rebuildNodes();
        canvas.repaint();
    }

    private void handleRemoveLink(Node node, RelationshipLink link) {
        CodexEntry target = currentWorld.getEntryByID(link.getToID());
        String targetName = target != null ? target.getName() : link.getToID();

        boolean confirmed = NotificationManager.showConfirm(mainFrame,
                "Remove \"" + link.getLabel() + "\" between "
                        + node.entry.getName() + " and " + targetName + "?",
                "Remove Link");

        if (!confirmed) return;

        worldManager.removeLink(currentWorld.getID(),
                node.entry.getID(), link.getToID(), link.getLabel());
        mainFrame.saveWorld(currentWorld);
        NotificationManager.showSuccess(mainFrame, "Linked removed.");

        updateSidebar(node);
        canvas.repaint();
    }

    //Inner class Node
    private static class Node {
        CodexEntry entry;
        double x, y;
        double vx, vy; //velocity for force layout

        Node(CodexEntry entry, double x, double y) {
            this.entry = entry;
            this.x = x;
            this.y = y;
        }
    }

    //Inner class GraphCanvas
    private class GraphCanvas extends JPanel {
        private final List<Node> nodes = new ArrayList<>();
        private Node selectedNode = null;
        private Node draggingNode = null;
        private Point dragOffset = new Point();

        GraphCanvas() {
            setBackground(ThemeConstants.colorBackground);
            setOpaque(true);

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    Node hit = nodeAt(e.getX(), e.getY());
                    if (hit != null) {
                        draggingNode = hit;
                        dragOffset.x = (int) (e.getX() - hit.x);
                        dragOffset.y = (int) (e.getY() - hit.y);
                        selectedNode = hit;
                        updateSidebar(hit);
                    } else {
                        selectedNode = null;
                        clearSidebar();
                    }
                    repaint();
                }

                public void mouseReleased(MouseEvent e) {
                    draggingNode = null;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (draggingNode != null) {
                        draggingNode.x = e.getX() - dragOffset.x;
                        draggingNode.y = e.getY() - dragOffset.y;
                        draggingNode.vx = 0;
                        draggingNode.vy = 0;
                        repaint();
                    }
                }
            });
        }

        //Rebuild node list from world entries, preserving positions of existing nodes
        void rebuildNodes() {
            //Save existing positions by entry ID
            Map<String, double[]> positions = new HashMap<>();
            for (Node n : nodes) {
                positions.put(n.entry.getID(), new double[]{n.x, n.y});
            }

            nodes.clear();
            List<CodexEntry> entries = currentWorld.getEntries();
            int total = entries.size();

            int w = Math.max(getWidth(), 600);
            int h = Math.max(getHeight(), 400);
            double cx = w / 2.0;
            double cy = h / 2.0;
            double radius = Math.min(w, h) * 0.35;

            for (int i = 0; i < total; i++) {
                CodexEntry e = entries.get(i);
                double[] pos = positions.get(e.getID());
                double x, y;
                if (pos != null) {
                    x = pos[0];
                    y = pos[1];
                } else {
                    //Place new nodes evenly around a circle
                    double angle = 2 * Math.PI * i / Math.max(total, 1);
                    x = cx + radius * Math.cos(angle);
                    y = cy + radius * Math.sin(angle);
                }
                nodes.add(new Node(e, x, y));
            }
        }

        //Run several ticks of force-directed layout to get a good initial spread
        void settle() {
            for (int i = 0; i < simSteps; i++) tick();
        }

        private void tick() {
            int w = Math.max(getWidth(), 600);
            int h = Math.max(getHeight(), 400);

            //Repulsion between every pair
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    Node a = nodes.get(i);
                    Node b = nodes.get(j);
                    double dx = b.x - a.x;
                    double dy = b.y - a.y;
                    double dist = Math.max(Math.sqrt(dx * dx + dy * dy), 1.0);
                    double force = repulsion / (dist * dist);
                    double fx = force * dx / dist;
                    double fy = force * dy / dist;
                    a.vx -= fx;
                    a.vy -= fy;
                    b.vx += fx;
                    b.vy += fy;
                }
            }

            //Spring attraction along edges
            RelationshipGraph graph = currentWorld.getRelationshipGraph();
            for (RelationshipLink link : graph.getAllLinks()) {
                Node a = nodeFor(link.getFromID());
                Node b = nodeFor(link.getToID());
                if (a == null || b == null) continue;
                double dx = b.x - a.x;
                double dy = b.y - a.y;
                double dist = Math.max(Math.sqrt(dx * dx + dy * dy), 1.9);
                double force = springK * (dist - springLen);
                double fx = force * dx / dist;
                double fy = force * dy / dist;
                a.vx += fx;
                a.vy += fy;
                b.vx -= fx;
                b.vy -= fy;
            }

            //Gravity toward center so nodes don't drift off-screen
            double gravK = 0.01;
            for (Node n : nodes) {
                n.vx += gravK * (w / 2.0 - n.x);
                n.vy += gravK * (h / 2.0 - n.y);
            }

            //Integrate + damp
            for (Node n : nodes) {
                if (n == draggingNode) continue;
                n.vx *= damping;
                n.vy *= damping;
                n.x += n.vx;
                n.y += n.vy;
                //Clamp to canvas bounds
                n.x = Math.max(nodeRadius, Math.min(w - nodeRadius, n.x));
                n.y = Math.max(nodeRadius, Math.min(h - nodeRadius, n.y));
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentWorld == null) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            //Draw empty-state hint
            if (nodes.isEmpty()) {
                g2.setFont(ThemeConstants.fontBody);
                g2.setColor(ThemeConstants.colorTextSecondary);
                String msg = "No entries in this world yet.";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
                g2.dispose();
                return;
            }

            RelationshipGraph graph = currentWorld.getRelationshipGraph();
            Map<String, Node> idMap = new HashMap<>();
            for (Node n : nodes) idMap.put(n.entry.getID(), n);

            //Draw edges
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (RelationshipLink link : graph.getAllLinks()) {
                Node a = idMap.get(link.getFromID());
                Node b = idMap.get(link.getToID());
                if (a == null || b == null) continue;

                g2.setColor(ThemeConstants.colorBorder);
                g2.draw(new Line2D.Double(a.x, a.y, b.x, b.y));

                // Draw label at midpoint
                double mx = (a.x + b.x) / 2;
                double my = (a.y + b.y) / 2;
                g2.setFont(ThemeConstants.fontSmall);
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(link.getLabel());
                int th = fm.getHeight();

                // Small pill background for readability
                g2.setColor(ThemeConstants.colorSurface);
                g2.fillRoundRect((int) (mx - tw / 2 - 4), (int) (my - th + 2), tw + 8, th + 2, 6, 6);

                g2.setColor(ThemeConstants.colorTextSecondary);
                g2.drawString(link.getLabel(), (int) (mx - tw / 2), (int) (my + 1));
            }

            //Draw nodes
            for (Node node : nodes) {
                boolean isSelected = node == selectedNode;
                Color fill = colorFor(node.entry.getType());
                Color border = isSelected ? ThemeConstants.colorAccent : fill.darker();

                //Selection glow
                if(isSelected){
                    g2.setColor(new Color(
                            ThemeConstants.colorAccent.getRed(),
                            ThemeConstants.colorAccent.getGreen(),
                            ThemeConstants.colorAccent.getBlue(), 60));
                    g2.fill(new Ellipse2D.Double(
                            node.x- nodeRadius - 6, node.y - nodeRadius - 6,
                            (nodeRadius + 6)*2, (nodeRadius+6)*2));
                }

                //Node circle fill
                g2.setColor(fill);
                g2.fill(new Ellipse2D.Double(
                        node.x - nodeRadius, node.y - nodeRadius,
                        nodeRadius * 2, nodeRadius * 2));

                // Node circle border
                g2.setColor(border);
                g2.setStroke(new BasicStroke(isSelected ? 2.5f : 1.5f));
                g2.draw(new Ellipse2D.Double(
                        node.x - nodeRadius, node.y - nodeRadius,
                        nodeRadius * 2, nodeRadius * 2));

                // Node label (name, possibly truncated)
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(Color.WHITE);
                String label = node.entry.getName();
                FontMetrics fm = g2.getFontMetrics();
                if (fm.stringWidth(label) > nodeRadius * 2 - 6) {
                    while (label.length() > 1
                            && fm.stringWidth(label + "…") > nodeRadius * 2 - 6) {
                        label = label.substring(0, label.length() - 1);
                    }
                    label += "…";
                }
                g2.drawString(label,
                        (int)(node.x - fm.stringWidth(label) / 2.0),
                        (int)(node.y + fm.getAscent() / 2.0 - 2));

                // Type label below node
                g2.setFont(ThemeConstants.fontSmall);
                g2.setColor(ThemeConstants.colorTextSecondary);
                String type = node.entry.getType();
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(type,
                        (int)(node.x - fm2.stringWidth(type) / 2.0),
                        (int)(node.y + nodeRadius + 14));
            }

            g2.dispose();
        }

        private Node nodeAt(int px, int py){
            for(Node n : nodes) {
                double dx = px - n.x;
                double dy = py - n.y;
                if(dx*dx + dy*dy <= (nodeRadius + 4)*(nodeRadius + 4)) return n;
            }
            return null;
        }

        private Node nodeFor(String id){
            for(Node n: nodes){
                if(n.entry.getID().equals(id)) return n;
            }
            return null;
        }

        private Color colorFor(String type){
            switch(type){
                case "Character": return new Color(80,  120, 180);
                case "Location": return new Color(70,  150, 100);
                case "Item": return new Color(160, 100,  50);
                case "Faction": return new Color(130,  70, 160);
                case "Lore": return new Color(160,  80,  80);
                default: return ThemeConstants.colorSurfaceHover;
            }
        }
    }

    private JSeparator makeSidebarDivider(){
        JSeparator sep = new JSeparator();
        sep.setForeground(ThemeConstants.colorBorder);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JLabel dialogLabel(String text){
        JLabel l = new JLabel(text);
        l.setFont(ThemeConstants.fontSmall);
        l.setForeground(ThemeConstants.colorTextPlaceholder);
        return l;
    }

    private JTextField buildDialogTextField(String placeholder){
        JTextField field = new JTextField(20);
        field.setFont(ThemeConstants.fontBody);
        field.setForeground(ThemeConstants.colorTextPlaceholder);
        field.setBackground(ThemeConstants.colorSurface);
        field.setCaretColor(ThemeConstants.colorTextPrimary);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeConstants.colorBorder),
                new EmptyBorder(6, 8, 6, 8)));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(ThemeConstants.colorTextPrimary);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(ThemeConstants.colorTextPlaceholder);
                }
            }
        });
        return field;
    }

    private void styleCombo(JComboBox<String> combo){
        combo.setFont(ThemeConstants.fontBody);
        combo.setBackground(ThemeConstants.colorSurface);
        combo.setForeground(ThemeConstants.colorTextPrimary);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    private JButton buildStyledButton(String text, Color bgColor, Color hover){
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(ThemeConstants.fontButton);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); btn.repaint(); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bgColor);    btn.repaint(); }
        });
        return btn;
    }
}
