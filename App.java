package com.smartinventory;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/** Entry point for the Smart Inventory Management System. */
public final class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.apply(Preferences.userNodeForPackage(App.class).getBoolean("dark", false));
            new LoginFrame().setVisible(true);
        });
    }
}

final class Theme {
    // Sapphire workspace: calm neutrals with vibrant, accessible action colors.
    static final Color BLUE = new Color(79, 70, 229), GREEN = new Color(16, 185, 129), AMBER = new Color(245, 158, 11), RED = new Color(239, 68, 68);
    static final Color INK = new Color(20, 28, 51), PAPER = new Color(245, 247, 255), PLUM = new Color(25, 31, 70);
    static boolean dark;
    static void apply(boolean useDark) {
        dark = useDark;

        if (useDark) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        Color canvas=useDark ? new Color(17, 21, 39) : PAPER;
        Color surface=useDark ? new Color(31, 37, 61) : Color.WHITE;
        Color text=useDark ? new Color(238, 242, 255) : INK;
        Color quiet=useDark ? new Color(174, 185, 216) : new Color(100, 116, 139);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14)); UIManager.put("Component.arc", 16); UIManager.put("Button.arc", 14);
        UIManager.put("TextComponent.arc", 12); UIManager.put("Table.rowHeight", 44); UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Panel.background", canvas); UIManager.put("Viewport.background", canvas); UIManager.put("Label.foreground", text);
        UIManager.put("Button.background", surface); UIManager.put("Button.foreground", text); UIManager.put("Button.borderColor", useDark ? new Color(65,75,111) : new Color(222, 226, 242));
        UIManager.put("Button.hoverBackground", useDark ? new Color(45, 53, 88) : new Color(239, 240, 255));
        UIManager.put("TextField.background", surface); UIManager.put("TextField.foreground", text); UIManager.put("TextField.borderColor", useDark ? new Color(76,88,129) : new Color(210, 216, 238));
        UIManager.put("Table.background", surface); UIManager.put("Table.foreground", text); UIManager.put("TableHeader.background", useDark ? new Color(39,47,76) : new Color(239, 241, 252));
        UIManager.put("TableHeader.foreground", text); UIManager.put("Table.selectionBackground", new Color(BLUE.getRed(), BLUE.getGreen(), BLUE.getBlue(), 65));
        UIManager.put("Table.selectionForeground", text); UIManager.put("Separator.foreground", useDark ? new Color(62,72,106) : new Color(222, 226, 242)); UIManager.put("Component.focusColor", BLUE);
    }
    static Color bg(){ return dark ? new Color(17,21,39) : PAPER; }
    static Color panel(){ return dark ? new Color(31,37,61) : Color.WHITE; }
    static Color muted(){ return dark ? new Color(174,185,216) : new Color(100,116,139); }
    static Color ink(){ return dark ? new Color(238,242,255) : INK; }
    static Color sidebarInk(){ return new Color(234, 238, 255); }
    static Border card(){ return new CompoundBorder(new LineBorder(dark ? new Color(71,83,126) : new Color(226, 230, 246)), new EmptyBorder(18,18,18,18)); }
    static JButton button(String text, boolean primary) {
        ModernButton b=new ModernButton(text,primary ? BLUE : panel(),primary ? Color.WHITE : ink());
        b.setFont(new Font("Segoe UI",Font.BOLD,13));
        b.setBorder(new EmptyBorder(9,16,9,16));
        return b;
    }
    static JLabel label(String text, int size, boolean bold){ JLabel l=new JLabel(text);l.setFont(new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,size));l.setForeground(ink());return l; }
}

/** Consistent, keyboard-accessible business button with clear hover and press feedback. */
final class ModernButton extends JButton {
    private final Color fill, foreground; private boolean hovered, pressed;
    ModernButton(String text, Color fill, Color foreground) {
        super(text); this.fill=fill; this.foreground=foreground;
        setForeground(foreground); setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
        setOpaque(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter(){ public void mouseEntered(MouseEvent e){hovered=true;repaint();}
            public void mouseExited(MouseEvent e){hovered=false;pressed=false;repaint();}
            public void mousePressed(MouseEvent e){pressed=true;repaint();}
            public void mouseReleased(MouseEvent e){pressed=false;repaint();} });
    }
    @Override protected void paintComponent(Graphics raw) {
        Graphics2D g=(Graphics2D)raw.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Color paint=fill;
        if(!isEnabled()) paint=new Color(fill.getRed(),fill.getGreen(),fill.getBlue(),90);
        else if(pressed) paint=fill.darker(); else if(hovered) paint=fill.equals(Theme.BLUE)?new Color(67,56,202):new Color(Math.max(0,fill.getRed()-8),Math.max(0,fill.getGreen()-8),Math.max(0,fill.getBlue()-8));
        g.setColor(paint); g.fillRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
        if(!fill.equals(Theme.BLUE)) { g.setColor(Theme.dark?new Color(79,91,130):new Color(218,224,238));g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14); }
        if(isFocusOwner()) { g.setColor(new Color(Theme.BLUE.getRed(),Theme.BLUE.getGreen(),Theme.BLUE.getBlue(),115));g.setStroke(new BasicStroke(2f));g.drawRoundRect(2,2,getWidth()-5,getHeight()-5,11,11); }
        g.dispose(); super.paintComponent(raw);
    }
}

/** Reusable JTable polish: hierarchy, spacing, zebra rows, and status awareness. */
final class TableStyleUtil {
    private TableStyleUtil() {}
    static void apply(JTable table) {
        table.setFillsViewportHeight(true); table.setRowHeight(46); table.setShowVerticalLines(false); table.setShowHorizontalLines(true);
        table.setGridColor(Theme.dark ? new Color(53,64,97) : new Color(234,237,245));
        table.setSelectionBackground(new Color(Theme.BLUE.getRed(),Theme.BLUE.getGreen(),Theme.BLUE.getBlue(),70));
        table.setSelectionForeground(Theme.ink()); table.setIntercellSpacing(new Dimension(0,1));
        table.getTableHeader().setReorderingAllowed(false); table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        table.getTableHeader().setPreferredSize(new Dimension(0,38));
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object value,boolean selected,boolean focused,int row,int col){
                JLabel cell=(JLabel)super.getTableCellRendererComponent(t,value,selected,focused,row,col);
                cell.setBorder(new EmptyBorder(0,12,0,12));
                if(!selected) cell.setBackground(row%2==0?Theme.panel():(Theme.dark?new Color(35,42,69):new Color(249,250,253)));
                String text=String.valueOf(value);
                if(text.contains("OUT OF STOCK")) cell.setForeground(Theme.RED);
                else if(text.contains("LOW STOCK")||text.contains("WARNING")) cell.setForeground(Theme.AMBER);
                else if(text.contains("AVAILABLE")||text.contains("SUCCESS")||text.contains("ACTIVE")) cell.setForeground(Theme.GREEN);
                else cell.setForeground(Theme.ink());
                return cell;
            }
        });
    }
}

/** Painted rather than image-based, so the graphic identity stays crisp at every window size. */
final class SapphireHero extends JPanel {
    SapphireHero(){ super(new GridBagLayout()); setOpaque(true); }
    @Override protected void paintComponent(Graphics g0){
        super.paintComponent(g0); Graphics2D g=(Graphics2D)g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(),h=getHeight();
        GradientPaint wash=new GradientPaint(0,0,new Color(40,45,107),w,h,new Color(98,66,177)); g.setPaint(wash); g.fillRect(0,0,w,h);
        g.setColor(new Color(129,140,248,75)); g.fillOval(w-250,-110,330,330);
        g.setColor(new Color(45,212,191,55)); g.fillOval(-130,h-180,330,330);
        g.setColor(new Color(255,255,255,32)); g.setStroke(new BasicStroke(2f));
        for(int x=-h;x<w;x+=44)g.drawLine(x,0,x+h,h);
        g.dispose();
    }
}

/** A subtle warehouse-inspired backdrop built in code, so the login screen stays self-contained. */
final class InventoryBackdrop extends JPanel {
    InventoryBackdrop(){ super(new GridBagLayout()); setOpaque(true); }
    @Override protected void paintComponent(Graphics g0){
        super.paintComponent(g0); Graphics2D g=(Graphics2D)g0.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int w=getWidth(),h=getHeight();
        g.setPaint(new GradientPaint(0,0,new Color(11,31,57),w,h,new Color(50,42,105)));g.fillRect(0,0,w,h);
        g.setColor(new Color(129,140,248,34));g.fillOval(w-280,-170,470,470);g.setColor(new Color(45,212,191,22));g.fillOval(-230,h-280,480,480);
        g.setColor(new Color(255,255,255,18));g.setStroke(new BasicStroke(3f));for(int y=100;y<h;y+=90){g.drawLine(0,y,w,y);for(int x=70;x<w;x+=150)g.drawLine(x,y-64,x,y+64);}g.dispose();
    }
}

