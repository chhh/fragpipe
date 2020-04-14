package com.dmtavt.fragpipe.tabs;

import com.dmtavt.fragpipe.Fragpipe;
import com.dmtavt.fragpipe.Version;
import com.dmtavt.fragpipe.api.Bus;
import com.dmtavt.fragpipe.messages.MessageClearCache;
import com.dmtavt.fragpipe.messages.MessageClearConsole;
import com.dmtavt.fragpipe.messages.MessageExportLog;
import com.dmtavt.fragpipe.messages.MessageKillAll;
import com.dmtavt.fragpipe.messages.MessageKillAll.REASON;
import com.dmtavt.fragpipe.messages.MessageRun;
import com.dmtavt.fragpipe.messages.MessageRunButtonEnabled;
import com.dmtavt.fragpipe.messages.MessageSaveAsWorkflow;
import com.dmtavt.fragpipe.messages.MessageShowAboutDialog;
import com.github.chhh.utils.OsUtils;
import com.github.chhh.utils.PathUtils;
import com.github.chhh.utils.StringUtils;
import com.github.chhh.utils.SwingUtils;
import com.github.chhh.utils.swing.FileChooserUtils;
import com.github.chhh.utils.swing.FileChooserUtils.FcMode;
import com.github.chhh.utils.swing.FormEntry;
import com.github.chhh.utils.swing.MigUtils;
import com.github.chhh.utils.swing.UiCheck;
import com.github.chhh.utils.swing.UiText;
import com.github.chhh.utils.swing.UiUtils;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import com.github.chhh.utils.swing.JPanelWithEnablement;
import com.github.chhh.utils.swing.TextConsole;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.tools.jconsole.Tab;
import umich.msfragger.params.ThisAppProps;

public class TabRun extends JPanelWithEnablement {
  private static final Logger log = LoggerFactory.getLogger(TabRun.class);
  public static final MigUtils mu = MigUtils.get();
  private static final String TAB_PREFIX = "tab-run.";
  private static final String LAST_WORK_DIR = "workdir.last-path";
  final TextConsole console;
  Color defTextColor;
  private UiText uiTextWorkdir;
  private UiCheck uiCheckDryRun;
  private JButton btnRun;
  private JPanel pTop;
  private JPanel pConsole;
  private UiCheck uiCheckWordWrap;

  public TabRun(TextConsole console) {
    this.console = console;
    init();
    initMore();
  }

  private void initMore() {
    Bus.registerQuietly(this);
  }

  private void clearConsole() {
    console.setText("");
  }

  public String getWorkdirText() {
    return uiTextWorkdir.getNonGhostText();
  }

  @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
  public void on(MessageClearConsole m) {
    clearConsole();
  }

  @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
  public void on(MessageRunButtonEnabled m) {
    btnRun.setEnabled(m.isEnabled);
  }

