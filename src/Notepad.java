import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.awt.datatransfer.*;

public class Notepad extends JFrame implements ActionListener, DocumentListener {
    // 菜单栏及菜单
    private JMenuBar menuBar;
    private JMenu menuFile, menuEdit, menuFormat, menuView, menuHelp;

    // 文件菜单项
    private JMenuItem menuItemNew, menuItemOpen, menuItemSave, menuItemSaveAs,
            menuItemPageSetup, menuItemPrint, menuItemExit;

    // 编辑菜单项
    private JMenuItem menuItemUndo, menuItemCut, menuItemCopy, menuItemPaste,
            menuItemDelete, menuItemFind, menuItemFindNext, menuItemReplace,
            menuItemGoTo, menuItemSelectAll, menuItemTimeDate;

    // 格式菜单项
    private JCheckBoxMenuItem menuItemWordWrap;
    private JMenuItem menuItemFont;

    // 查看菜单项
    private JCheckBoxMenuItem menuItemStatusBar;

    // 帮助菜单项
    private JMenuItem menuItemHelpTopics, menuItemAbout;

    // 右键弹出菜单及其菜单项
    private JPopupMenu popupMenu;
    private JMenuItem popupUndo, popupCut, popupCopy, popupPaste, popupDelete, popupSelectAll;

    // 文本编辑区域
    private JTextArea textArea;

    // 状态栏
    private JLabel statusBar;

    // 系统剪贴板
    private Clipboard clipboard;

    // 撤销管理器
    private UndoManager undoManager;

    // 文件相关变量
    private boolean isNewFile = true;
    private File currentFile = null;
    private String originalContent = "";