final class LoginFrame extends JFrame {
    private final JTextField username=new JTextField(); private final JPasswordField password=new JPasswordField();
    LoginFrame(){
        super("Smart Inventory | Sign in"); setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(760,750); setMinimumSize(new Dimension(600,620)); setLocationRelativeTo(null);
        Color navy=new Color(17,48,85), bodyText=new Color(71,85,105), line=new Color(203,213,225); JPanel shell=new InventoryBackdrop();shell.setBorder(new EmptyBorder(42,32,42,32));
        JPanel form=new JPanel();form.setOpaque(true);form.setBackground(Color.WHITE);form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));form.setPreferredSize(new Dimension(456,548));form.setBorder(new CompoundBorder(new LineBorder(new Color(241,245,249)),new EmptyBorder(43,45,36,45)));
        JLabel brand=Theme.label("◈  SMART INVENTORY",13,true);brand.setForeground(navy);brand.setAlignmentX(Component.LEFT_ALIGNMENT);form.add(brand);form.add(Box.createVerticalStrut(10));
        JLabel title=new JLabel("Welcome back");title.setFont(new Font("Georgia",Font.BOLD,29));title.setForeground(new Color(15,23,42));title.setAlignmentX(Component.LEFT_ALIGNMENT);form.add(title);
        form.add(Box.createVerticalStrut(13)); JLabel sub=Theme.label("Sign in to manage suppliers, customers, purchases, sales\nand real-time inventory decisions.",14,false);sub.setForeground(bodyText);sub.setAlignmentX(Component.LEFT_ALIGNMENT);form.add(sub);form.add(Box.createVerticalStrut(27));
        JLabel userLabel=Theme.label("Username",13,true);userLabel.setForeground(new Color(30,41,59));userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);form.add(userLabel);form.add(Box.createVerticalStrut(6));username.putClientProperty("JTextField.placeholderText","Enter your username");username.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));username.setBorder(new CompoundBorder(new LineBorder(line),new EmptyBorder(0,14,0,14)));form.add(username);
        form.add(Box.createVerticalStrut(18));JLabel passwordLabel=Theme.label("Password",13,true);passwordLabel.setForeground(new Color(30,41,59));passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);form.add(passwordLabel);form.add(Box.createVerticalStrut(6));password.putClientProperty("JTextField.placeholderText","Enter your password");password.setBorder(null);JPanel passwordRow=new JPanel(new BorderLayout(4,0));passwordRow.setBackground(Color.WHITE);passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));passwordRow.setBorder(new CompoundBorder(new LineBorder(line),new EmptyBorder(0,14,0,8)));passwordRow.add(password,BorderLayout.CENTER);JToggleButton showPassword=new JToggleButton("Show");showPassword.setFocusPainted(false);showPassword.setBorderPainted(false);showPassword.setContentAreaFilled(false);showPassword.setForeground(navy);showPassword.setFont(new Font("Segoe UI",Font.BOLD,12));showPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));showPassword.addActionListener(e->{boolean visible=showPassword.isSelected();password.setEchoChar(visible?(char)0:'•');showPassword.setText(visible?"Hide":"Show");});passwordRow.add(showPassword,BorderLayout.EAST);form.add(passwordRow);
        form.add(Box.createVerticalStrut(13));JPanel options=new JPanel(new BorderLayout());options.setOpaque(false);JCheckBox remember=new JCheckBox("Remember this device");remember.setOpaque(false);remember.setForeground(bodyText);remember.setFocusPainted(false);options.add(remember,BorderLayout.WEST);JButton forgot=new JButton("Forgot Password?");forgot.setBorderPainted(false);forgot.setContentAreaFilled(false);forgot.setForeground(navy);forgot.setFont(new Font("Segoe UI",Font.BOLD,13));forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));forgot.addActionListener(e->Toast.show(this,"Contact an administrator to reset your password",Theme.AMBER));options.add(forgot,BorderLayout.EAST);form.add(options);
        form.add(Box.createVerticalStrut(25));JButton login=new JButton("Sign In to Inventory");login.setAlignmentX(Component.CENTER_ALIGNMENT);login.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));login.setBackground(navy);login.setForeground(Color.WHITE);login.setFont(new Font("Segoe UI",Font.BOLD,14));login.setFocusPainted(false);login.setBorder(new EmptyBorder(8,16,8,16));login.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));login.addActionListener(e->login());form.add(login);
        form.add(Box.createVerticalStrut(11));JPanel demo=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));demo.setOpaque(false);JLabel demoLabel=Theme.label("Try a demo:  ",12,false);demoLabel.setForeground(bodyText);demo.add(demoLabel);JButton adminDemo=demoButton("Admin", "admin", "admin123");JButton staffDemo=demoButton("Staff", "staff", "user123");demo.add(adminDemo);demo.add(Box.createHorizontalStrut(6));demo.add(staffDemo);form.add(demo);
        form.add(Box.createVerticalStrut(16));JPanel signup=new JPanel(new FlowLayout(FlowLayout.CENTER,4,0));signup.setOpaque(false);JLabel question=Theme.label("New to Smart Inventory?",13,false);question.setForeground(bodyText);signup.add(question);JButton link=new JButton("Create an account");link.setBorderPainted(false);link.setContentAreaFilled(false);link.setMargin(new Insets(0,0,0,0));link.setForeground(navy);link.setFont(new Font("Segoe UI",Font.BOLD,13));link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));link.addActionListener(e->Toast.show(this,"Ask an administrator to create your account",Theme.AMBER));signup.add(link);form.add(signup);
        form.add(Box.createVerticalStrut(18));JLabel help=new JLabel("<html><b style='font-size:15px'>ⓘ</b>&nbsp;&nbsp; Inventory flow: Supplier → Purchase → Stock In<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Customer → Sale → Stock Out → Dashboard & Reports</html>");help.setFont(new Font("Segoe UI",Font.PLAIN,12));help.setForeground(bodyText);help.setOpaque(true);help.setBackground(new Color(255,252,245));help.setBorder(new CompoundBorder(new LineBorder(new Color(253,230,138)),new EmptyBorder(12,13,12,13)));help.setAlignmentX(Component.LEFT_ALIGNMENT);help.setMaximumSize(new Dimension(Integer.MAX_VALUE,64));form.add(help);
        shell.add(form);setContentPane(shell);getRootPane().setDefaultButton(login);
    }
    private JButton demoButton(String label,String demoUser,String demoPassword){JButton b=new JButton(label);b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setFont(new Font("Segoe UI",Font.BOLD,11));b.setMargin(new Insets(3,9,3,9));b.addActionListener(e->{username.setText(demoUser);password.setText(demoPassword);password.requestFocusInWindow();});return b;}
    private void login(){
        if(username.getText().isBlank()||password.getPassword().length==0){Toast.show(this,"Enter your username and password",Theme.RED);return;}
        String loginName=username.getText().trim(), secret=new String(password.getPassword()); setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Session,Void>(){
            protected Session doInBackground(){return new AuthService().authenticate(loginName,secret);}
            protected void done(){setCursor(Cursor.getDefaultCursor());try{Session session=get();if(session==null){Toast.show(LoginFrame.this,"Incorrect username or password",Theme.RED);return;}new MainFrame(session.name,session.role).setVisible(true);dispose();}catch(Exception e){Toast.show(LoginFrame.this,"Unable to sign in. Please try again.",Theme.RED);}}
        }.execute();
    }
}