  private JPanel createPanelTop(TextConsole console) {
    JButton btnSaveAsWorkflow = UiUtils.createButton("Save current config as Workflow",
            e -> Bus.post(new MessageSaveAsWorkflow()));
    JButton btnAbout = UiUtils.createButton("About", e -> Bus.post(new MessageShowAboutDialog()));
    uiTextWorkdir = UiUtils.uiTextBuilder().cols(30).create();
    FormEntry feWorkdir = mu.feb("workdir", uiTextWorkdir).label("Output dir:")
        .tooltip("Processing results will be stored in this directory").create();
    JButton btnBrowse = feWorkdir
        .browseButton(() -> FileChooserUtils.builder("Select output directory")
                .mode(FcMode.DIRS_ONLY).multi(false)
                .paths(Stream.of(uiTextWorkdir.getNonGhostText(),
                    Fragpipe.propsVar().getProperty(LAST_WORK_DIR))).create(),
            "Select output directory",
            selected -> {
              uiTextWorkdir.setText(selected.get(0).toString());
            }
        );
    JButton btnOpenInFileManager = UiUtils.createButton("Open in File Manager", e -> {
      String text = uiTextWorkdir.getNonGhostText();
      if (StringUtils.isBlank(text)) {
        SwingUtils.showInfoDialog(TabRun.this, "Empty path", "Not exists");
        return;
      }
      Path existing = PathUtils.existing(text);
      if (existing == null) {
        SwingUtils
            .showInfoDialog(TabRun.this, "Path:\n'" + text + "'\nDoes not exist", "Not exists");
        return;
      }
      try {
        Desktop.getDesktop().open(existing.toFile());
      } catch (IOException ex) {
        SwingUtils
            .showErrorDialog(TabRun.this, "Could not open path in system file browser.", "Error");
        return;
      }
    });

    uiCheckDryRun = UiUtils.createUiCheck("Dry Run", false);
    btnRun = UiUtils
        .createButton("<html><b>RUN", e -> Bus.post(new MessageRun(isDryRun())));

    JButton btnStop = UiUtils
        .createButton("Stop", e -> Bus.post(new MessageKillAll(REASON.USER_ACTION)));
    JButton btnPrintCommands = UiUtils
        .createButton("Print Commands", e -> Bus.post(new MessageRun(true)));
    JButton btnExport = UiUtils.createButton("Export Log", e -> Bus.post(new MessageExportLog()));
    JButton btnReportErrors = UiUtils.createButton("Report Errors", e -> {
      final String issueTrackerAddress = Fragpipe.getPropFix(Version.PROP_ISSUE_TRACKER_URL);
      try {
        Desktop.getDesktop().browse(URI.create(issueTrackerAddress));
      } catch (IOException ex) {
        log.error("Exception while trying to open default browser: {}", ex.getMessage());
        SwingUtils.showErrorDialogWithStacktrace(ex, TabRun.this);
      }
    });
    JButton btnClearConsole = UiUtils.createButton("Clear Console", e -> clearConsole() );
    uiCheckWordWrap = UiUtils
        .createUiCheck("Word wrap", console.getScrollableTracksViewportWidth(), e -> {
          console.setScrollableTracksViewportWidth(uiCheckWordWrap.isSelected());
          console.setVisible(false);
          console.setVisible(true);
        });

    JPanel p = mu.newPanel("Top panel", mu.lcFillXNoInsetsTopBottom());
    mu.add(p, btnSaveAsWorkflow).split().spanX();
    mu.add(p, btnAbout).wrap();
    mu.add(p, feWorkdir.label(), false).split().spanX();
    mu.add(p, feWorkdir.comp).growX();
    mu.add(p, btnBrowse);
    mu.add(p, btnOpenInFileManager).wrap();
    mu.add(p, btnRun).split().spanX();
    mu.add(p, btnStop);
    mu.add(p, uiCheckDryRun).pushX();
    mu.add(p, btnPrintCommands);
    mu.add(p, btnExport);
    mu.add(p, btnReportErrors);
    mu.add(p, btnClearConsole);
    mu.add(p, uiCheckWordWrap).wrap();

    return p;
  }

  public boolean isDryRun() {
    return SwingUtils.isEnabledAndChecked(uiCheckDryRun);
  }

  protected void init() {
    defTextColor = UIManager.getColor("TextField.foreground");
    if (defTextColor == null) {
      defTextColor = Color.BLACK;
    }

    pTop = createPanelTop(console);

    //pTop.setMinimumSize(new Dimension(300, 50));
    initConsole(console);
    pConsole = createPanelConsole(console);

    mu.layout(this, mu.lcFillXNoInsetsTopBottom());
    mu.add(this, pTop).growX().growY().wrap();
    mu.add(this, pConsole).growX().wrap();
  }

  private JPanel createPanelConsole(TextConsole tc) {
    JPanel p = mu.newPanel("Console", mu.lcFillXNoInsetsTopBottom());
    JScrollPane scroll = new JScrollPane(tc);

    mu.add(p, scroll).growX().wrap();
    return p;
  }

  private void initConsole(TextConsole console) {
    final Font currentFont = console.getFont();

    console.setFont(new Font(Font.MONOSPACED, currentFont.getStyle(), currentFont.getSize()));
    console.setContentType("text/plain; charset=UTF-8");
    console.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          doPop(e);
        }
      }

      private void doPop(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem ctxItemExport = new JMenuItem("Export log to text file");
        ctxItemExport.addActionListener(e1 -> {
          Bus.post(new MessageExportLog());
        });
        menu.add(ctxItemExport);
        menu.show(e.getComponent(), e.getX(), e.getY());
      }
    });
  }
}