    // 构造方法
    public Notepad() {
        super("Java记事本");
        initializeComponents();
        initializeMenuBar();
        initializeTextArea();
        initializePopupMenu();
        initializeStatusBar();
        setFrameProperties();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                promptExit();
            }
        });
    }

    // 初始化组件
    private void initializeComponents() {
        // 设置全局字体
        Font globalFont = new Font("Dialog", Font.PLAIN, 12);
        for (Object key : UIManager.getDefaults().keySet()) {
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, globalFont);
            }
        }

        // 初始化剪贴板
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // 初始化撤销管理器
        undoManager = new UndoManager();
    }

    // 初始化菜单栏
    private void initializeMenuBar() {
        menuBar = new JMenuBar();

        // 文件菜单
        menuFile = new JMenu("文件(F)");
        menuFile.setMnemonic('F');

        menuItemNew = createMenuItem("新建(N)", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
        menuItemOpen = createMenuItem("打开(O)...", KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK);
        menuItemSave = createMenuItem("保存(S)", KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        menuItemSaveAs = createMenuItem("另存为(A)...", null, 0);
        menuItemPageSetup = createMenuItem("页面设置(U)...", null, 0);
        menuItemPrint = createMenuItem("打印(P)...", KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK);
        menuItemExit = createMenuItem("退出(X)", null, 0);

        menuFile.add(menuItemNew);
        menuFile.add(menuItemOpen);
        menuFile.add(menuItemSave);
        menuFile.add(menuItemSaveAs);
        menuFile.addSeparator();
        menuFile.add(menuItemPageSetup);
        menuFile.add(menuItemPrint);
        menuFile.addSeparator();
        menuFile.add(menuItemExit);

        // 编辑菜单
        menuEdit = new JMenu("编辑(E)");
        menuEdit.setMnemonic('E');
        menuEdit.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                updateEditMenuItems();
            }
            public void menuDeselected(MenuEvent e) {}
            public void menuCanceled(MenuEvent e) {}
        });

        menuItemUndo = createMenuItem("撤销(U)", KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);
        menuItemUndo.setEnabled(false);
        menuItemCut = createMenuItem("剪切(T)", KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK);
        menuItemCopy = createMenuItem("复制(C)", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        menuItemPaste = createMenuItem("粘贴(P)", KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
        menuItemDelete = createMenuItem("删除(D)", KeyEvent.VK_DELETE, 0);
        menuItemFind = createMenuItem("查找(F)...", KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        menuItemFindNext = createMenuItem("查找下一个(N)", KeyEvent.VK_F3, 0);
        menuItemReplace = createMenuItem("替换(R)...", KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK);
        menuItemGoTo = createMenuItem("转到(G)...", KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK);
        menuItemSelectAll = createMenuItem("全选(A)", KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK);
        menuItemTimeDate = createMenuItem("时间/日期(D)", KeyEvent.VK_F5, 0);

        menuEdit.add(menuItemUndo);
        menuEdit.addSeparator();
        menuEdit.add(menuItemCut);
        menuEdit.add(menuItemCopy);
        menuEdit.add(menuItemPaste);
        menuEdit.add(menuItemDelete);
        menuEdit.addSeparator();
        menuEdit.add(menuItemFind);
        menuEdit.add(menuItemFindNext);
        menuEdit.add(menuItemReplace);
        menuEdit.add(menuItemGoTo);
        menuEdit.addSeparator();
        menuEdit.add(menuItemSelectAll);
        menuEdit.add(menuItemTimeDate);

        // 格式菜单
        menuFormat = new JMenu("格式(O)");
        menuFormat.setMnemonic('O');

        menuItemWordWrap = new JCheckBoxMenuItem("自动换行(W)");
        menuItemWordWrap.setMnemonic('W');
        menuItemWordWrap.setState(true);
        menuItemWordWrap.addActionListener(this);

        menuItemFont = createMenuItem("字体(F)...", null, 0);

        menuFormat.add(menuItemWordWrap);
        menuFormat.add(menuItemFont);

        // 查看菜单
        menuView = new JMenu("查看(V)");
        menuView.setMnemonic('V');

        menuItemStatusBar = new JCheckBoxMenuItem("状态栏(S)");
        menuItemStatusBar.setMnemonic('S');
        menuItemStatusBar.setState(true);
        menuItemStatusBar.addActionListener(this);

        menuView.add(menuItemStatusBar);

        // 帮助菜单
        menuHelp = new JMenu("帮助(H)");
        menuHelp.setMnemonic('H');

        menuItemHelpTopics = createMenuItem("帮助主题(H)", KeyEvent.VK_F1, 0);
        menuItemAbout = createMenuItem("关于记事本(A)", null, 0);

        menuHelp.add(menuItemHelpTopics);
        menuHelp.addSeparator();
        menuHelp.add(menuItemAbout);

        // 添加所有菜单到菜单栏
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuFormat);
        menuBar.add(menuView);
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);
    }

    // 创建菜单项的辅助方法
    private JMenuItem createMenuItem(String title, Integer keyCode, int modifiers) {
        JMenuItem menuItem = new JMenuItem(title);
        if (keyCode != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
        }
        menuItem.addActionListener(this);
        return menuItem;
    }

    // 初始化文本编辑区域
    private void initializeTextArea() {
        textArea = new JTextArea(20, 50);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);

        originalContent = textArea.getText();

        // 注册撤销和文档监听器
        textArea.getDocument().addUndoableEditListener(new UndoHandler());
        textArea.getDocument().addDocumentListener(this);
    }

    // 初始化右键弹出菜单
    private void initializePopupMenu() {
        popupMenu = new JPopupMenu();
        popupUndo = createPopupMenuItem("撤销(U)", false);
        popupCut = createPopupMenuItem("剪切(T)", true);
        popupCopy = createPopupMenuItem("复制(C)", true);
        popupPaste = createPopupMenuItem("粘贴(P)", false);
        popupDelete = createPopupMenuItem("删除(D)", true);
        popupSelectAll = createPopupMenuItem("全选(A)", false);

        popupMenu.add(popupUndo);
        popupMenu.addSeparator();
        popupMenu.add(popupCut);
        popupMenu.add(popupCopy);
        popupMenu.add(popupPaste);
        popupMenu.add(popupDelete);
        popupMenu.addSeparator();
        popupMenu.add(popupSelectAll);

        // 添加鼠标监听器以显示弹出菜单
        textArea.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                handlePopup(e);
            }
            public void mouseReleased(MouseEvent e) {
                handlePopup(e);
            }
            private void handlePopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    updatePopupMenuItems();
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    // 创建弹出菜单项的辅助方法
    private JMenuItem createPopupMenuItem(String title, boolean requiresSelection) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(this);
        menuItem.setEnabled(!requiresSelection); // 根据是否需要选择文本设置初始可用性
        return menuItem;
    }

    // 初始化状态栏
    private void initializeStatusBar() {
        statusBar = new JLabel("　按F1获取帮助");
        add(statusBar, BorderLayout.SOUTH);
    }

    // 设置窗口属性
    private void setFrameProperties() {
        setLocation(100, 100);
        setSize(650, 550);
        setVisible(true);
        textArea.requestFocus();
    }

    // 更新编辑菜单项的可用性
    private void updateEditMenuItems() {
        String selectedText = textArea.getSelectedText();
        boolean hasSelection = selectedText != null;

        menuItemCut.setEnabled(hasSelection);
        menuItemCopy.setEnabled(hasSelection);
        menuItemDelete.setEnabled(hasSelection);

        popupCut.setEnabled(hasSelection);
        popupCopy.setEnabled(hasSelection);
        popupDelete.setEnabled(hasSelection);

        // 判断剪贴板内容是否可粘贴
        Transferable contents = clipboard.getContents(this);
        boolean canPaste = contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        menuItemPaste.setEnabled(canPaste);
        popupPaste.setEnabled(canPaste);
    }

    // 更新右键菜单项的可用性
    private void updatePopupMenuItems() {
        String selectedText = textArea.getSelectedText();
        boolean hasSelection = selectedText != null;

        popupCut.setEnabled(hasSelection);
        popupCopy.setEnabled(hasSelection);
        popupDelete.setEnabled(hasSelection);

        Transferable contents = clipboard.getContents(this);
        boolean canPaste = contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        popupPaste.setEnabled(canPaste);

        popupUndo.setEnabled(undoManager.canUndo());
    }

    // 提示用户保存更改并退出
    private void promptExit() {
        textArea.requestFocus();
        String currentContent = textArea.getText();
        boolean isChanged = !currentContent.equals(originalContent);

        if (isChanged) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "您的文件尚未保存，是否保存？",
                    "退出提示",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                if (saveFile()) {
                    System.exit(0);
                }
            } else if (choice == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
            // 如果选择取消，则不退出
        } else {
            System.exit(0);
        }
    }

    // 保存文件的辅助方法
    private boolean saveFile() {
        if (isNewFile) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("保存");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showSaveDialog(this);

            if (result == JFileChooser.CANCEL_OPTION) {
                statusBar.setText("您没有选择任何文件");
                return false;
            }

            File fileToSave = fileChooser.getSelectedFile();
            if (fileToSave == null || fileToSave.getName().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "不合法的文件名", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            currentFile = fileToSave;
            isNewFile = false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            writer.write(textArea.getText());
            originalContent = textArea.getText();
            setTitle(currentFile.getName() + " - 记事本");
            statusBar.setText("当前打开文件: " + currentFile.getAbsolutePath());
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "保存文件时出错", "错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // 查找功能
    private void find() {
        FindDialog findDialog = new FindDialog(this, textArea);
        findDialog.setVisible(true);
    }

    // 替换功能
    private void replace() {
        ReplaceDialog replaceDialog = new ReplaceDialog(this, textArea);
        replaceDialog.setVisible(true);
    }

    // 字体设置功能
    private void chooseFont() {
        FontDialog fontDialog = new FontDialog(this, textArea);
        fontDialog.setVisible(true);
    }

    // 处理菜单项的动作事件
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // 文件菜单操作
        if (source == menuItemNew) {
            handleNewFile();
        } else if (source == menuItemOpen) {
            handleOpenFile();
        } else if (source == menuItemSave) {
            saveFile();
        } else if (source == menuItemSaveAs) {
            handleSaveAs();
        } else if (source == menuItemPageSetup || source == menuItemPrint) {
            JOptionPane.showMessageDialog(this, "对不起，此功能尚未实现！更多请看http://pan.muyi.so", "提示", JOptionPane.WARNING_MESSAGE);
        } else if (source == menuItemExit) {
            promptExit();

            // 编辑菜单操作
        } else if (source == menuItemUndo || source == popupUndo) {
            handleUndo();
        } else if (source == menuItemCut || source == popupCut) {
            textArea.cut();
            updateEditMenuItems();
        } else if (source == menuItemCopy || source == popupCopy) {
            textArea.copy();
            updateEditMenuItems();
        } else if (source == menuItemPaste || source == popupPaste) {
            textArea.paste();
            updateEditMenuItems();
        } else if (source == menuItemDelete || source == popupDelete) {
            textArea.replaceSelection("");
            updateEditMenuItems();
        } else if (source == menuItemFind || source == menuItemFindNext) {
            find();
        } else if (source == menuItemReplace) {
            replace();
        } else if (source == menuItemGoTo) {
            JOptionPane.showMessageDialog(this, "对不起，此功能尚未实现！更多请看http://pan.muyi.so", "提示", JOptionPane.WARNING_MESSAGE);
        } else if (source == menuItemSelectAll || source == popupSelectAll) {
            textArea.selectAll();
        } else if (source == menuItemTimeDate) {
            Calendar calendar = Calendar.getInstance();
            textArea.insert(calendar.getTime().toString(), textArea.getCaretPosition());
        }

        // 格式菜单操作
        else if (source == menuItemWordWrap) {
            textArea.setLineWrap(menuItemWordWrap.isSelected());
        } else if (source == menuItemFont) {
            chooseFont();
        }

        // 查看菜单操作
        else if (source == menuItemStatusBar) {
            statusBar.setVisible(menuItemStatusBar.isSelected());
        }

        // 帮助菜单操作
        else if (source == menuItemHelpTopics) {
            JOptionPane.showMessageDialog(this, "路漫漫其修远兮，吾将上下而求索。", "帮助主题", JOptionPane.INFORMATION_MESSAGE);
        } else if (source == menuItemAbout) {
            JOptionPane.showMessageDialog(this,
                    "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n" +
                            " 电子科技大学选修课：Java课程作业 \n" +
                            " 编写时间：2024-05-14                          \n" +
                            " 一些地方借鉴他人，不足之处希望大家能提出意见，谢谢！  \n" +
                            "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n",
                    "记事本", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // 处理新建文件
    private void handleNewFile() {
        textArea.requestFocus();
        String currentContent = textArea.getText();
        boolean isChanged = !currentContent.equals(originalContent);

        if (isChanged) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "您的文件尚未保存，是否保存？",
                    "提示",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                if (saveFile()) {
                    clearEditor();
                }
            } else if (choice == JOptionPane.NO_OPTION) {
                clearEditor();
            }
            // 如果选择取消，则不执行任何操作
        } else {
            clearEditor();
        }
    }

    // 清空编辑区域并重置状态
    private void clearEditor() {
        textArea.setText("");
        setTitle("无标题 - 记事本");
        statusBar.setText(" 新建文件");
        isNewFile = true;
        currentFile = null;
        undoManager.discardAllEdits();
        menuItemUndo.setEnabled(false);
        originalContent = textArea.getText();
    }

    // 处理打开文件
    private void handleOpenFile() {
        textArea.requestFocus();
        String currentContent = textArea.getText();
        boolean isChanged = !currentContent.equals(originalContent);

        if (isChanged) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "您的文件尚未保存，是否保存？",
                    "提示",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                if (!saveFile()) {
                    return; // 如果保存失败，则取消打开操作
                }
            } else if (choice == JOptionPane.CANCEL_OPTION) {
                return; // 取消打开操作
            }
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("打开文件");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.CANCEL_OPTION) {
            statusBar.setText(" 您没有选择任何文件 ");
            return;
        }

        File fileToOpen = fileChooser.getSelectedFile();
        if (fileToOpen == null || fileToOpen.getName().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "不合法的文件名", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileToOpen))) {
            textArea.setText("");
            String line;
            while ((line = reader.readLine()) != null) {
                textArea.append(line + "\n");
            }
            setTitle(fileToOpen.getName() + " - 记事本");
            statusBar.setText(" 当前打开文件：" + fileToOpen.getAbsolutePath());
            isNewFile = false;
            currentFile = fileToOpen;
            originalContent = textArea.getText();
            undoManager.discardAllEdits();
            menuItemUndo.setEnabled(false);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "打开文件时出错", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 处理另存为操作
    private void handleSaveAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("另存为");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.CANCEL_OPTION) {
            statusBar.setText("　您没有选择任何文件");
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();
        if (fileToSave == null || fileToSave.getName().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "不合法的文件名", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
            writer.write(textArea.getText());
            originalContent = textArea.getText();
            setTitle(fileToSave.getName() + " - 记事本");
            statusBar.setText("　当前打开文件: " + fileToSave.getAbsolutePath());
            isNewFile = false;
            currentFile = fileToSave;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "保存文件时出错", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 处理撤销操作
    private void handleUndo() {
        textArea.requestFocus();
        if (undoManager.canUndo()) {
            try {
                undoManager.undo();
            } catch (CannotUndoException ex) {
                System.out.println("无法撤销: " + ex);
            }
        }
        if (!undoManager.canUndo()) {
            menuItemUndo.setEnabled(false);
        }
    }

    // 实现DocumentListener接口的方法
    public void insertUpdate(DocumentEvent e) {
        menuItemUndo.setEnabled(true);
    }

    public void removeUpdate(DocumentEvent e) {
        menuItemUndo.setEnabled(true);
    }

    public void changedUpdate(DocumentEvent e) {
        menuItemUndo.setEnabled(true);
    }

    // 撤销操作监听器
    private class UndoHandler implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {
            undoManager.addEdit(e.getEdit());
            menuItemUndo.setEnabled(undoManager.canUndo());
            popupUndo.setEnabled(undoManager.canUndo());
        }
    }

    // 主方法
    public static void main(String[] args) {
        Notepad editor = new Notepad();
        editor.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
}

// 查找对话框类
class FindDialog extends JDialog implements ActionListener {
    private JTextField txtFind;
    private JCheckBox chkMatchCase;
    private JRadioButton rdoUp, rdoDown;
    private JButton btnFindNext, btnCancel;
    private JTextArea textArea;

    public FindDialog(JFrame parent, JTextArea textArea) {
        super(parent, "查找", false);
        this.textArea = textArea;
        initializeComponents();
        setLayout(new GridLayout(3, 1));
        setSize(400, 180);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        JPanel panelFind = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblFind = new JLabel("查找内容(N)：");
        txtFind = new JTextField(15);
        panelFind.add(lblFind);
        panelFind.add(txtFind);

        JPanel panelOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chkMatchCase = new JCheckBox("区分大小写(C)");
        panelOptions.add(chkMatchCase);

        JPanel panelDirection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelDirection.setBorder(BorderFactory.createTitledBorder("方向"));
        rdoUp = new JRadioButton("向上(U)");
        rdoDown = new JRadioButton("向下(U)", true);
        ButtonGroup group = new ButtonGroup();
        group.add(rdoUp);
        group.add(rdoDown);
        panelDirection.add(rdoUp);
        panelDirection.add(rdoDown);
        panelOptions.add(panelDirection);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnFindNext = new JButton("查找下一个(F)");
        btnCancel = new JButton("取消");
        btnFindNext.addActionListener(this);
        btnCancel.addActionListener(this);
        panelButtons.add(btnFindNext);
        panelButtons.add(btnCancel);

        add(panelFind);
        add(panelOptions);
        add(panelButtons);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnFindNext) {
            performFindNext();
        } else if (e.getSource() == btnCancel) {
            dispose();
        }
    }

    private void performFindNext() {
        String findText = txtFind.getText();
        if (findText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写查找内容!", "提示", JOptionPane.WARNING_MESSAGE);
            txtFind.requestFocus();
            return;
        }

        String content = textArea.getText();
        String target = findText;

        if (!chkMatchCase.isSelected()) {
            content = content.toLowerCase();
            target = findText.toLowerCase();
        }

        int caretPosition = textArea.getCaretPosition();
        int index;

        if (rdoUp.isSelected()) {
            index = content.lastIndexOf(target, caretPosition - 1);
        } else {
            index = content.indexOf(target, caretPosition);
        }

        if (index != -1) {
            textArea.select(index, index + findText.length());
            textArea.setCaretPosition(index + findText.length());
        } else {
            JOptionPane.showMessageDialog(this, "找不到您查找的内容！", "查找", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

// 替换对话框类
class ReplaceDialog extends JDialog implements ActionListener {
    private JTextField txtFind, txtReplace;
    private JCheckBox chkMatchCase;
    private JRadioButton rdoUp, rdoDown;
    private JButton btnFindNext, btnReplace, btnReplaceAll, btnCancel;
    private JTextArea textArea;
    private int replaceCount = 0;

    public ReplaceDialog(JFrame parent, JTextArea textArea) {
        super(parent, "替换", false);
        this.textArea = textArea;
        initializeComponents();
        setLayout(new GridLayout(4, 1));
        setSize(420, 220);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        JPanel panelFind = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblFind = new JLabel("查找内容(N)：");
        txtFind = new JTextField(15);
        btnFindNext = new JButton("查找下一个(F)");
        btnFindNext.addActionListener(this);
        panelFind.add(lblFind);
        panelFind.add(txtFind);
        panelFind.add(btnFindNext);

        JPanel panelReplace = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblReplace = new JLabel("替换为(P)：");
        txtReplace = new JTextField(15);
        btnReplace = new JButton("替换(R)");
        btnReplace.addActionListener(this);
        btnReplaceAll = new JButton("全部替换(A)");
        btnReplaceAll.addActionListener(this);
        panelReplace.add(lblReplace);
        panelReplace.add(txtReplace);
        panelReplace.add(btnReplace);
        panelReplace.add(btnReplaceAll);

        JPanel panelOptions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chkMatchCase = new JCheckBox("区分大小写(C)");
        panelOptions.add(chkMatchCase);

        JPanel panelDirection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelDirection.setBorder(BorderFactory.createTitledBorder("方向"));
        rdoUp = new JRadioButton("向上(U)");
        rdoDown = new JRadioButton("向下(U)", true);
        ButtonGroup group = new ButtonGroup();
        group.add(rdoUp);
        group.add(rdoDown);
        panelDirection.add(rdoUp);
        panelDirection.add(rdoDown);
        panelOptions.add(panelDirection);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancel = new JButton("取消");
        btnCancel.addActionListener(this);
        panelButtons.add(btnCancel);

        add(panelFind);
        add(panelReplace);
        add(panelOptions);
        add(panelButtons);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == btnFindNext) {
            performFindNext();
        } else if (source == btnReplace) {
            performReplace();
        } else if (source == btnReplaceAll) {
            performReplaceAll();
        } else if (source == btnCancel) {
            dispose();
        }
    }

    private void performFindNext() {
        String findText = txtFind.getText();
        if (findText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写查找内容!", "提示", JOptionPane.WARNING_MESSAGE);
            txtFind.requestFocus();
            return;
        }

        String replaceText = txtReplace.getText();
        String content = textArea.getText();
        String target = findText;

        if (!chkMatchCase.isSelected()) {
            content = content.toLowerCase();
            target = findText.toLowerCase();
        }

        int caretPosition = textArea.getCaretPosition();
        int index;

        if (rdoUp.isSelected()) {
            index = content.lastIndexOf(target, caretPosition - 1);
        } else {
            index = content.indexOf(target, caretPosition);
        }

        if (index != -1) {
            textArea.select(index, index + findText.length());
            textArea.setCaretPosition(index + findText.length());
        } else {
            JOptionPane.showMessageDialog(this, "找不到您查找的内容！", "查找", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void performReplace() {
        String findText = txtFind.getText();
        String replaceText = txtReplace.getText();
        String selectedText = textArea.getSelectedText();

        if (selectedText != null) {
            if (!chkMatchCase.isSelected()) {
                if (selectedText.toLowerCase().equals(findText.toLowerCase())) {
                    textArea.replaceSelection(replaceText);
                    replaceCount++;
                }
            } else {
                if (selectedText.equals(findText)) {
                    textArea.replaceSelection(replaceText);
                    replaceCount++;
                }
            }
        }
    }

    private void performReplaceAll() {
        String findText = txtFind.getText();
        String replaceText = txtReplace.getText();

        if (findText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写查找内容!", "提示", JOptionPane.WARNING_MESSAGE);
            txtFind.requestFocus();
            return;
        }

        String content = textArea.getText();
        String target = findText;
        String replacement = replaceText;

        if (!chkMatchCase.isSelected()) {
            content = content.toLowerCase();
            target = findText.toLowerCase();
        }

        int index = content.indexOf(target);
        replaceCount = 0;

        while (index != -1) {
            textArea.select(index, index + findText.length());
            textArea.replaceSelection(replacement);
            content = textArea.getText();
            if (!chkMatchCase.isSelected()) {
                content = content.toLowerCase();
            }
            index = content.indexOf(target, index + replacement.length());
            replaceCount++;
        }

        if (replaceCount > 0) {
            JOptionPane.showMessageDialog(this, "成功替换 " + replaceCount + " 个匹配内容！", "替换成功", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "找不到您查找的内容！", "替换", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

// 字体设置对话框类
class FontDialog extends JDialog implements ActionListener, ListSelectionListener {
    private JTextField txtFont, txtStyle, txtSize;
    private JList<String> listFonts, listStyles, listSizes;
    private JLabel lblSample;
    private JButton btnOK, btnCancel;
    private JTextArea textArea;

    public FontDialog(JFrame parent, JTextArea textArea) {
        super(parent, "字体设置", false);
        this.textArea = textArea;
        initializeComponents();
        setLayout(new BorderLayout());
        setSize(350, 340);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        JPanel panelTop = new JPanel(new GridLayout(3, 2, 5, 5));
        JLabel lblFont = new JLabel("字体(F)：");
        txtFont = new JTextField(9);
        txtFont.setEditable(false);
        JLabel lblStyle = new JLabel("字形(Y)：");
        txtStyle = new JTextField(8);
        txtStyle.setEditable(false);
        JLabel lblSize = new JLabel("大小(S)：");
        txtSize = new JTextField(5);
        txtSize.setEditable(false);

        panelTop.add(lblFont);
        panelTop.add(txtFont);
        panelTop.add(lblStyle);
        panelTop.add(txtStyle);
        panelTop.add(lblSize);
        panelTop.add(txtSize);

        // 字体列表
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        listFonts = new JList<>(fontNames);
        listFonts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listFonts.addListSelectionListener(this);
        JScrollPane scrollFonts = new JScrollPane(listFonts);
        scrollFonts.setPreferredSize(new Dimension(150, 100));

        // 字形列表
        String[] fontStyles = {"常规", "粗体", "斜体", "粗斜体"};
        listStyles = new JList<>(fontStyles);
        listStyles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listStyles.addListSelectionListener(this);
        JScrollPane scrollStyles = new JScrollPane(listStyles);
        scrollStyles.setPreferredSize(new Dimension(100, 100));

        // 大小列表
        String[] fontSizes = {"8","9","10","11","12","14","16","18","20","22","24","26","28","36","48","72"};
        listSizes = new JList<>(fontSizes);
        listSizes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSizes.addListSelectionListener(this);
        JScrollPane scrollSizes = new JScrollPane(listSizes);
        scrollSizes.setPreferredSize(new Dimension(50, 100));

        JPanel panelLists = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelLists.add(scrollFonts);
        panelLists.add(scrollStyles);
        panelLists.add(scrollSizes);

        // 示例标签
        lblSample = new JLabel("张选仲的记事本-ZXZ's Notepad");
        lblSample.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel panelSample = new JPanel();
        panelSample.setBorder(BorderFactory.createTitledBorder("示例"));
        panelSample.add(lblSample);

        // 按钮
        btnOK = new JButton("确定");
        btnCancel = new JButton("取消");
        btnOK.addActionListener(this);
        btnCancel.addActionListener(this);
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelButtons.add(btnOK);
        panelButtons.add(btnCancel);

        // 设置初始字体信息
        Font currentFont = textArea.getFont();
        txtFont.setText(currentFont.getFontName());
        txtStyle.setText(getStyleName(currentFont.getStyle()));
        txtSize.setText(String.valueOf(currentFont.getSize()));

        listFonts.setSelectedValue(currentFont.getFontName(), true);
        listStyles.setSelectedIndex(getStyleIndex(currentFont.getStyle()));
        listSizes.setSelectedValue(String.valueOf(currentFont.getSize()), true);

        add(panelTop, BorderLayout.NORTH);
        add(panelLists, BorderLayout.CENTER);
        add(panelSample, BorderLayout.SOUTH);
        add(panelButtons, BorderLayout.PAGE_END);
    }

    // 获取字形名称
    private String getStyleName(int style) {
        switch (style) {
            case Font.PLAIN: return "常规";
            case Font.BOLD: return "粗体";
            case Font.ITALIC: return "斜体";
            case Font.BOLD + Font.ITALIC: return "粗斜体";
            default: return "常规";
        }
    }

    // 获取字形索引
    private int getStyleIndex(int style) {
        switch (style) {
            case Font.PLAIN: return 0;
            case Font.BOLD: return 1;
            case Font.ITALIC: return 2;
            case Font.BOLD + Font.ITALIC: return 3;
            default: return 0;
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == btnOK) {
            applyFont();
            dispose();
        } else if (source == btnCancel) {
            dispose();
        }
    }

    private void applyFont() {
        String fontName = txtFont.getText();
        String styleName = txtStyle.getText();
        int fontStyle = Font.PLAIN;

        switch (styleName) {
            case "常规": fontStyle = Font.PLAIN; break;
            case "粗体": fontStyle = Font.BOLD; break;
            case "斜体": fontStyle = Font.ITALIC; break;
            case "粗斜体": fontStyle = Font.BOLD + Font.ITALIC; break;
        }

        int fontSize = Integer.parseInt(txtSize.getText());
        Font newFont = new Font(fontName, fontStyle, fontSize);
        textArea.setFont(newFont);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            String selectedFont = listFonts.getSelectedValue();
            String selectedStyle = listStyles.getSelectedValue();
            String selectedSize = listSizes.getSelectedValue();

            if (selectedFont != null) {
                txtFont.setText(selectedFont);
            }
            if (selectedStyle != null) {
                txtStyle.setText(selectedStyle);
            }
            if (selectedSize != null) {
                txtSize.setText(selectedSize);
            }

            try {
                int style = Font.PLAIN;
                switch (selectedStyle) {
                    case "常规": style = Font.PLAIN; break;
                    case "粗体": style = Font.BOLD; break;
                    case "斜体": style = Font.ITALIC; break;
                    case "粗斜体": style = Font.BOLD + Font.ITALIC; break;
                }
                int size = Integer.parseInt(selectedSize);
                Font sampleFont = new Font(selectedFont, style, size);
                lblSample.setFont(sampleFont);
            } catch (NumberFormatException ex) {
                // 忽略格式错误
            }
        }
    }
}

// 字体对话框类（简化版）
class FontDialogSimple extends JDialog {
    // 可以根据需要进一步实现
}