final class MainFrame extends JFrame {
    private final InventoryRepository repo=new InventoryRepository(); private final JPanel cards=new JPanel(new CardLayout()); private final JLabel pageTitle=Theme.label("Dashboard",18,true), crumb=Theme.label("Overview",12,false), dbStatus=Theme.label("● Connecting…",12,false);
    private final Map<String,JButton> nav=new LinkedHashMap<>(); private boolean collapsed=false; private JPanel sidebar; private String user,role;
    MainFrame(String user,String role){
        super("Smart Inventory Management System");this.user=user;this.role=role;setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(1400,860);setMinimumSize(new Dimension(1100,680));setLocationRelativeTo(null);
        new SwingWorker<Boolean,Void>(){ protected Boolean doInBackground(){return repo.connect();} protected void done(){try{dbStatus.setText(get()?"● Database Connected":"● Demo mode — Database Disconnected");dbStatus.setForeground(get()?Theme.GREEN:Theme.AMBER);refreshDashboard();}catch(Exception ignored){}}}.execute();
        setContentPane(build()); installShortcuts(); showPage("Dashboard");
    }
    private JComponent build(){ JPanel root=new JPanel(new BorderLayout());root.add(topBar(),BorderLayout.NORTH); sidebar=sidebar();root.add(sidebar,BorderLayout.WEST);root.add(cards,BorderLayout.CENTER);root.add(status(),BorderLayout.SOUTH);
        addPage("Dashboard",new DashboardPanel(this,repo)); addPage("Products",new ProductsPanel(this,repo)); addPage("Categories",new MasterPanel(this,"Categories","Organize your product catalogue",new String[]{"CATEGORY","DESCRIPTION","PRODUCTS","STATUS"}));
        addPage("Suppliers",new MasterPanel(this,"Suppliers","Manage purchase partners",new String[]{"SUPPLIER","CONTACT","PHONE","EMAIL","STATUS"}));addPage("Customers",new MasterPanel(this,"Customers","Manage sales customers",new String[]{"CUSTOMER","CONTACT","PHONE","EMAIL","STATUS"})); addPage("Purchases",new StockInPanel(this,repo));addPage("Sales",new StockOutPanel(this,repo));addPage("Adjustment",new AdjustmentPanel(this,repo));
        addPage("Transactions",new TransactionsPanel(this,repo));addPage("Reports",new ReportsPanel(this,repo));addPage("Low Stock",new ProductsPanel(this,repo,true));addPage("Expiry",new InsightPanel(this,repo,"Expiry Monitor","Track products approaching their expiry date",false));addPage("Profit",new InsightPanel(this,repo,"Profit Overview","See the margin held in your current inventory",true));addPage("Users",new MasterPanel(this,"Users","Control user access",new String[]{"NAME","USERNAME","ROLE","STATUS","LAST LOGIN"}));
        addPage("My Profile",profile()); addPage("Change Password",new ChangePasswordPanel(this,user)); addPage("Settings",settings()); addPage("About",about()); return root; }
    private JComponent topBar(){ JPanel bar=new JPanel(new BorderLayout(14,0));bar.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,darkBorder()),new EmptyBorder(11,18,11,18))); JButton toggle=Theme.button("☰",false);toggle.setToolTipText("Collapse sidebar");toggle.addActionListener(e->toggleSidebar()); JPanel left=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));left.add(toggle);left.add(pageTitle);left.add(new JSeparator(SwingConstants.VERTICAL));crumb.setForeground(Theme.muted());left.add(crumb);bar.add(left,BorderLayout.WEST);
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0)); JTextField search=new JTextField();search.putClientProperty("JTextField.placeholderText","Search products, suppliers…");search.setPreferredSize(new Dimension(230,34));search.addActionListener(e->{showPage("Products");});right.add(search); JButton bell=Theme.button("♟",false);bell.setText("🔔");bell.setToolTipText("Notifications");bell.addActionListener(e->notifications());right.add(bell);
        JButton profile=Theme.button("●  "+user+"  ▾",false);profile.setToolTipText(role);profile.addActionListener(e->profileMenu(profile));right.add(profile);bar.add(right,BorderLayout.EAST);return bar; }
    private Color darkBorder(){return Theme.dark?new Color(76,84,80):new Color(221,214,201);}
    private JPanel sidebar(){ JPanel side=new JPanel(new BorderLayout());side.setBackground(Theme.PLUM);side.setPreferredSize(new Dimension(235,0));side.setBorder(new MatteBorder(0,0,0,1,new Color(255,250,235,38))); JPanel head=new JPanel(new FlowLayout(FlowLayout.LEFT,14,16));head.setBackground(Theme.PLUM);JLabel logo=Theme.label("◈  SMART INVENTORY",15,true);logo.setForeground(Theme.sidebarInk());head.add(logo);side.add(head,BorderLayout.NORTH); JPanel list=new JPanel();list.setBackground(Theme.PLUM);list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));String[] pages={"Dashboard","Products","Categories","Suppliers","Customers","Purchases","Sales","Adjustment","Transactions","Low Stock","Expiry","Profit","Reports","Users","Change Password","Settings"};String[] icons={"⌂","▣","▤","▱","♙","↓","↑","↕","☷","!","◷","₹","◫","♙","⌁","⚙"};for(int i=0;i<pages.length;i++){if(!role.equals("ADMIN")&&(pages[i].equals("Categories")||pages[i].equals("Purchases")||pages[i].equals("Adjustment")||pages[i].equals("Reports")||pages[i].equals("Profit")||pages[i].equals("Users")||pages[i].equals("Settings")))continue; JButton b=navButton(icons[i],pages[i]);list.add(b);}
        side.add(new JScrollPane(list){ {setBorder(null);getViewport().setBackground(Theme.PLUM);getVerticalScrollBar().setUnitIncrement(16);} },BorderLayout.CENTER);JPanel foot=new JPanel();foot.setBackground(Theme.PLUM);foot.setLayout(new BoxLayout(foot,BoxLayout.Y_AXIS));JButton about=navButton("i","About");JButton logout=navButton("⇥","Logout");logout.addActionListener(e->logout());foot.add(about);foot.add(logout);side.add(foot,BorderLayout.SOUTH);return side; }
    private JButton navButton(String icon,String page){ JButton b=new JButton(icon+"   "+page);b.setForeground(Theme.sidebarInk());b.setHorizontalAlignment(SwingConstants.LEFT);b.setMaximumSize(new Dimension(Integer.MAX_VALUE,43));b.setBorder(new EmptyBorder(8,18,8,12));b.setFocusPainted(false);b.setContentAreaFilled(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setToolTipText(page);b.addActionListener(e->showPage(page));nav.put(page,b);return b; }
    private JComponent status(){ JPanel p=new JPanel(new BorderLayout());p.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,darkBorder()),new EmptyBorder(7,16,7,16)));JLabel info=Theme.label("Smart Inventory Management System     User: "+user+"     Role: "+role,12,false);info.setForeground(Theme.muted());p.add(info,BorderLayout.WEST);p.add(dbStatus,BorderLayout.EAST);return p; }
    void addPage(String name,JComponent p){cards.add(p,name);} void showPage(String page){ if(!nav.containsKey(page)&&!page.equals("About")&&!page.equals("My Profile"))return;((CardLayout)cards.getLayout()).show(cards,page);pageTitle.setText(page);crumb.setText(page.equals("Dashboard")?"Overview":"Inventory / "+page);nav.values().forEach(b->{b.setBackground(null);b.setOpaque(false);b.setForeground(Theme.sidebarInk());});JButton active=nav.get(page);if(active!=null){active.setOpaque(true);active.setBackground(Theme.BLUE);active.setForeground(Color.WHITE);} }
    void refreshDashboard(){ for(Component c:cards.getComponents())if(c instanceof Refreshable r)r.refresh();}
    void toggleSidebar(){collapsed=!collapsed;sidebar.setPreferredSize(new Dimension(collapsed?64:235,0));for(Map.Entry<String,JButton>e:nav.entrySet()){String page=e.getKey();JButton b=e.getValue();String icon=b.getText().substring(0,1);b.setText(collapsed?icon:icon+"   "+page);}sidebar.revalidate();}
    void openAddProduct(){ JDialog d=new ProductDialog(this,repo,null);d.setVisible(true); }
    void notifications(){
        JDialog dialog=new JDialog(this,"Alerts Center",false); dialog.setSize(470,420); dialog.setLocationRelativeTo(this);
        JPanel root=new JPanel(new BorderLayout(0,12));root.setBorder(new EmptyBorder(22,24,22,24));
        JPanel heading=new JPanel(new BorderLayout()); heading.add(Theme.label("Alerts Center",21,true),BorderLayout.WEST);
        JLabel count=Theme.label(repo.lowProducts().size()+" action item(s)",12,true);count.setForeground(repo.lowProducts().isEmpty()?Theme.GREEN:Theme.AMBER);heading.add(count,BorderLayout.EAST);root.add(heading,BorderLayout.NORTH);
        JPanel rows=new JPanel();rows.setLayout(new BoxLayout(rows,BoxLayout.Y_AXIS));
        for(Product product:repo.lowProducts()){
            JPanel item=new JPanel(new BorderLayout(12,0));item.setBorder(new CompoundBorder(new LineBorder(product.stock==0?new Color(Theme.RED.getRed(),Theme.RED.getGreen(),Theme.RED.getBlue(),100):new Color(Theme.AMBER.getRed(),Theme.AMBER.getGreen(),Theme.AMBER.getBlue(),100)),new EmptyBorder(12,13,12,13)));
            JLabel icon=Theme.label(product.stock==0?"!":"⚠",20,true);icon.setForeground(product.stock==0?Theme.RED:Theme.AMBER);item.add(icon,BorderLayout.WEST);
            JPanel copy=new JPanel();copy.setLayout(new BoxLayout(copy,BoxLayout.Y_AXIS));copy.add(Theme.label(product.name,14,true));JLabel detail=Theme.label(product.stock==0?"Out of stock — reorder immediately":"Low stock: "+product.stock+" units / minimum "+product.minimum,12,false);detail.setForeground(Theme.muted());copy.add(detail);item.add(copy,BorderLayout.CENTER);
            JButton view=Theme.button("View",false);view.addActionListener(e->{dialog.dispose();showPage("Products");});item.add(view,BorderLayout.EAST);rows.add(item);rows.add(Box.createVerticalStrut(8));
        }
        if(repo.lowProducts().isEmpty()){JLabel empty=Theme.label("All clear — no low-stock alerts right now.",14,false);empty.setForeground(Theme.muted());empty.setBorder(new EmptyBorder(28,8,28,8));rows.add(empty);}
        JScrollPane scroll=new JScrollPane(rows);scroll.setBorder(null);root.add(scroll,BorderLayout.CENTER);JButton close=Theme.button("Close",false);close.addActionListener(e->dialog.dispose());JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));actions.add(close);root.add(actions,BorderLayout.SOUTH);dialog.setContentPane(root);dialog.setVisible(true);
    }
    void profileMenu(Component anchor){JPopupMenu m=new JPopupMenu();for(String s:new String[]{"My Profile","Change Password","Settings","Logout"}){JMenuItem i=new JMenuItem(s);i.addActionListener(e->{if(s.equals("Logout"))logout();else showPage(s);});m.add(i);}m.show(anchor,0,anchor.getHeight());}
    void logout(){if(JOptionPane.showConfirmDialog(this,"End the current session?","Logout",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){dispose();new LoginFrame().setVisible(true);}}
    void installShortcuts(){JComponent r=getRootPane();bind(r,"control N","new",this::openAddProduct);bind(r,"control R","refresh",this::refreshDashboard);bind(r,"control L","logout",this::logout);bind(r,"control F","find",()->showPage("Products"));}
    private void bind(JComponent c,String key,String id,Runnable run){c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key),id);c.getActionMap().put(id,new AbstractAction(){public void actionPerformed(ActionEvent e){run.run();}});}
    private JComponent simplePage(String title,String subtitle,String[] labels,String save){JPanel p=page(title,subtitle);JPanel form=new JPanel(new GridBagLayout());form.setBorder(Theme.card());GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(7,7,7,7);g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;for(int i=0;i<labels.length;i++){g.gridx=0;g.gridy=i;form.add(Theme.label(labels[i],13,true),g);g.gridx=1;JTextField f=labels[i].contains("Password")?new JPasswordField():new JTextField();form.add(f,g);}g.gridx=1;g.gridy=labels.length;JButton b=Theme.button(save,true);b.addActionListener(e->Toast.show(this,"Saved successfully",Theme.GREEN));form.add(b,g);p.add(form,BorderLayout.NORTH);return p;}
    private JComponent settings(){JPanel p=page("Settings","Personalize your workspace");JPanel card=new JPanel();card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));card.setBorder(Theme.card());card.add(Theme.label("Appearance",18,true));card.add(Box.createVerticalStrut(8));JCheckBox dark=new JCheckBox("Use dark theme");dark.setSelected(Theme.dark);dark.addActionListener(e->{Preferences.userNodeForPackage(App.class).putBoolean("dark",dark.isSelected());Theme.apply(dark.isSelected());SwingUtilities.updateComponentTreeUI(this);});card.add(dark);card.add(Box.createVerticalStrut(18));card.add(Theme.label("Application",18,true));card.add(Box.createVerticalStrut(8));card.add(Theme.label("Currency: Indian Rupee (₹)   •   Date format: dd MMM yyyy",14,false));p.add(card,BorderLayout.NORTH);return p;}
    private JComponent profile(){JPanel p=page("My Profile","Manage your account information");JPanel card=new JPanel(new GridBagLayout());card.setBorder(Theme.card());GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(8,8,8,8);g.fill=GridBagConstraints.HORIZONTAL;g.gridx=0;g.gridy=0;g.gridheight=3;JLabel avatar=Theme.label(user.isBlank()?"U":user.substring(0,1).toUpperCase(),32,true);avatar.setHorizontalAlignment(SwingConstants.CENTER);avatar.setOpaque(true);avatar.setBackground(Theme.BLUE);avatar.setForeground(Color.WHITE);avatar.setPreferredSize(new Dimension(72,72));avatar.setBorder(new EmptyBorder(8,8,8,8));card.add(avatar,g);g.gridheight=1;g.gridx=1;g.gridy=0;card.add(Theme.label(user,20,true),g);g.gridy=1;JLabel roleLabel=Theme.label(role+" account",13,false);roleLabel.setForeground(Theme.muted());card.add(roleLabel,g);g.gridy=2;JButton passwordButton=Theme.button("Change Password",false);passwordButton.addActionListener(e->showPage("Change Password"));card.add(passwordButton,g);g.gridx=0;g.gridy=3;g.gridwidth=2;g.insets=new Insets(24,8,8,8);card.add(new JSeparator(),g);g.gridwidth=1;g.insets=new Insets(8,8,8,8);String[] labels={"Display Name","Username","Role"};String[] values={user,user,role};for(int i=0;i<labels.length;i++){g.gridx=0;g.gridy=i+4;g.weightx=0;card.add(Theme.label(labels[i],13,true),g);g.gridx=1;g.weightx=1;JTextField field=new JTextField(values[i]);field.setEditable(i==0);if(i>0)field.setEnabled(false);card.add(field,g);}g.gridx=1;g.gridy=7;JButton save=Theme.button("Save Profile",true);save.addActionListener(e->Toast.show(this,"Profile updated successfully",Theme.GREEN));card.add(save,g);p.add(card,BorderLayout.NORTH);return p;}
    private JComponent about(){JPanel p=page("About","Smart Inventory Management System");JPanel c=new JPanel();c.setBorder(Theme.card());c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));c.add(Theme.label("Smart Inventory",26,true));c.add(Box.createVerticalStrut(8));c.add(Theme.label("Version 1.0.0 • Java 17 • Swing • MySQL",14,false));c.add(Box.createVerticalStrut(16));c.add(Theme.label("A modern, database-driven inventory workspace.",14,false));p.add(c,BorderLayout.NORTH);return p;}
    static JPanel page(String title,String subtitle){JPanel p=new JPanel(new BorderLayout(0,18));p.setBorder(new EmptyBorder(24,28,24,28));JPanel h=new JPanel();h.setLayout(new BoxLayout(h,BoxLayout.Y_AXIS));h.add(Theme.label(title,27,true));JLabel sub=Theme.label(subtitle,14,false);sub.setForeground(Theme.muted());h.add(Box.createVerticalStrut(4));h.add(sub);p.add(h,BorderLayout.NORTH);return p;}
}

interface Refreshable {void refresh();}

final class DashboardPanel extends JPanel implements Refreshable {
    private final MainFrame frame;private final InventoryRepository repo; private JPanel metrics,alerts;private JTable transactions;
    DashboardPanel(MainFrame f,InventoryRepository r){super(new BorderLayout(0,18));frame=f;repo=r;setBorder(new EmptyBorder(24,28,24,28));add(welcomeHero(),BorderLayout.NORTH);JPanel body=new JPanel();body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));metrics=new JPanel(new GridLayout(1,5,14,14));body.add(metrics);body.add(Box.createVerticalStrut(18));JPanel middle=new JPanel(new GridLayout(1,2,18,0));middle.add(chartCard("Stock Overview","Monthly stock movement",false));middle.add(chartCard("Category Distribution","Current inventory allocation",true));body.add(middle);body.add(Box.createVerticalStrut(18));alerts=new JPanel(new GridLayout(1,2,18,0));body.add(alerts);body.add(Box.createVerticalStrut(18));body.add(transactionsCard());JScrollPane scroll=new JScrollPane(body);scroll.setBorder(null);scroll.getVerticalScrollBar().setUnitIncrement(18);add(scroll,BorderLayout.CENTER);}
    private JComponent welcomeHero(){
        JPanel hero=new JPanel(new BorderLayout(18,0)){
            @Override protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setPaint(new GradientPaint(0,0,Theme.dark?new Color(34,42,83):new Color(238,241,255),getWidth(),getHeight(),Theme.dark?new Color(28,69,83):new Color(232,250,247)));g.fillRoundRect(0,0,getWidth(),getHeight(),18,18);g.setColor(new Color(Theme.BLUE.getRed(),Theme.BLUE.getGreen(),Theme.BLUE.getBlue(),26));g.fillOval(getWidth()-140,-70,210,210);g.dispose();super.paintComponent(raw);}
        };hero.setOpaque(false);hero.setBorder(new CompoundBorder(new LineBorder(Theme.dark?new Color(89,101,153):new Color(214,220,248)),new EmptyBorder(18,22,18,22)));
        JPanel copy=new JPanel();copy.setOpaque(false);copy.setLayout(new BoxLayout(copy,BoxLayout.Y_AXIS));copy.add(Theme.label("Dashboard",27,true));JLabel w=Theme.label("Your inventory at a glance  •  "+LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),14,false);w.setForeground(Theme.muted());copy.add(Box.createVerticalStrut(4));copy.add(w);hero.add(copy,BorderLayout.WEST);
        JPanel signal=new JPanel();signal.setOpaque(false);signal.setLayout(new BoxLayout(signal,BoxLayout.Y_AXIS));JLabel live=Theme.label("●  LIVE INVENTORY",12,true);live.setForeground(Theme.GREEN);live.setAlignmentX(Component.RIGHT_ALIGNMENT);signal.add(live);signal.add(Box.createVerticalStrut(5));JLabel detail=Theme.label("Keep stock moving, not waiting",13,false);detail.setForeground(Theme.muted());detail.setAlignmentX(Component.RIGHT_ALIGNMENT);signal.add(detail);hero.add(signal,BorderLayout.EAST);return hero;
    }
    public void refresh(){Metrics m=repo.metrics();metrics.removeAll();String[][] x={{"▣","TOTAL PRODUCTS",String.valueOf(m.products),"+8.2% this month"},{"▤","TOTAL STOCK",fmt(m.stock),"Units in warehouse"},{"!","LOW STOCK",String.valueOf(m.low),"Needs attention"},{"×","OUT OF STOCK",String.valueOf(m.out),"Critical"},{"₹","INVENTORY VALUE",money(m.value),"Current value"}};for(int i=0;i<x.length;i++){int n=i;metrics.add(stat(x[i][0],x[i][1],x[i][2],x[i][3],()->frame.showPage(n==2||n==3?"Low Stock":n==0?"Products":"Reports")));}alerts.removeAll();alerts.add(lowStock());alerts.add(quick());TableModel tm=repo.transactionModel();transactions.setModel(tm);styleTable(transactions);revalidate();repaint();}
    private JComponent stat(String icon,String title,String value,String foot,Runnable click){JPanel c=new JPanel();c.setBorder(Theme.card());c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));JLabel a=Theme.label(icon+"  "+title,12,true);a.setForeground(Theme.muted());c.add(a);c.add(Box.createVerticalStrut(17));c.add(Theme.label(value,27,true));c.add(Box.createVerticalGlue());JLabel f=Theme.label(foot,12,false);f.setForeground(Theme.GREEN);c.add(f);c.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){click.run();}});return c;}
    private JComponent chartCard(String title,String subtitle,boolean donut){JPanel p=new JPanel(new BorderLayout());p.setBorder(Theme.card());JPanel h=new JPanel();h.setLayout(new BoxLayout(h,BoxLayout.Y_AXIS));h.add(Theme.label(title,17,true));JLabel s=Theme.label(subtitle,12,false);s.setForeground(Theme.muted());h.add(s);p.add(h,BorderLayout.NORTH);p.add(new Chart(donut),BorderLayout.CENTER);return p;}
    private JComponent lowStock(){JPanel p=new JPanel(new BorderLayout(0,8));p.setBorder(Theme.card());JPanel h=new JPanel(new BorderLayout());h.add(Theme.label("⚠  LOW STOCK ALERTS",16,true));JButton all=Theme.button("View all →",false);all.addActionListener(e->frame.showPage("Low Stock"));h.add(all,BorderLayout.EAST);p.add(h,BorderLayout.NORTH);JPanel rows=new JPanel();rows.setLayout(new BoxLayout(rows,BoxLayout.Y_AXIS));for(Product x:repo.lowProducts()){JButton b=new JButton(x.name+"     "+x.category+"     "+x.stock+" / Minimum "+x.minimum+"     "+(x.stock==0?"OUT":"LOW"));b.setHorizontalAlignment(SwingConstants.LEFT);b.setBorderPainted(false);b.setContentAreaFilled(false);b.setForeground(x.stock==0?Theme.RED:Theme.AMBER);b.addActionListener(e->new ProductDialog(frame,repo,x).setVisible(true));rows.add(b);}p.add(rows,BorderLayout.CENTER);return p;}
    private JComponent quick(){JPanel p=new JPanel(new BorderLayout(0,8));p.setBorder(Theme.card());p.add(Theme.label("QUICK ACTIONS",16,true),BorderLayout.NORTH);JPanel b=new JPanel(new GridLayout(2,3,8,8));String[] names={"+ Add Product","New Sale","New Purchase","+ Add Supplier","Stock Adjustment","View Reports"};String[] targets={"Products","Sales","Purchases","Suppliers","Adjustment","Reports"};for(int i=0;i<names.length;i++){int n=i;JButton z=Theme.button(names[i],i==0);z.addActionListener(e->{if(n==0)frame.openAddProduct();else frame.showPage(targets[n]);});b.add(z);}p.add(b,BorderLayout.CENTER);return p;}
    private JComponent transactionsCard(){JPanel p=new JPanel(new BorderLayout(0,8));p.setBorder(Theme.card());p.add(Theme.label("RECENT TRANSACTIONS",16,true),BorderLayout.NORTH);transactions=new JTable();styleTable(transactions);JScrollPane s=new JScrollPane(transactions);s.setPreferredSize(new Dimension(0,185));p.add(s,BorderLayout.CENTER);return p;}
    static void styleTable(JTable t){TableStyleUtil.apply(t);}
    static String money(double a){return NumberFormat.getCurrencyInstance(new Locale("en","IN")).format(a).replace("₹","₹");}static String fmt(int x){return NumberFormat.getIntegerInstance(new Locale("en","IN")).format(x);}
}

final class Chart extends JComponent {private final boolean donut;Chart(boolean d){donut=d;setPreferredSize(new Dimension(330,180));setToolTipText(d?"Inventory category distribution":"Monthly stock movement");}protected void paintComponent(Graphics gg){super.paintComponent(gg);Graphics2D g=(Graphics2D)gg.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int w=getWidth(),h=getHeight();if(donut){int[] a={40,25,18,17};Color[] c={Theme.BLUE,Theme.GREEN,Theme.AMBER,new Color(129,94,189)};int start=0;for(int i=0;i<a.length;i++){g.setColor(c[i]);g.fillArc(w/2-65,h/2-62,130,130,start,a[i]*360/100);start+=a[i]*360/100;}g.setColor(Theme.panel());g.fillOval(w/2-35,h/2-32,70,70);g.setColor(Theme.ink());g.setFont(new Font("Segoe UI",Font.BOLD,14));g.drawString("Stock",w/2-19,h/2+5);}else{int[] v={52,90,72,121,105,143};g.setColor(new Color(Theme.BLUE.getRed(),Theme.BLUE.getGreen(),Theme.BLUE.getBlue(),45));g.fillRect(24,h-28,w-48,1);for(int i=0;i<v.length;i++){int bw=(w-80)/v.length;int x=38+i*(bw+7);g.setColor(Theme.BLUE);g.fillRoundRect(x,h-30-v[i],bw,v[i],8,8);g.setColor(Theme.muted());g.setFont(new Font("Segoe UI",Font.PLAIN,11));g.drawString("M"+(i+1),x,h-10);}}g.dispose();}}

final class ProductsPanel extends JPanel implements Refreshable {
    private final MainFrame frame;private final InventoryRepository repo;private final boolean lowOnly;private final JTable table=new JTable();private final JTextField search=new JTextField();private final JPanel holder=new JPanel(new BorderLayout());private List<Product> shown=List.of();
    ProductsPanel(MainFrame f,InventoryRepository r){this(f,r,false);}ProductsPanel(MainFrame f,InventoryRepository r,boolean low){super(new BorderLayout(0,16));frame=f;repo=r;lowOnly=low;setBorder(new EmptyBorder(24,28,24,28));String name=low?"Low Stock Products":"Products";JPanel header=new JPanel(new BorderLayout());JPanel h=new JPanel();h.setLayout(new BoxLayout(h,BoxLayout.Y_AXIS));h.add(Theme.label(name,27,true));JLabel s=Theme.label(low?"Items that need immediate inventory attention":"Manage your inventory products",14,false);s.setForeground(Theme.muted());h.add(s);header.add(h,BorderLayout.WEST);JButton add=Theme.button("+ Add Product",true);add.addActionListener(e->frame.openAddProduct());header.add(add,BorderLayout.EAST);add(header,BorderLayout.NORTH);add(toolbar(),BorderLayout.CENTER);holder.setBorder(Theme.card());add(holder,BorderLayout.SOUTH);table.setAutoCreateRowSorter(true);DashboardPanel.styleTable(table);table.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){if(e.getClickCount()==2)editSelected();}});}
    private JComponent toolbar(){JPanel all=new JPanel();all.setLayout(new BoxLayout(all,BoxLayout.Y_AXIS));JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));search.setPreferredSize(new Dimension(280,35));search.putClientProperty("JTextField.placeholderText","🔍 Search products...");search.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){refresh();}public void removeUpdate(DocumentEvent e){refresh();}public void changedUpdate(DocumentEvent e){refresh();}});p.add(search);JButton edit=Theme.button("Edit",false);edit.addActionListener(e->editSelected());JButton delete=Theme.button("Delete",false);delete.addActionListener(e->deleteSelected());JButton history=Theme.button("Stock History",false);history.addActionListener(e->history());JButton reload=Theme.button("↻ Refresh",false);reload.addActionListener(e->refresh());p.add(edit);p.add(delete);p.add(history);p.add(reload);all.add(p);all.add(Box.createVerticalStrut(14));return all;}
    public void refresh(){List<Product> list=repo.products();String q=search.getText().toLowerCase();if(lowOnly)list=list.stream().filter(p->p.stock<=p.minimum).toList();if(!q.isBlank())list=list.stream().filter(p->(p.name+p.sku+p.category+p.supplier).toLowerCase().contains(q)).toList();shown=list;if(list.isEmpty()){holder.removeAll();holder.add(empty(),BorderLayout.CENTER);}else{table.setModel(repo.productModel(list));holder.removeAll();holder.add(new JScrollPane(table),BorderLayout.CENTER);}holder.revalidate();holder.repaint();}
    private Product selected(){int r=table.getSelectedRow();return r<0?null:shown.get(table.convertRowIndexToModel(r));}
    private void editSelected(){Product p=selected();if(p==null){Toast.show(frame,"Select a product first",Theme.AMBER);return;}new ProductDialog(frame,repo,p).setVisible(true);frame.refreshDashboard();}
    private void deleteSelected(){Product p=selected();if(p==null){Toast.show(frame,"Select a product first",Theme.AMBER);return;}if(JOptionPane.showConfirmDialog(frame,"Delete \""+p.name+"\"? This action cannot be undone.","Delete Product",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION){repo.delete(p);Toast.show(frame,"Product deleted",Theme.RED);frame.refreshDashboard();}}
    private void history(){Product p=selected();if(p==null){Toast.show(frame,"Select a product first",Theme.AMBER);return;}JOptionPane.showMessageDialog(frame,"Stock history for "+p.name+"\n\nUse Transactions to view all recorded inventory changes.","Stock History",JOptionPane.INFORMATION_MESSAGE);}
    private JComponent empty(){JPanel e=new JPanel();e.setLayout(new BoxLayout(e,BoxLayout.Y_AXIS));JLabel i=Theme.label("▣",40,false);i.setAlignmentX(.5f);e.add(Box.createVerticalGlue());e.add(i);JLabel t=Theme.label("No Products Found",19,true);t.setAlignmentX(.5f);e.add(t);JLabel s=Theme.label("There are no products matching your current search or filters.",13,false);s.setForeground(Theme.muted());s.setAlignmentX(.5f);e.add(s);JButton a=Theme.button("Add Product",true);a.setAlignmentX(.5f);a.addActionListener(x->frame.openAddProduct());e.add(Box.createVerticalStrut(12));e.add(a);e.add(Box.createVerticalGlue());return e;}
}

final class ProductDialog extends JDialog {
    private final InventoryRepository repo;private final Product original;private final JTextField name=new JTextField(),sku=new JTextField(),brand=new JTextField(),supplier=new JTextField(),purchase=new JTextField(),selling=new JTextField(),opening=new JTextField(),minimum=new JTextField(),location=new JTextField();private final JComboBox<String> category=new JComboBox<>(new String[]{"Electronics","Accessories","Stationery","Furniture","Networking"});
    ProductDialog(MainFrame owner,InventoryRepository repo,Product p){super(owner,p==null?"Add New Product":"Product Details",true);this.repo=repo;original=p;setSize(760,650);setMinimumSize(new Dimension(620,560));setLocationRelativeTo(owner);JPanel root=new JPanel(new BorderLayout(0,15));root.setBorder(new EmptyBorder(22,26,22,26));JPanel h=new JPanel();h.setLayout(new BoxLayout(h,BoxLayout.Y_AXIS));h.add(Theme.label(p==null?"Add New Product":p.name,25,true));JLabel sub=Theme.label(p==null?"Create a new inventory item":"SKU: "+p.sku+" • Update product information",14,false);sub.setForeground(Theme.muted());h.add(sub);root.add(h,BorderLayout.NORTH);JPanel form=new JPanel(new GridBagLayout());form.setBorder(Theme.card());String[] labels={"Product Name *","SKU / Barcode *","Category *","Brand","Supplier","Purchase Price (₹)","Selling Price (₹)","Opening Quantity","Minimum Stock","Location"};JComponent[] fields={name,sku,category,brand,supplier,purchase,selling,opening,minimum,location};GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(6,8,6,8);g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;for(int i=0;i<labels.length;i++){g.gridx=(i%2)*2;g.gridy=i/2;g.weightx=0;form.add(Theme.label(labels[i],13,true),g);g.gridx=(i%2)*2+1;g.weightx=1;form.add(fields[i],g);}root.add(new JScrollPane(form){ {setBorder(null);} },BorderLayout.CENTER);JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));JButton cancel=Theme.button("Cancel",false);cancel.addActionListener(e->dispose());JButton save=Theme.button(p==null?"Save Product":"Save Changes",true);save.addActionListener(e->save());actions.add(cancel);actions.add(save);root.add(actions,BorderLayout.SOUTH);setContentPane(root);if(p!=null)fill(p);}
    private void fill(Product p){name.setText(p.name);sku.setText(p.sku);category.setSelectedItem(p.category);supplier.setText(p.supplier);purchase.setText(String.valueOf(p.purchase));selling.setText(String.valueOf(p.selling));opening.setText(String.valueOf(p.stock));minimum.setText(String.valueOf(p.minimum));location.setText(p.location);}
    private void save(){try{if(name.getText().isBlank()||sku.getText().isBlank())throw new IllegalArgumentException("Product name and SKU are required.");Product p=new Product(original==null?0:original.id,sku.getText(),name.getText(),(String)category.getSelectedItem(),supplier.getText().isBlank()?"Unassigned":supplier.getText(),Integer.parseInt(opening.getText()),Integer.parseInt(minimum.getText()),Double.parseDouble(purchase.getText()),Double.parseDouble(selling.getText()),location.getText());repo.save(p);Toast.show(this,"✓ Product saved successfully",Theme.GREEN);dispose();((MainFrame)getOwner()).refreshDashboard();}catch(NumberFormatException e){Toast.show(this,"Enter valid numeric inventory and price values",Theme.RED);}catch(IllegalArgumentException e){Toast.show(this,e.getMessage(),Theme.RED);}}
}

final class ChangePasswordPanel extends JPanel {
    private final MainFrame frame; private final String user; private final JPasswordField current=new JPasswordField(),next=new JPasswordField(),confirm=new JPasswordField();
    ChangePasswordPanel(MainFrame frame,String user){super(new BorderLayout(0,16));this.frame=frame;this.user=user;setBorder(new EmptyBorder(24,28,24,28));add(MainFrame.page("Change Password","Update your account credentials").getComponent(0),BorderLayout.NORTH);JPanel card=new JPanel(new GridBagLayout());card.setBorder(Theme.card());GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(8,8,8,8);g.fill=GridBagConstraints.HORIZONTAL;String[] labels={"Current Password *","New Password *","Confirm New Password *"};JPasswordField[] fields={current,next,confirm};for(int i=0;i<labels.length;i++){g.gridx=0;g.gridy=i;g.weightx=0;card.add(Theme.label(labels[i],13,true),g);g.gridx=1;g.weightx=1;card.add(fields[i],g);}JLabel help=Theme.label("Use at least 8 characters. Password changes take effect at the next login.",12,false);help.setForeground(Theme.muted());g.gridx=1;g.gridy=3;card.add(help,g);JButton save=Theme.button("Update Password",true);save.addActionListener(e->save());g.gridy=4;card.add(save,g);add(card,BorderLayout.CENTER);}
    private void save(){String old=new String(current.getPassword()), a=new String(next.getPassword()), b=new String(confirm.getPassword());if(a.length()<8){Toast.show(frame,"Use at least 8 characters for the new password",Theme.RED);return;}if(!a.equals(b)){Toast.show(frame,"The new passwords do not match",Theme.RED);return;}if(new AuthService().changePassword(user,old,a))Toast.show(frame,"✓ Password updated successfully",Theme.GREEN);else Toast.show(frame,"Your current password is incorrect",Theme.RED);}
}

final class EntityPanel extends JPanel {EntityPanel(MainFrame frame,String title,String subtitle,String[] cols){super(new BorderLayout(0,16));setBorder(new EmptyBorder(24,28,24,28));JPanel h=new JPanel(new BorderLayout());JPanel x=new JPanel();x.setLayout(new BoxLayout(x,BoxLayout.Y_AXIS));x.add(Theme.label(title,27,true));JLabel q=Theme.label(subtitle,14,false);q.setForeground(Theme.muted());x.add(q);h.add(x,BorderLayout.WEST);JButton add=Theme.button("+ Add "+title.substring(0,title.length()-1),true);add.addActionListener(e->Toast.show(frame,"New "+title.substring(0,title.length()-1)+" form is ready for data entry",Theme.GREEN));h.add(add,BorderLayout.EAST);add(h,BorderLayout.NORTH);JPanel card=new JPanel(new BorderLayout(0,12));card.setBorder(Theme.card());JTextField search=new JTextField();search.putClientProperty("JTextField.placeholderText","🔍 Search "+title.toLowerCase()+"...");card.add(search,BorderLayout.NORTH);DefaultTableModel m=new DefaultTableModel(cols,0);if(title.equals("Categories")){m.addRow(new Object[]{"Electronics","Devices and displays",12,"ACTIVE"});m.addRow(new Object[]{"Accessories","Computer peripherals",21,"ACTIVE"});}else if(title.equals("Users")){m.addRow(new Object[]{"Administrator","admin","ADMIN","ACTIVE","Today"});}else{m.addRow(new Object[]{"ABC Computers","Rahul Mehta","98765 43210","sales@abc.in","ACTIVE"});}JTable t=new JTable(m);DashboardPanel.styleTable(t);card.add(new JScrollPane(t),BorderLayout.CENTER);add(card,BorderLayout.CENTER);}}

/** Shared, persistent management panel for categories, suppliers and users. */
final class MasterPanel extends JPanel implements Refreshable {
    private final MainFrame frame; private final String type; private final MasterStore store; private final JTable table=new JTable(); private final JTextField search=new JTextField(); private List<MasterItem> visible=List.of();
    MasterPanel(MainFrame frame,String type,String subtitle,String[] columns){super(new BorderLayout(0,16));this.frame=frame;this.type=type;this.store=new MasterStore(type);setBorder(new EmptyBorder(24,28,24,28));
        JPanel header=new JPanel(new BorderLayout());JPanel copy=new JPanel();copy.setLayout(new BoxLayout(copy,BoxLayout.Y_AXIS));copy.add(Theme.label(type,27,true));JLabel sub=Theme.label(subtitle,14,false);sub.setForeground(Theme.muted());copy.add(sub);header.add(copy,BorderLayout.WEST);JButton add=Theme.button("+ Add "+singular(),true);add.addActionListener(e->edit(null));header.add(add,BorderLayout.EAST);add(header,BorderLayout.NORTH);
        JPanel card=new JPanel(new BorderLayout(0,12));card.setBorder(Theme.card());JPanel toolbar=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));search.setPreferredSize(new Dimension(290,35));search.putClientProperty("JTextField.placeholderText","🔍 Search "+type.toLowerCase()+"...");search.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){refresh();}public void removeUpdate(DocumentEvent e){refresh();}public void changedUpdate(DocumentEvent e){refresh();}});toolbar.add(search);JButton edit=Theme.button("Edit",false);edit.addActionListener(e->selectedEdit());JButton deactivate=Theme.button("Deactivate",false);deactivate.addActionListener(e->deactivate());JButton delete=Theme.button("Delete",false);delete.addActionListener(e->delete());JButton reload=Theme.button("↻ Refresh",false);reload.addActionListener(e->refresh());toolbar.add(edit);toolbar.add(deactivate);toolbar.add(delete);toolbar.add(reload);card.add(toolbar,BorderLayout.NORTH);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);DashboardPanel.styleTable(table);table.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){if(e.getClickCount()==2)selectedEdit();}});card.add(new JScrollPane(table),BorderLayout.CENTER);add(card,BorderLayout.CENTER);
    }
    private String singular(){return type.equals("Categories")?"Category":type.equals("Suppliers")?"Supplier":"User";}
    public void refresh(){String q=search.getText().trim().toLowerCase();visible=store.items().stream().filter(i->i.searchable().contains(q)).toList();String[] headers=type.equals("Categories")?new String[]{"CATEGORY","DESCRIPTION","PRODUCTS","STATUS"}:type.equals("Suppliers")?new String[]{"SUPPLIER","CONTACT","PHONE","EMAIL","STATUS"}:new String[]{"NAME","USERNAME","ROLE","STATUS","LAST LOGIN"};Object[][] rows=new Object[visible.size()][headers.length];for(int i=0;i<visible.size();i++){MasterItem item=visible.get(i);rows[i]=item.row(type);}table.setModel(new DefaultTableModel(rows,headers){public boolean isCellEditable(int r,int c){return false;}});DashboardPanel.styleTable(table);}
    private MasterItem selected(){int r=table.getSelectedRow();return r<0?null:visible.get(table.convertRowIndexToModel(r));}
    private void selectedEdit(){MasterItem item=selected();if(item==null){Toast.show(frame,"Select a "+singular().toLowerCase()+" first",Theme.AMBER);return;}edit(item);}
    private void edit(MasterItem item){MasterDialog d=new MasterDialog(frame,type,item);d.setVisible(true);if(d.saved!=null){store.save(d.saved);refresh();Toast.show(frame,"✓ "+singular()+" saved successfully",Theme.GREEN);}}
    private void deactivate(){MasterItem item=selected();if(item==null){Toast.show(frame,"Select a "+singular().toLowerCase()+" first",Theme.AMBER);return;}item.active=false;store.save(item);refresh();Toast.show(frame,singular()+" deactivated",Theme.AMBER);}
    private void delete(){MasterItem item=selected();if(item==null){Toast.show(frame,"Select a "+singular().toLowerCase()+" first",Theme.AMBER);return;}if(JOptionPane.showConfirmDialog(frame,"Delete "+item.primary()+"? This cannot be undone.","Delete "+singular(),JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION){store.delete(item.id);refresh();Toast.show(frame,singular()+" deleted",Theme.RED);}}
}

final class MasterDialog extends JDialog {
    MasterItem saved; private final String type; private final MasterItem original; private final JTextField a=new JTextField(),b=new JTextField(),c=new JTextField(),d=new JTextField(); private final JComboBox<String> role=new JComboBox<>(new String[]{"ADMIN","USER"}); private final JCheckBox active=new JCheckBox("Active",true);
    MasterDialog(MainFrame owner,String type,MasterItem item){super(owner,(item==null?"Add ":"Edit ")+type.substring(0,type.length()-1),true);this.type=type;this.original=item;setSize(550,440);setLocationRelativeTo(owner);JPanel root=new JPanel(new BorderLayout(0,14));root.setBorder(new EmptyBorder(22,26,22,26));root.add(Theme.label(item==null?"Add "+type.substring(0,type.length()-1):"Edit "+type.substring(0,type.length()-1),23,true),BorderLayout.NORTH);JPanel form=new JPanel(new GridBagLayout());form.setBorder(Theme.card());String[] labels=type.equals("Categories")?new String[]{"Category Name *","Description","Status"}:type.equals("Suppliers")?new String[]{"Supplier Name *","Contact Person","Phone","Email","Status"}:new String[]{"Full Name *","Username *","Role","Status"};JComponent[] fields=type.equals("Categories")?new JComponent[]{a,b,active}:type.equals("Suppliers")?new JComponent[]{a,b,c,d,active}:new JComponent[]{a,b,role,active};GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(7,8,7,8);g.fill=GridBagConstraints.HORIZONTAL;for(int i=0;i<labels.length;i++){g.gridx=0;g.gridy=i;g.weightx=0;form.add(Theme.label(labels[i],13,true),g);g.gridx=1;g.weightx=1;form.add(fields[i],g);}root.add(form,BorderLayout.CENTER);JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));JButton cancel=Theme.button("Cancel",false);cancel.addActionListener(e->dispose());JButton save=Theme.button("Save",true);save.addActionListener(e->save());actions.add(cancel);actions.add(save);root.add(actions,BorderLayout.SOUTH);setContentPane(root);if(item!=null)fill(item);getRootPane().setDefaultButton(save);}
    private void fill(MasterItem x){a.setText(x.a);b.setText(x.b);c.setText(x.c);d.setText(x.d);role.setSelectedItem(x.c);active.setSelected(x.active);}
    private void save(){if(a.getText().isBlank()){Toast.show(this,"The first field is required",Theme.RED);return;}saved=new MasterItem(original==null?0:original.id,a.getText().trim(),b.getText().trim(),type.equals("Users")?(String)role.getSelectedItem():c.getText().trim(),d.getText().trim(),active.isSelected());dispose();}
}

final class MasterItem {int id;String a,b,c,d;boolean active;MasterItem(int id,String a,String b,String c,String d,boolean active){this.id=id;this.a=a;this.b=b;this.c=c;this.d=d;this.active=active;}String primary(){return a;}String searchable(){return (a+" "+b+" "+c+" "+d).toLowerCase();}Object[] row(String type){String status=active?"● ACTIVE":"● INACTIVE";if(type.equals("Categories"))return new Object[]{a,b,"—",status};if(type.equals("Suppliers"))return new Object[]{a,b,c,d,status};return new Object[]{a,b,c,status,"—"};}}

/** Master-data storage uses the same configured MySQL database with an in-memory fallback. */
final class MasterStore {private final String type;private final List<MasterItem> local=new ArrayList<>();private Connection db;MasterStore(String type){this.type=type;seed();open();}private void seed(){if(type.equals("Categories")){local.add(new MasterItem(1,"Electronics","Devices and displays","","",true));local.add(new MasterItem(2,"Accessories","Computer peripherals","","",true));}else if(type.equals("Suppliers"))local.add(new MasterItem(1,"ABC Computers","Rahul Mehta","98765 43210","sales@abc.in",true));else local.add(new MasterItem(1,"Administrator","admin","ADMIN","",true));}private String table(){return type.equals("Categories")?"inventory_categories":type.equals("Suppliers")?"inventory_suppliers":"inventory_users";}private void open(){try{Properties p=new Properties();try(var in=getClass().getResourceAsStream("/application.properties")){p.load(in);}db=DriverManager.getConnection(p.getProperty("db.url"),p.getProperty("db.user"),p.getProperty("db.password"));try(Statement s=db.createStatement()){s.executeUpdate("CREATE TABLE IF NOT EXISTS "+table()+"(id INT AUTO_INCREMENT PRIMARY KEY, field_a VARCHAR(160), field_b VARCHAR(160), field_c VARCHAR(160), field_d VARCHAR(160), active BOOLEAN DEFAULT TRUE)");}load();}catch(Exception ignored){db=null;}}private void load(){try(Statement s=db.createStatement();ResultSet r=s.executeQuery("SELECT * FROM "+table()+" ORDER BY id")){if(!r.next()){for(MasterItem x:local)insert(x);return;}local.clear();do{local.add(new MasterItem(r.getInt("id"),r.getString("field_a"),r.getString("field_b"),r.getString("field_c"),r.getString("field_d"),r.getBoolean("active")));}while(r.next());}catch(SQLException ignored){}}List<MasterItem> items(){return local;}void save(MasterItem x){if(x.id==0){x.id=local.stream().mapToInt(i->i.id).max().orElse(0)+1;local.add(x);if(db!=null)insert(x);}else{for(int i=0;i<local.size();i++)if(local.get(i).id==x.id)local.set(i,x);if(db!=null)try(PreparedStatement s=db.prepareStatement("UPDATE "+table()+" SET field_a=?,field_b=?,field_c=?,field_d=?,active=? WHERE id=?")){bind(s,x);s.setInt(6,x.id);s.executeUpdate();}catch(SQLException ignored){}}}private void insert(MasterItem x){try(PreparedStatement s=db.prepareStatement("INSERT INTO "+table()+"(field_a,field_b,field_c,field_d,active) VALUES(?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){bind(s,x);s.executeUpdate();try(ResultSet k=s.getGeneratedKeys()){if(k.next())x.id=k.getInt(1);}}catch(SQLException ignored){}}private void bind(PreparedStatement s,MasterItem x)throws SQLException{s.setString(1,x.a);s.setString(2,x.b);s.setString(3,x.c);s.setString(4,x.d);s.setBoolean(5,x.active);}void delete(int id){local.removeIf(x->x.id==id);if(db!=null)try(PreparedStatement s=db.prepareStatement("DELETE FROM "+table()+" WHERE id=?")){s.setInt(1,id);s.executeUpdate();}catch(SQLException ignored){}}}

final class Session {final String name,role;Session(String name,String role){this.name=name;this.role=role;}}

/** Authentication is database-backed when MySQL is available, with safe demo credentials for offline presentations. */
final class AuthService {
    Session authenticate(String username,String password){
        try{Properties p=new Properties();try (var in = getClass().getResourceAsStream("/application.properties")) {
            p.load(in);
        }try(Connection c=DriverManager.getConnection(p.getProperty("db.url"),p.getProperty("db.user"),p.getProperty("db.password"))){initialise(c);try(PreparedStatement s=c.prepareStatement("SELECT display_name, role FROM inventory_accounts WHERE username=? AND password_hash=? AND active=TRUE")){s.setString(1,username);s.setString(2,hash(password));try(ResultSet r=s.executeQuery()){return r.next()?new Session(r.getString(1),r.getString(2)):null;}}}}
        catch(Exception ignored){if(username.equals("admin")&&password.equals("admin123"))return new Session("Administrator","ADMIN");if(username.equals("staff")&&password.equals("user123"))return new Session("Store User","USER");return null;}
    }
    boolean changePassword(String displayName,String oldPassword,String newPassword){
        try{Properties p=new Properties();try(var in=getClass().getResourceAsStream("/application.properties")){p.load(in);}try(Connection c=DriverManager.getConnection(p.getProperty("db.url"),p.getProperty("db.user"),p.getProperty("db.password"))){initialise(c);try(PreparedStatement u=c.prepareStatement("UPDATE inventory_accounts SET password_hash=? WHERE display_name=? AND password_hash=? AND active=TRUE")){u.setString(1,hash(newPassword));u.setString(2,displayName);u.setString(3,hash(oldPassword));return u.executeUpdate()==1;}}}
        catch(Exception ignored){return displayName.equals("Administrator")&&oldPassword.equals("admin123");}
    }
    private void initialise(Connection c)throws SQLException{try(Statement s=c.createStatement()){s.executeUpdate("CREATE TABLE IF NOT EXISTS inventory_accounts(id INT AUTO_INCREMENT PRIMARY KEY, display_name VARCHAR(120), username VARCHAR(80) UNIQUE, password_hash VARCHAR(128), role VARCHAR(20), active BOOLEAN DEFAULT TRUE)");}try(PreparedStatement q=c.prepareStatement("SELECT id FROM inventory_accounts WHERE username='admin'")){try(ResultSet r=q.executeQuery()){if(!r.next())try(PreparedStatement i=c.prepareStatement("INSERT INTO inventory_accounts(display_name,username,password_hash,role,active) VALUES(?,?,?,?,TRUE)")){i.setString(1,"Administrator");i.setString(2,"admin");i.setString(3,hash("admin123"));i.setString(4,"ADMIN");i.executeUpdate();}}}}
    private String hash(String value){try{byte[] bytes=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format("%02x",b));return out.toString();}catch(Exception e){throw new IllegalStateException(e);}}
}

final class StockInPanel extends JPanel {private final MainFrame frame;private final InventoryRepository repo;private final JTextField qty=new JTextField("1"),price=new JTextField("0");private final JLabel total=Theme.label("₹0",20,true);StockInPanel(MainFrame f,InventoryRepository r){super(new BorderLayout(0,16));frame=f;repo=r;setBorder(new EmptyBorder(24,28,24,28));add(header("New Purchase","Record incoming inventory"),BorderLayout.NORTH);JPanel card=new JPanel(new GridBagLayout());card.setBorder(Theme.card());GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(7,8,7,8);g.fill=GridBagConstraints.HORIZONTAL;String[] l={"Supplier","Invoice Number","Purchase Date","Product","Quantity","Unit Price (₹)"};JComponent[] fs={new JComboBox<>(new String[]{"ABC Computers","Tech Mart","Office Supplies"}),new JTextField(),new JTextField(LocalDate.now().toString()),products(),qty,price};for(int i=0;i<l.length;i++){g.gridx=0;g.gridy=i;g.weightx=0;card.add(Theme.label(l[i],13,true),g);g.gridx=1;g.weightx=1;card.add(fs[i],g);}g.gridx=1;g.gridy=6;card.add(total,g);JButton save=Theme.button("Save Purchase",true);save.addActionListener(e->{try{Product p=(Product)((JComboBox<?>)fs[3]).getSelectedItem();int n=Integer.parseInt(qty.getText());repo.adjust(p,n,"STOCK IN","PUR-"+(100+repo.products().size()));Toast.show(frame,"✓ Purchase saved successfully",Theme.GREEN);frame.refreshDashboard();}catch(Exception x){Toast.show(frame,"Enter a valid quantity",Theme.RED);}});g.gridy=7;card.add(save,g);DocumentListener d=new DocumentListener(){public void insertUpdate(DocumentEvent e){calc();}public void removeUpdate(DocumentEvent e){calc();}public void changedUpdate(DocumentEvent e){calc();}};qty.getDocument().addDocumentListener(d);price.getDocument().addDocumentListener(d);add(card,BorderLayout.CENTER);}private void calc(){try{total.setText(DashboardPanel.money(Double.parseDouble(qty.getText())*Double.parseDouble(price.getText())));}catch(Exception e){total.setText("₹0");}}private JComboBox<Product> products(){return new JComboBox<>(repo.products().toArray(new Product[0]));}private JComponent header(String a,String b){return (JComponent) MainFrame.page(a,b).getComponent(0);}}

final class StockOutPanel extends JPanel {private final MainFrame frame;private final InventoryRepository repo;private final JComboBox<Product> product;private final JTextField qty=new JTextField("1");private final JLabel available=Theme.label("Available Stock: —",15,true),remaining=Theme.label("Remaining Stock: —",15,true);private final JButton save=Theme.button("Save Stock Issue",true);StockOutPanel(MainFrame f,InventoryRepository r){super(new BorderLayout(0,16));frame=f;repo=r;setBorder(new EmptyBorder(24,28,24,28));add(MainFrame.page("Stock Out","Issue inventory while keeping stock accurate").getComponent(0),BorderLayout.NORTH);product=new JComboBox<>(repo.products().toArray(new Product[0]));JPanel c=new JPanel(new GridBagLayout());c.setBorder(Theme.card());GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(8,8,8,8);g.fill=GridBagConstraints.HORIZONTAL;g.gridx=0;g.gridy=0;c.add(Theme.label("Product",13,true),g);g.gridx=1;g.weightx=1;c.add(product,g);g.gridx=1;g.gridy=1;c.add(available,g);g.gridx=0;g.gridy=2;g.weightx=0;c.add(Theme.label("Issue Quantity",13,true),g);g.gridx=1;g.weightx=1;c.add(qty,g);g.gridy=3;c.add(remaining,g);g.gridy=4;c.add(save,g);add(c,BorderLayout.CENTER);product.addActionListener(e->validateStock());qty.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){validateStock();}public void removeUpdate(DocumentEvent e){validateStock();}public void changedUpdate(DocumentEvent e){validateStock();}});save.addActionListener(e->{Product p=(Product)product.getSelectedItem();int n=Integer.parseInt(qty.getText());repo.adjust(p,-n,"STOCK OUT","ISS-"+(100+repo.products().size()));Toast.show(frame,"✓ Stock issued successfully",Theme.GREEN);frame.refreshDashboard();validateStock();});validateStock();}private void validateStock(){Product p=(Product)product.getSelectedItem();try{int n=Integer.parseInt(qty.getText());available.setText("Available Stock: "+p.stock+" units");remaining.setText((n>p.stock?"⚠ Insufficient stock — ":"Remaining Stock: ")+(p.stock-n)+" units");remaining.setForeground(n>p.stock?Theme.RED:Theme.GREEN);save.setEnabled(n>0&&n<=p.stock);}catch(Exception e){save.setEnabled(false);}}}

final class AdjustmentPanel extends JPanel {
    private final MainFrame frame; private final InventoryRepository repo; private final JComboBox<Product> product; private final JTextField physical=new JTextField(); private final JTextArea remarks=new JTextArea(3,22); private final JLabel system=Theme.label("Current System Stock: —",15,true),difference=Theme.label("Difference: —",15,true); private final JButton save=Theme.button("Save Adjustment",true);
    AdjustmentPanel(MainFrame f,InventoryRepository r){super(new BorderLayout(0,16));frame=f;repo=r;setBorder(new EmptyBorder(24,28,24,28));add(MainFrame.page("Stock Adjustment","Correct physical inventory differences").getComponent(0),BorderLayout.NORTH);product=new JComboBox<>(r.products().toArray(new Product[0]));JPanel c=new JPanel(new GridBagLayout());c.setBorder(Theme.card());GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(7,8,7,8);g.fill=GridBagConstraints.HORIZONTAL;String[] labels={"Product","Current System Stock","Physical Stock *","Difference","Reason","Remarks"};JComponent[] fields={product,system,physical,difference,new JComboBox<>(new String[]{"Damaged","Expired","Count correction","Lost","Other"}),new JScrollPane(remarks)};for(int i=0;i<labels.length;i++){g.gridx=0;g.gridy=i;g.weightx=0;c.add(Theme.label(labels[i],13,true),g);g.gridx=1;g.weightx=1;c.add(fields[i],g);}g.gridx=1;g.gridy=6;JLabel warning=Theme.label("⚠ This adjustment will change the inventory quantity.",13,true);warning.setForeground(Theme.AMBER);c.add(warning,g);g.gridy=7;c.add(save,g);add(c,BorderLayout.CENTER);product.addActionListener(e->calculate());physical.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){calculate();}public void removeUpdate(DocumentEvent e){calculate();}public void changedUpdate(DocumentEvent e){calculate();}});save.addActionListener(e->commit());calculate();}
    private void calculate(){Product p=(Product)product.getSelectedItem();if(p==null)return;system.setText("Current System Stock: "+p.stock+" units");try{int count=Integer.parseInt(physical.getText());int d=count-p.stock;difference.setText("Difference: "+(d>0?"+":"")+d+" units");difference.setForeground(d==0?Theme.GREEN:Theme.AMBER);save.setEnabled(count>=0);}catch(Exception e){difference.setText("Difference: enter a valid physical quantity");difference.setForeground(Theme.RED);save.setEnabled(false);}}
    private void commit(){Product p=(Product)product.getSelectedItem();int count=Integer.parseInt(physical.getText());int delta=count-p.stock;if(delta==0){Toast.show(frame,"No adjustment is needed",Theme.AMBER);return;}if(JOptionPane.showConfirmDialog(frame,"Change "+p.name+" from "+p.stock+" to "+count+" units?","Confirm stock adjustment",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION){repo.adjust(p,delta,"ADJUSTMENT","ADJ-"+System.currentTimeMillis()%100000);Toast.show(frame,"✓ Stock adjustment saved",Theme.GREEN);frame.refreshDashboard();calculate();}}
}

final class InsightPanel extends JPanel implements Refreshable {private final MainFrame frame;private final InventoryRepository repo;private final boolean profit;private final JLabel summary=Theme.label("",15,false);private final JTable table=new JTable();InsightPanel(MainFrame f,InventoryRepository r,String title,String subtitle,boolean profit){super(new BorderLayout(0,16));frame=f;repo=r;this.profit=profit;setBorder(new EmptyBorder(24,28,24,28));add(MainFrame.page(title,subtitle).getComponent(0),BorderLayout.NORTH);JPanel card=new JPanel(new BorderLayout(0,12));card.setBorder(Theme.card());summary.setForeground(Theme.muted());card.add(summary,BorderLayout.NORTH);card.add(new JScrollPane(table),BorderLayout.CENTER);JButton action=Theme.button(profit?"Open Full Reports":"Review Low Stock",true);action.addActionListener(e->frame.showPage(profit?"Reports":"Low Stock"));JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));actions.add(action);card.add(actions,BorderLayout.SOUTH);add(card,BorderLayout.CENTER);}public void refresh(){if(profit){String[] h={"PRODUCT","STOCK","COST VALUE","SALES VALUE","POTENTIAL PROFIT"};Object[][] d=new Object[repo.products().size()][5];double total=0;for(int i=0;i<repo.products().size();i++){Product p=repo.products().get(i);double margin=p.stock*(p.selling-p.purchase);total+=margin;d[i]=new Object[]{p.name,p.stock,DashboardPanel.money(p.stock*p.purchase),DashboardPanel.money(p.stock*p.selling),DashboardPanel.money(margin)};}table.setModel(new DefaultTableModel(d,h){public boolean isCellEditable(int r,int c){return false;}});summary.setText("Potential profit from current inventory: "+DashboardPanel.money(total));}else{table.setModel(repo.productModel(repo.products()));summary.setText("Expiry dates are recorded during purchasing. No products are currently flagged for expiry in this demo.");}DashboardPanel.styleTable(table);}}

final class TransactionsPanel extends JPanel implements Refreshable {private final InventoryRepository repo;private final JTable table=new JTable();TransactionsPanel(MainFrame f,InventoryRepository r){super(new BorderLayout(0,16));repo=r;setBorder(new EmptyBorder(24,28,24,28));add(MainFrame.page("Transactions","Track every inventory movement").getComponent(0),BorderLayout.NORTH);JPanel c=new JPanel(new BorderLayout(0,10));c.setBorder(Theme.card());JPanel filters=new JPanel(new FlowLayout(FlowLayout.LEFT));for(String s:new String[]{"🔍 Search","Transaction Type ▾","Product ▾","From Date","To Date","Apply Filters","Reset"})filters.add(s.contains("Search")?new JTextField(s,18):Theme.button(s,false));c.add(filters,BorderLayout.NORTH);c.add(new JScrollPane(table),BorderLayout.CENTER);add(c,BorderLayout.CENTER);}public void refresh(){table.setModel(repo.transactionModel());DashboardPanel.styleTable(table);}}

final class ReportsPanel extends JPanel implements Refreshable {
    private final MainFrame frame; private final InventoryRepository repo; private final CardLayout views=new CardLayout(); private final JPanel body=new JPanel(views); private final JTable table=new JTable(); private final JLabel reportTitle=Theme.label("Inventory Report",20,true), summary=Theme.label("",13,false); private String current="Inventory";
    ReportsPanel(MainFrame f,InventoryRepository r){super(new BorderLayout(0,16));frame=f;repo=r;setBorder(new EmptyBorder(24,28,24,28));add(MainFrame.page("Reports","Generate actionable inventory insights").getComponent(0),BorderLayout.NORTH);body.add(landing(),"landing");body.add(detail(),"detail");add(body,BorderLayout.CENTER);}
    private JComponent landing(){JPanel g=new JPanel(new GridLayout(2,2,16,16));String[][] reports={{"▣ Inventory","Current Stock"},{"⚠ Low Stock","Needs Attention"},{"↓ Purchases","Stock In"},{"↑ Stock Issues","Stock Out"}};for(String[] a:reports){JPanel c=new JPanel();c.setBorder(Theme.card());c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));c.add(Theme.label(a[0],18,true));JLabel s=Theme.label(a[1],14,false);s.setForeground(Theme.muted());c.add(s);c.add(Box.createVerticalStrut(20));JButton b=Theme.button("Open Report",true);b.setAlignmentX(Component.LEFT_ALIGNMENT);b.addActionListener(e->{current=a[1];load();views.show(body,"detail");});c.add(b);g.add(c);}return g;}
    private JComponent detail(){JPanel p=new JPanel(new BorderLayout(0,12));JPanel title=new JPanel(new BorderLayout());JPanel copy=new JPanel();copy.setLayout(new BoxLayout(copy,BoxLayout.Y_AXIS));copy.add(reportTitle);summary.setForeground(Theme.muted());copy.add(summary);title.add(copy,BorderLayout.WEST);JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));JButton back=Theme.button("← All Reports",false);back.addActionListener(e->views.show(body,"landing"));JButton print=Theme.button("Print",false);print.addActionListener(e->{try{table.print();}catch(Exception x){Toast.show(frame,"Printing was cancelled",Theme.AMBER);}});JButton export=Theme.button("⇩ Export CSV",true);export.addActionListener(e->export());actions.add(back);actions.add(print);actions.add(export);title.add(actions,BorderLayout.EAST);p.add(title,BorderLayout.NORTH);JPanel c=new JPanel(new BorderLayout(0,10));c.setBorder(Theme.card());JPanel filter=new JPanel(new FlowLayout(FlowLayout.LEFT));JTextField search=new JTextField();search.setPreferredSize(new Dimension(250,34));search.putClientProperty("JTextField.placeholderText","Search this report...");search.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){filter(search.getText());}public void removeUpdate(DocumentEvent e){filter(search.getText());}public void changedUpdate(DocumentEvent e){filter(search.getText());}});filter.add(search);filter.add(Theme.button("Generated: "+LocalDate.now(),false));c.add(filter,BorderLayout.NORTH);DashboardPanel.styleTable(table);c.add(new JScrollPane(table),BorderLayout.CENTER);p.add(c,BorderLayout.CENTER);return p;}
    private void load(){reportTitle.setText(current+" Report");if(current.equals("Current Stock")){table.setModel(repo.productModel(repo.products()));summary.setText(repo.products().size()+" products • Current inventory valuation available in dashboard");}else if(current.equals("Needs Attention")){table.setModel(repo.productModel(repo.lowProducts()));summary.setText(repo.lowProducts().size()+" products require replenishment");}else{table.setModel(repo.transactionModel());summary.setText("All recorded "+(current.equals("Stock In")?"purchase":"issue")+" movements");}DashboardPanel.styleTable(table);}
    private void filter(String q){TableRowSorter<TableModel> sorter=new TableRowSorter<>(table.getModel());table.setRowSorter(sorter);sorter.setRowFilter(q.isBlank()?null:RowFilter.regexFilter("(?i)"+java.util.regex.Pattern.quote(q)));}
    private void export(){JFileChooser chooser=new JFileChooser();chooser.setDialogTitle("Export "+current+" report as CSV");chooser.setSelectedFile(new java.io.File(current.replace(" ","-").toLowerCase()+"-report.csv"));if(chooser.showSaveDialog(frame)!=JFileChooser.APPROVE_OPTION)return;try(java.io.PrintWriter out=new java.io.PrintWriter(chooser.getSelectedFile(),java.nio.charset.StandardCharsets.UTF_8)){TableModel m=table.getModel();for(int c=0;c<m.getColumnCount();c++){if(c>0)out.print(',');out.print('"'+m.getColumnName(c).replace("\"","\"\"")+'"');}out.println();for(int r=0;r<m.getRowCount();r++){for(int c=0;c<m.getColumnCount();c++){if(c>0)out.print(',');String v=String.valueOf(m.getValueAt(r,c));out.print('"'+v.replace("\"","\"\"")+'"');}out.println();}Toast.show(frame,"✓ Report exported successfully",Theme.GREEN);}catch(Exception e){Toast.show(frame,"Unable to export the report",Theme.RED);}}
    public void refresh(){if(!current.equals("Inventory"))load();}
}

final class Toast {static void show(Window owner,String message,Color color){JWindow w=new JWindow(owner);JPanel p=new JPanel(new BorderLayout());p.setBackground(color);p.setBorder(new EmptyBorder(12,18,12,18));JLabel l=new JLabel(message);l.setForeground(Color.WHITE);l.setFont(new Font("Segoe UI",Font.BOLD,13));p.add(l);w.setContentPane(p);w.pack();Point q=owner.getLocationOnScreen();w.setLocation(q.x+owner.getWidth()-w.getWidth()-28,q.y+owner.getHeight()-w.getHeight()-55);w.setAlwaysOnTop(true);w.setVisible(true);new javax.swing.Timer(2600,e->w.dispose()){{setRepeats(false);}}.start();}}

final class Product {int id,stock,minimum;String sku,name,category,supplier,location;double purchase,selling;Product(int id,String sku,String name,String category,String supplier,int stock,int minimum,double purchase,double selling,String location){this.id=id;this.sku=sku;this.name=name;this.category=category;this.supplier=supplier;this.stock=stock;this.minimum=minimum;this.purchase=purchase;this.selling=selling;this.location=location;}public String toString(){return name+" ("+sku+")";}}

final class Metrics {int products,stock,low,out;double value;}

/** Repository uses MySQL when configured; local sample data keeps the UI demonstrable if MySQL is unavailable. */

final class InventoryRepository {
    private Connection db;
    private final List<Product> local = new ArrayList<>();
    private final List<Object[]> tx = new ArrayList<>();

    InventoryRepository() {
        seed();
    }

    boolean connect() {
        try {
            Properties p = new Properties();

            try (var in = getClass().getResourceAsStream("/application.properties")) {
                if (in == null) {
                    throw new IllegalStateException(
                            "application.properties file not found"
                    );
                }
                p.load(in);
            }

            db = DriverManager.getConnection(
                    p.getProperty("db.url"),
                    p.getProperty("db.user"),
                    p.getProperty("db.password")
            );

            init();
            load();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void init() throws SQLException {
        try (Statement s = db.createStatement()) {
            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS products(" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "sku VARCHAR(80) UNIQUE, " +
                            "name VARCHAR(160), " +
                            "category VARCHAR(80), " +
                            "supplier VARCHAR(120), " +
                            "stock INT, " +
                            "minimum_stock INT, " +
                            "purchase_price DECIMAL(12,2), " +
                            "selling_price DECIMAL(12,2), " +
                            "location VARCHAR(80))"
            );

            s.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS transactions(" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "product_name VARCHAR(160), " +
                            "type VARCHAR(30), " +
                            "previous_stock INT, " +
                            "change_qty INT, " +
                            "new_stock INT, " +
                            "username VARCHAR(80), " +
                            "reference_no VARCHAR(80))"
            );
        }
    }

    private void load() throws SQLException {
        try (Statement s = db.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM products")) {

            if (!rs.next()) {
                for (Product p : local) {
                    insert(p);
                }
                return;
            }

            local.clear();

            do {
                local.add(read(rs));
            } while (rs.next());
        }
    }

    private Product read(ResultSet r) throws SQLException {
        return new Product(
                r.getInt("id"),
                r.getString("sku"),
                r.getString("name"),
                r.getString("category"),
                r.getString("supplier"),
                r.getInt("stock"),
                r.getInt("minimum_stock"),
                r.getDouble("purchase_price"),
                r.getDouble("selling_price"),
                r.getString("location")
        );
    }

    private void seed() {
        local.add(new Product(1, "KB-001", "HP Wireless Keyboard", "Accessories", "ABC Computers", 5, 10, 700, 999, "Rack A-02"));
        local.add(new Product(2, "MS-002", "Logitech Mouse", "Accessories", "ABC Computers", 2, 8, 450, 649, "Rack A-03"));
        local.add(new Product(3, "MN-003", "Dell 24-inch Monitor", "Electronics", "Tech Mart", 0, 5, 8000, 10499, "Rack B-01"));
        local.add(new Product(4, "RT-004", "TP-Link Router", "Networking", "Net Systems", 36, 8, 1800, 2499, "Rack C-04"));
        local.add(new Product(5, "CH-005", "Ergonomic Office Chair", "Furniture", "Office Furniture", 14, 4, 5200, 7500, "Floor 1"));

        tx.add(new Object[]{"25 Aug 2026", "HP Wireless Keyboard", "STOCK IN", "+50", "Admin", "SUCCESS"});
        tx.add(new Object[]{"25 Aug 2026", "Logitech Mouse", "STOCK OUT", "-10", "Rahul", "SUCCESS"});
        tx.add(new Object[]{"24 Aug 2026", "Dell Monitor", "ADJUSTMENT", "-2", "Admin", "WARNING"});
    }

    List<Product> products() {
        return local;
    }

    Metrics metrics() {
        Metrics m = new Metrics();
        m.products = local.size();

        for (Product p : local) {
            m.stock += p.stock;
            m.value += p.stock * p.purchase;

            if (p.stock == 0) {
                m.out++;
            } else if (p.stock <= p.minimum) {
                m.low++;
            }
        }

        return m;
    }

    List<Product> lowProducts() {
        return local.stream()
                .filter(p -> p.stock <= p.minimum)
                .toList();
    }

    void save(Product p) {
        if (p.id == 0) {
            p.id = local.stream()
                    .mapToInt(x -> x.id)
                    .max()
                    .orElse(0) + 1;

            local.add(p);

            if (db != null) {
                insert(p);
            }

        } else {
            int index = -1;

            for (int i = 0; i < local.size(); i++) {
                if (local.get(i).id == p.id) {
                    index = i;
                    break;
                }
            }

            if (index >= 0) {
                local.set(index, p);
            }

            if (db != null) {
                try (PreparedStatement s = db.prepareStatement(
                        "UPDATE products SET sku=?,name=?,category=?,supplier=?,stock=?,minimum_stock=?,purchase_price=?,selling_price=?,location=? WHERE id=?"
                )) {
                    bind(s, p);
                    s.setInt(10, p.id);
                    s.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void insert(Product p) {
        try (PreparedStatement s = db.prepareStatement(
                "INSERT INTO products(sku,name,category,supplier,stock,minimum_stock,purchase_price,selling_price,location) VALUES(?,?,?,?,?,?,?,?,?)"
        )) {
            bind(s, p);
            s.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void bind(PreparedStatement s, Product p) throws SQLException {
        s.setString(1, p.sku);
        s.setString(2, p.name);
        s.setString(3, p.category);
        s.setString(4, p.supplier);
        s.setInt(5, p.stock);
        s.setInt(6, p.minimum);
        s.setDouble(7, p.purchase);
        s.setDouble(8, p.selling);
        s.setString(9, p.location);
    }

    void delete(Product p) {
        local.removeIf(x -> x.id == p.id);

        if (db != null) {
            try (PreparedStatement s = db.prepareStatement(
                    "DELETE FROM products WHERE id=?"
            )) {
                s.setInt(1, p.id);
                s.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void adjust(Product p, int change, String type, String ref) {
        int previous = p.stock;
        p.stock += change;

        tx.add(0, new Object[]{
                LocalDate.now().toString(),
                p.name,
                type,
                (change > 0 ? "+" : "") + change,
                "Admin",
                "SUCCESS"
        });

        if (db != null) {
            try (
                    PreparedStatement a = db.prepareStatement(
                            "UPDATE products SET stock=? WHERE id=?"
                    );
                    PreparedStatement t = db.prepareStatement(
                            "INSERT INTO transactions(product_name,type,previous_stock,change_qty,new_stock,username,reference_no) VALUES(?,?,?,?,?,?,?)"
                    )
            ) {
                a.setInt(1, p.stock);
                a.setInt(2, p.id);
                a.executeUpdate();

                t.setString(1, p.name);
                t.setString(2, type);
                t.setInt(3, previous);
                t.setInt(4, change);
                t.setInt(5, p.stock);
                t.setString(6, "Admin");
                t.setString(7, ref);
                t.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    TableModel productModel(List<Product> ps) {
        String[] h = {
                "SKU", "PRODUCT", "CATEGORY",
                "SUPPLIER", "STOCK", "PRICE", "STATUS"
        };

        Object[][] d = new Object[ps.size()][h.length];

        for (int i = 0; i < ps.size(); i++) {
            Product p = ps.get(i);

            String st = p.stock == 0
                    ? "● OUT OF STOCK"
                    : p.stock <= p.minimum
                    ? "● LOW STOCK"
                    : "● AVAILABLE";

            d[i] = new Object[]{
                    p.sku,
                    p.name,
                    p.category,
                    p.supplier,
                    p.stock + " units",
                    DashboardPanel.money(p.selling),
                    st
            };
        }

        return new DefaultTableModel(d, h) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    TableModel transactionModel() {
        String[] h = {
                "DATE", "PRODUCT", "TYPE",
                "QUANTITY", "USER", "STATUS"
        };

        return new DefaultTableModel(
                tx.toArray(Object[][]::new), h
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }
}
