package com.dmtavt.fragpipe.tabs;

import static com.dmtavt.fragpipe.Fragpipe.fe;

import com.dmtavt.fragpipe.Fragpipe;
import com.dmtavt.fragpipe.FragpipeLocations;
import com.dmtavt.fragpipe.api.Notifications;
import com.dmtavt.fragpipe.api.Bus;
import com.dmtavt.fragpipe.api.PyInfo;
import com.dmtavt.fragpipe.exceptions.UnexpectedException;
import com.dmtavt.fragpipe.exceptions.ValidationException;
import com.dmtavt.fragpipe.messages.MessageBalloon;
import com.dmtavt.fragpipe.messages.MessageClearCache;
import com.dmtavt.fragpipe.messages.MessageDbNewPath;
import com.dmtavt.fragpipe.messages.MessageFindSystemPython;
import com.dmtavt.fragpipe.messages.MessageLcmsAddFolder;
import com.dmtavt.fragpipe.messages.MessageMsfraggerNewBin;
import com.dmtavt.fragpipe.messages.MessageMsfraggerUpdateAvailable;
import com.dmtavt.fragpipe.messages.MessagePhiDlProgress;
import com.dmtavt.fragpipe.messages.MessagePhilosopherNewBin;
import com.dmtavt.fragpipe.messages.MessagePythonNewBin;
import com.dmtavt.fragpipe.messages.MessageShowAboutDialog;
import com.dmtavt.fragpipe.messages.MessageUiRevalidate;
import com.dmtavt.fragpipe.messages.MessageUmpireEnabled;
import com.dmtavt.fragpipe.messages.NoteConfigDbsplit;
import com.dmtavt.fragpipe.messages.NoteConfigMsfragger;
import com.dmtavt.fragpipe.messages.NoteConfigPhilosopher;
import com.dmtavt.fragpipe.messages.NoteConfigPython;
import com.dmtavt.fragpipe.messages.NoteConfigSpeclibgen;
import com.dmtavt.fragpipe.messages.NoteFragpipeUpdate;
import com.dmtavt.fragpipe.tools.fragger.Msfragger;
import com.dmtavt.fragpipe.tools.fragger.Msfragger.Version;
import com.dmtavt.fragpipe.tools.philosopher.PhiDownloadProgress;
import com.dmtavt.fragpipe.tools.philosopher.Philosopher;
import com.dmtavt.fragpipe.tools.philosopher.Philosopher.UpdateInfo;
import com.github.chhh.utils.JarUtils;
import com.github.chhh.utils.OsUtils;
import com.github.chhh.utils.PathUtils;
import com.github.chhh.utils.StringUtils;
import com.github.chhh.utils.swing.ContentChangedFocusAdapter;
import com.github.chhh.utils.swing.FileChooserUtils;
import com.github.chhh.utils.swing.FileChooserUtils.FcMode;
import com.github.chhh.utils.swing.FormEntry;
import com.github.chhh.utils.swing.HtmlStyledJEditorPane;
import com.github.chhh.utils.swing.MigUtils;
import com.github.chhh.utils.swing.UiCheck;
import com.github.chhh.utils.swing.UiText;
import com.github.chhh.utils.swing.UiUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jooq.lambda.Seq;
import org.jsoup.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;
import com.dmtavt.fragpipe.params.ThisAppProps;
import com.github.chhh.utils.SwingUtils;
import com.github.chhh.utils.swing.JPanelWithEnablement;
import com.dmtavt.fragpipe.tools.fragger.MsfraggerProps;

public class TabConfig extends JPanelWithEnablement {

  private static final Logger log = LoggerFactory.getLogger(TabConfig.class);

  private static final MigUtils mu = MigUtils.get();
  private UiText uiTextBinFragger;
  private HtmlStyledJEditorPane epFraggerVer;
  private UiText uiTextBinPhi;
  private HtmlStyledJEditorPane epPhiVer;
  private UiText uiTextBinPython;
  private HtmlStyledJEditorPane epPythonVer;
  private HtmlStyledJEditorPane epDbsplitText;
  private HtmlStyledJEditorPane epDbsplitErr;
  private Container epDbsplitErrParent;
  private HtmlStyledJEditorPane epSpeclibgenText;
  private HtmlStyledJEditorPane epSpeclibgenErr;
  private Container epSpeclibgenErrParent;
  private JButton btnAbout;
  public static final String TIP_MSFRAGGER_BIN = "tip.msfragger.bin";
  public static final String TIP_PHILOSOPHER_BIN = "tip.pholosopher.bin";
  public static final String TIP_PYTHON_BIN = "tip.python.bin";
  private static final String TIP_DBSPLIT = "tip.dbsplit";
  private static final String TIP_SPECLIBGEN = "tip.speclibgen";
  private static final String TIP_FRAGPIPE_UPDATE = "tip.fragpipe.update";
  public static final String TAB_PREFIX = "fragpipe-config.";


  public TabConfig() {
    init();
    initMore();
  }

  private void initMore() {
    Bus.register(this);
  }

  private void init() {
    this.setLayout(new MigLayout(new LC().fillX()));
    add(createPanelTopButtons(), new CC().growX().wrap());
    add(createPanelFragger(), new CC().growX().wrap());
    add(createPanelPhilosopher(), new CC().growX().wrap());
    add(createPanelPython(), new CC().growX().wrap());
    add(createPanelDbsplit(), new CC().growX().wrap());
    add(createPanelSpeclibgen(), new CC().growX().wrap());
    add(createPanelBottomInfo(), new CC().growX().wrap());
    add(createPanelBottomLink(), new CC().growX().wrap());
  }

  private JLabel createPanelBottomInfo() {
    JLabel c = new JLabel();
    c.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    c.setAlignmentX(Component.CENTER_ALIGNMENT);
    c.setText(SwingUtils.makeHtml(
        "Tabs on top represent processing steps and will be performed sequentially.\n"
            + "Tabs will become enabled once the tools on this panel are configured."));
    return c;
  }

  private JPanel createPanelBottomLink() {
    Properties props = ThisAppProps.getRemotePropertiesWithLocalDefaults();
//      Properties props = ThisAppProps.getLocalProperties(); // for testing
    String linkUrl = props.getProperty(ThisAppProps.PROP_SETUP_TUTORIAL_URL,
        "https://msfragger.nesvilab.org/tutorial_setup_fragpipe.html");

    JEditorPane c = SwingUtils.createClickableHtml(
        "<a href='" + linkUrl + "'>Configuration Help</a>");
    c.setAlignmentX(Component.CENTER_ALIGNMENT);
    JPanel p = new JPanel();
    p.setAlignmentX(Component.CENTER_ALIGNMENT);
    p.add(c);

    return p;
  }

  private JPanel createPanelTopButtons() {
    JPanel p = newMigPanel();
    btnAbout = UiUtils.createButton("About", e -> Bus.post(new MessageShowAboutDialog()));
    JButton btnOpenCacheInExplorer = SwingUtils
        .createButtonOpenInFileManager(this, "Open cache location",
            () -> FragpipeLocations.get().getPathLongTermStorage());
    mu.add(p, btnAbout).split().spanX();
    mu.add(p, btnOpenCacheInExplorer);
    mu.add(p, UiUtils.createButton("Clear Cache and Close", e -> Bus.post(new MessageClearCache(true))));

    if (com.dmtavt.fragpipe.Version.isDevBuild() || log.isDebugEnabled()) {
      mu.add(p, UiUtils.createButton("Debug button", e -> {
        Bus.post(new MessageMsfraggerNewBin("C:\\Users\\chhh\\lib\\msfragger\\MSFragger-2.4\\MSFragger-2.4.jar"));
        Bus.post(new MessagePhilosopherNewBin("C:\\Users\\chhh\\lib\\philosopher\\philosopher_v3.2.3_windows_amd64\\philosopher.exe"));
        Bus.post(new MessageDbNewPath("D:\\ms-data\\fasta\\2019-09-26-td-RefSeq.20180629_Human_ucsc_hg38_cpdbnr_mito_264contams.fasta"));
        Bus.post(new MessageLcmsAddFolder(Seq.of("D:\\ms-data\\TMTIntegrator_v1.1.4\\TMT-I-Test\\tmti-test-data_5-min-cuts").map(Paths::get).toList()));
//        log.debug("Debugging python environment vars");
//        NoteConfigPython configPython = Fragpipe.getStickyStrict(NoteConfigPython.class);
//        PyInfo.modifyEnvironmentVariablesForPythonSubprocesses(configPython.pi.getCommand(), new HashMap<>());



      }));
    }

    UiCheck uiCheckUmpire = UiUtils.createUiCheck("Enable DIA-Umpire", false,
        e -> Bus.post(new MessageUmpireEnabled(((JCheckBox) e.getSource()).isSelected())));
    p.add(uiCheckUmpire, ccL());
    JLabel sysInfo = new JLabel(SwingUtils.makeHtml(
        OsUtils.OsInfo() + "\n"
            + OsUtils.JavaInfo()
            + "\nFragPipe: v" + com.dmtavt.fragpipe.Version.version(false)));
            //+ FragpipeLocations.get().getJarPath().toString()));
    sysInfo.setVerticalAlignment(JLabel.TOP);
    p.add(sysInfo, ccR().wrap());
    //p.add(UiUtils.createButton("Find tools", e -> post(new MessageFindTools())), ccL.get().split().spanX());
    JLabel label = new JLabel("Main tools configuration");
    label.setFont(new Font(label.getFont().getName(), Font.BOLD, label.getFont().getSize() + 3));
    p.add(label, ccL().wrap());
    return p;
  }

  private JPanel createPanelFragger() {
    JPanel p = newMigPanel();
    p.setBorder(new TitledBorder("MSFragger"));

    final String binMsfraggerTip = "Select path to MSFragger.jar";
    uiTextBinFragger = UiUtils.uiTextBuilder().create();
    uiTextBinFragger.addFocusListener(new ContentChangedFocusAdapter(uiTextBinFragger, (s, s2) -> {
      Bus.post(new MessageMsfraggerNewBin(s2));
    }));
    FormEntry feBinMsfragger = fe(uiTextBinFragger, "bin-msfragger", TAB_PREFIX)
        .tooltip(binMsfraggerTip).create();
    p.add(feBinMsfragger.comp, ccL().split().growX());

    JButton btnBrowse = feBinMsfragger
        .browseButton("Browse", binMsfraggerTip, this::createFraggerFilechooser, paths -> {
          paths.stream().findFirst()
              .ifPresent(jar -> Bus.post(new MessageMsfraggerNewBin(jar.toString())));
        });
    p.add(btnBrowse, ccL());
    JButton btnUpdate = UiUtils.createButton("Update", this::actionMsfraggerUpdate);
    JButton btnDownload = UiUtils.createButton("Download", this::actionMsfraggerDownload);

    btnUpdate.setToolTipText(SwingUtils.makeHtml("Open MSFragger upgrader tool in browser.\n" +
        "In order to update you <b>must</b> download an\n" +
        "original copy from the <b>download</b> website once."));
    p.add(btnUpdate, ccL());
    p.add(btnDownload, ccL().wrap());
    epFraggerVer = new HtmlStyledJEditorPane("MSFragger version: N/A");
    p.add(Fragpipe.renameNoCache(epFraggerVer, "msfragger.version-info", TAB_PREFIX),
        ccL().spanX().growX().wrap());
    JEditorPane ep = SwingUtils.createClickableHtml(createFraggerCitationBody());
    p.add(ep, ccL().spanX().growX().wrap());
    return p;
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN_ORDERED)
  public void on(NoteFragpipeUpdate m) {
    log.debug("Got NoteFragpipeUpdate: {}", m.toString());
    StringBuilder sb = new StringBuilder();
    if (StringUtils.isNotBlank(m.releaseVer)) {
      sb.append(String.format("FragPipe update available, new version %s\n", m.releaseVer));
    }
    if (StringUtils.isNotBlank(m.downloadUrl)) {
      sb.append(String.format("<a href=\"%s\">Click here to download</a>\n", m.downloadUrl));
    }
    if (StringUtils.isNotBlank(m.announcement)) {
      if (sb.length() != 0)
        sb.append("\n");
      sb.append(m.announcement).append("n");
    }
    if (sb.length() == 0) {
      log.warn("Received NoteFragpipeUpdate message, but did not compose any user notification out of it.");
    } else {
      HtmlStyledJEditorPane ep = SwingUtils.createClickableHtml(true, sb.toString());
      ep.setBackground(Color.white);
      Bus.postSticky(new MessageBalloon(TIP_FRAGPIPE_UPDATE, btnAbout, ep));
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
  public void on(MessageMsfraggerUpdateAvailable m) {
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format("There is a newer version of MSFragger available [%s].<br>\n", m.newVersion));

    JEditorPane ep = SwingUtils.createClickableHtml(sb.toString(), Notifications.BG_COLOR);
    JPanel content = new JPanel(new BorderLayout());
    content.setBackground(ep.getBackground());

    JPanel pBtns = new JPanel();
    pBtns.setBackground(ep.getBackground());
    pBtns.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

    if (!StringUtils.isNullOrWhitespace(m.manualDownloadUrl)) {
      JButton btnManualUpdate = new JButton("Download update");
      btnManualUpdate.addActionListener(e -> {
        try {
          SwingUtils.openBrowserOrThrow(new URI(m.manualDownloadUrl));
        } catch (URISyntaxException ex) {
          throw new IllegalStateException("Incorrect url/uri", ex);
        }
      });
      pBtns.add(btnManualUpdate);
    }

    JButton btnClose = new JButton("Close");
    btnClose.addActionListener(e -> {
      Bus.post(new MessageBalloon(TIP_MSFRAGGER_BIN));
    });

    content.add(ep, BorderLayout.CENTER);
    pBtns.add(btnClose);
    content.add(pBtns, BorderLayout.SOUTH);

    Bus.post(new MessageBalloon(TIP_MSFRAGGER_BIN, uiTextBinFragger, content));
  }

  private CC ccL() {
    return new CC().alignX("left");
  }

  private CC ccR() {
    return new CC().alignX("right");
  }

  private JPanel newMigPanel() {
    //return new JPanel(new MigLayout(new LC().fillX().insetsAll("0px")));
    return new JPanel(new MigLayout(new LC().fillX()));
  }

  private void actionMsfraggerUpdate(ActionEvent evt) {
    try {
      String url = MsfraggerProps.getProperties()
          .getProperty(MsfraggerProps.PROP_UPDATESERVER_WEBSITE_URL);
      Desktop.getDesktop().browse(URI.create(url));
    } catch (IOException ex) {
      throw new IllegalStateException("Could not open MSFragger update link in browser.", ex);
    }
  }

  private void actionMsfraggerDownload(ActionEvent e) {
    try {
      final String downloadUrl = MsfraggerProps.getProperties()
          .getProperty(MsfraggerProps.PROP_DOWNLOAD_URL, "");
      Desktop.getDesktop().browse(URI.create(downloadUrl));
    } catch (IOException ex) {
      throw new IllegalStateException("Could not open MSFragger download link in browser.", ex);
    }
  }

  private JFileChooser createFraggerFilechooser() {
    JFileChooser fc = FileChooserUtils.create("Select MSFragger jar", "Select",
        false, FcMode.FILES_ONLY, true,
        new FileNameExtensionFilter("JAR files", "jar"));
    FileChooserUtils.setPath(fc, Stream.of(
        uiTextBinFragger.getNonGhostText(),
        Fragpipe.propsVarGet(ThisAppProps.PROP_BINARIES_IN),
        FragpipeLocations.get().getDirFragpipeRoot().toString()));
    return fc;
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  public void on(MessagePhilosopherNewBin m) {
    if (StringUtils.isBlank(m.path)) {
      Bus.postSticky(new NoteConfigPhilosopher(null, "N/A"));
      return;
    }
    try {
      Philosopher.Version v = Philosopher.validate(m.path);
      String version = v.version + (StringUtils.isBlank(v.build) ? "" : " (build " + v.build + ")");
      Bus.postSticky(new NoteConfigPhilosopher(m.path, version));
    } catch (ValidationException | UnexpectedException e) {
      Bus.postSticky(new NoteConfigPhilosopher(m.path, "N/A", e));
    }
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN_ORDERED)
  public void on(NoteConfigPhilosopher m) {
    log.debug("Got {}", m);
    uiTextBinPhi.setText(m.path);

    Path existing = PathUtils.existing(m.path);
    if (existing != null) {
      Fragpipe.propsVarSet(ThisAppProps.PROP_BINARIES_IN, existing.toString());
      if (existing.toString().contains(" ")) {
        SwingUtils.showWarningDialog(this, "There are spaces in Philosopher path.\n"
            + "It is <b>HIGHLY</b> recommended to use a path without spaces.\n"
            + "Depending on your OS/Java/Input files some tools that FragPipe\n"
            + "runs might crash.", "Spaces in path");
      }
    }

    if (m.ex != null) {
      epPhiVer.setText("Philosopher version: N/A");
      showConfigError(m.ex, TIP_PHILOSOPHER_BIN, uiTextBinPhi);
    } else {
      epPhiVer.setText("Philosopher version: " + m.version);
      Notifications.tryClose(TIP_PHILOSOPHER_BIN);
      checkPhilosopherUpdateAsync(m.path);
    }
  }

  private void checkPhilosopherUpdateAsync(String path) {
    Observable<UpdateInfo> obs = Observable
        .fromCallable(() -> Philosopher.checkUpdates(path))
        .subscribeOn(Schedulers.io());
    obs.subscribe(info -> {
      log.debug("Got philosopher update info with updateAvailable={}", info.isUpdateAvailable);
      if (info.isUpdateAvailable) {
        MessageBalloon tip = new MessageBalloon(TIP_PHILOSOPHER_BIN, uiTextBinPhi,
            SwingUtils.makeHtml(String.format("Philosopher update available.\n<a href=\"%s\">Click here</a> to download.", info.downloadUrl)));
        Notifications.tryOpen(tip);
      }
    }, throwable -> {
      log.warn("Something happened when checking for philosopher updates", throwable);
    });
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  public void on(MessageMsfraggerNewBin m) {
    if (StringUtils.isBlank(m.binPath)) {
      return;
    }
    Version v;
    try {
      Msfragger.validateJar(m.binPath);
      v = Msfragger.version(Paths.get(m.binPath));
      if (v.isVersionParsed) {
        Bus.postSticky(new NoteConfigMsfragger(m.binPath, v.version));
      } else {
        Bus.postSticky(new NoteConfigMsfragger(m.binPath, v.version, true, null));
      }
    } catch (ValidationException | UnexpectedException e) {
      Bus.postSticky(new NoteConfigMsfragger(m.binPath, "N/A", e));
    }
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN_ORDERED)
  public void on(NoteConfigMsfragger m) {
    log.debug("Got {}", m);
    uiTextBinFragger.setText(m.path);

    Path existing = PathUtils.existing(m.path);
    if (existing != null) {
      Fragpipe.propsVarSet(ThisAppProps.PROP_BINARIES_IN, existing.toString());
      if (existing.toString().contains(" ")) {
        SwingUtils.showWarningDialog(this, "There are spaces in MSFragger path.\n"
            + "It is <b>HIGHLY</b> recommended to use a path without spaces.\n"
            + "Depending on your OS/Java/Input files some tools that FragPipe\n"
            + "runs might crash.", "Spaces in path");
      }
    }

    if (m.ex != null) {
      epFraggerVer.setText("MSFragger version: N/A");
      showConfigError(m.ex, TIP_MSFRAGGER_BIN, uiTextBinFragger);
    } else if (m.isTooOld) {
      epFraggerVer.setText("MSFragger version: too old, not supported anymore");
      Bus.post(new MessageBalloon(TIP_MSFRAGGER_BIN, uiTextBinFragger,
          "This version is not supported anymore.\n"
              + "Download a newer one."));
    } else {
      epFraggerVer.setText("MSFragger version: " + m.version);
      Notifications.tryClose(TIP_MSFRAGGER_BIN);
    }
    if (m.isValid()) {
      Msfragger.checkUpdates(m);
    }
  }

  @Subscribe
  public void on(MessageUiRevalidate m) {
    log.debug("Got MessageUiRevalidate");
    String binFragger = uiTextBinFragger.getNonGhostText();
    if (StringUtils.isNotBlank(binFragger)) {
      Bus.post(new MessageMsfraggerNewBin(binFragger));
    }
    String binPhi = uiTextBinPhi.getNonGhostText();
    if (StringUtils.isNotBlank(binPhi)) {
      Bus.post(new MessagePhilosopherNewBin(binPhi));
    }
    String binPython = uiTextBinPython.getNonGhostText();
    if (StringUtils.isNotBlank(binPython)) {
      Bus.post(new MessagePythonNewBin(binPython));
    } else {
      // python was not loaded, try finding system python
      Bus.post(new MessageFindSystemPython());
    }
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  public void on(MessageFindSystemPython m) {
    try {
      PyInfo pi = PyInfo.findSystemPython(3);
      log.debug("Found system wide python install: {}", pi);
      if (pi != null) {
        Bus.post(new MessagePythonNewBin(pi.getCommand()));
      } else {
        log.debug("Did not find system python");
      }
    } catch (UnexpectedException e) {
      log.warn("Problems while searching for system wide python installation", e);
    }
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  public void on(MessagePythonNewBin m) {
    PyInfo pi;
    try {
      // first check if the path is absolute, then it must exist
      Path path = Paths.get(m.command);
      final boolean fileExists = Files.exists(path) || (OsUtils.isWindows() && Files.exists(Paths.get(path.toString() + ".exe")));
      if (path.isAbsolute() && !fileExists) {
        throw new ValidationException("File not exists");
      }

      // if paths.get didn't throw, we can try the binary, it might be on PATH
      pi = PyInfo.fromCommand(m.command);
      if (pi.getMajorVersion() < 3) {
        Bus.postSticky(
            new NoteConfigPython(pi, new ValidationException("Python version 3+ required"),
                pi.getCommand(), pi.getVersion()));
      } else {
        Bus.postSticky(new NoteConfigPython(pi));
      }
    } catch (ValidationException | UnexpectedException e) {
      Bus.postSticky(new NoteConfigPython(null, e, m.command, "N/A"));
    }
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN_ORDERED)
  public void on(NoteConfigPython m) {
    uiTextBinPython.setText(m.command);
    epPythonVer.setText(StringUtils.isBlank(m.version) ? "Python version: N/A" : "Python version: " + m.version);
    if (m.ex != null) {
      showConfigError(m.ex, TIP_PYTHON_BIN, uiTextBinPython);
    }
  }

  private String textDbsplitEnabled(boolean isEnabled) {
    return "DB Splitting: <b>" + (isEnabled ? "Enabled" : "Disabled") + "</b>\n"
        + "Used for searching very large databases by splitting into smaller chunks.";
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN_ORDERED)
  public void on(NoteConfigDbsplit m) {
    if (m.ex != null) {
      log.debug("Got NoteDbsplitConfig with exception set");
      if (epDbsplitErrParent != null && !epDbsplitErrParent.isAncestorOf(epDbsplitErr)) {
        epDbsplitErrParent.add(epDbsplitErr, new CC().wrap());
        epDbsplitErr.setVisible(true);
      }
      epDbsplitText.setText(textDbsplitEnabled(false));
      if (m.ex instanceof ValidationException) {
        epDbsplitErr.setText(m.ex.getMessage());
      } else {
        showConfigError(m.ex, TIP_DBSPLIT, epDbsplitText);
      }
      this.revalidate();
      return;
    }
    if (m.instance == null) {
      throw new IllegalStateException("If no exception is reported from DBSplit init, instance should not be null");
    }
    log.debug("Got NoteDbsplitConfig without exceptions");

    epDbsplitErrParent = epDbsplitErr.getParent();
    if (epDbsplitErrParent != null) {
      epDbsplitErrParent.remove(epDbsplitErr);
    }
    epDbsplitText.setText(textDbsplitEnabled(true));
    this.revalidate();
  }

  private String textSpeclibgenEnabled(boolean isEnabled) {
    return "Spec lib generation: <b>" + (isEnabled ? "Enabled" : "Disabled") + "</b>";
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN_ORDERED)
  public void on(NoteConfigSpeclibgen m) {
    if (m.ex != null) {
      log.debug("Got NoteConfigSpeclibgen with exception set");
      if (epSpeclibgenErrParent != null && !epSpeclibgenErrParent.isAncestorOf(epSpeclibgenErr)) {
        epSpeclibgenErrParent.add(epSpeclibgenErr, new CC().wrap());
        epSpeclibgenErr.setVisible(true);
      }
      epSpeclibgenText.setText(textSpeclibgenEnabled(false));
      if (m.ex instanceof ValidationException) {
        epSpeclibgenErr.setText(m.ex.getMessage());
      } else {
        showConfigError(m.ex, TIP_SPECLIBGEN, epSpeclibgenText);
      }
      this.revalidate();
      return;
    }

    if (m.instance == null) {
      throw new IllegalStateException("If no exception is reported from Speclibgen init, instance should not be null");
    }

    log.debug("Got NoteConfigSpeclibgen without exceptions");
    List<String> errMsgLines = new ArrayList<>();
    if (!m.instance.missingModulesSpeclibgen.isEmpty()) {
      errMsgLines.add("Missing python modules: " + Seq.seq(m.instance.missingModulesSpeclibgen).map(pm -> pm.installName).toString(", "));
    }
    if (!m.instance.missingModulesSpectrast.isEmpty()) {
      errMsgLines.add("Missing python modules for SpectraST: " + Seq.seq(m.instance.missingModulesSpectrast).map(pm -> pm.installName).toString(", "));
    }
    if (!m.instance.missingModulesEasyPqp.isEmpty()) {
      errMsgLines.add("Missing python modules for EasyPQP: " + Seq.seq(m.instance.missingModulesEasyPqp).map(pm -> pm.installName).toString(", "));
    }

    if (errMsgLines.isEmpty()) {
      epSpeclibgenErrParent = epSpeclibgenErr.getParent();
      if (epSpeclibgenErrParent != null) {
        epSpeclibgenErrParent.remove(epSpeclibgenErr);
      }
    } else {
      epSpeclibgenErr.setText(String.join("\n", errMsgLines));
    }

    epSpeclibgenText.setText(textSpeclibgenEnabled(m.instance.isSomeSpeclibgenAvailable()));
    this.revalidate();
  }

  private void showConfigError(Throwable e, String balloonTopic, JComponent balloonParent) {
    if (e instanceof ValidationException) {
      Bus.post(new MessageBalloon(balloonTopic, balloonParent, e.getMessage()));
    } else {
      SwingUtils.showErrorDialogWithStacktrace(e, this);
    }
  }

  public static String createFraggerCitationHtml(Font font) {
    return SwingUtils.wrapInStyledHtml(createFraggerCitationBody(), font);
  }

  private static String createFraggerCitationBody() {
    final Properties p = ThisAppProps.getRemotePropertiesWithLocalDefaults();
    final String linkMsfragger = p
        .getProperty(MsfraggerProps.PROP_FRAGGER_SITE_URL, "https://nesvilab.github.io/MSFragger/");
    final String linkFragpipe = p
        .getProperty(ThisAppProps.PROP_FRAGPIPE_SITE_URL, "https://github.com/Nesvilab/FragPipe");
    final String doi = p.getProperty(ThisAppProps.PROP_MANUSCRIPT_DOI, "10.1038/nmeth.4256");
    final String linkManuscript = p.getProperty(ThisAppProps.PROP_MANUSCRIPT_URL,
        "http://www.nature.com/nmeth/journal/v14/n5/full/nmeth.4256.html");
    final StringBuilder sb = new StringBuilder();

    sb.append("<p style=\"margin-top: 0\">");
    sb.append("<b>Please cite: </b>");
    sb.append(
        "<a href=\"").append(linkManuscript).append(
        "\">MSFragger: ultrafast and comprehensive peptide identification in mass spectrometry–based proteomics</a>");
    sb.append("<br/>");
    sb.append("<b>DOI: </b>").append(doi);
    sb.append("</p>");

    sb.append("<p style=\"margin-top: 10\">");

    sb.append("More info and docs: <a href=\"").append(linkMsfragger)
        .append("\">MSFragger website</a>")
        .append(", <a href=\"").append(linkFragpipe).append("\">FragPipe GitHub page</a>");
    return sb.toString();
  }

  private JPanel createPanelPhilosopher() {
    JPanel p = newMigPanel();
    p.setBorder(new TitledBorder("Philosopher"));

    final String tip = "Select path to Philosopher binary";
    uiTextBinPhi = UiUtils.uiTextBuilder().create();
    uiTextBinPhi.addFocusListener(new ContentChangedFocusAdapter(uiTextBinPhi, (s, s2) -> {
      Bus.post(new MessagePhilosopherNewBin(s2));
    }));
    FormEntry feBin = fe(uiTextBinPhi, "bin-philosopher", TAB_PREFIX)
        .tooltip(tip).create();
    p.add(feBin.comp, ccL().split().growX());

    JButton btnBrowse = feBin
        .browseButton("Browse", tip, this::createPhilosopherFilechooser,
            paths -> paths.stream().findFirst()
                .ifPresent(bin -> Bus.post(new MessagePhilosopherNewBin(bin.toString()))));
    p.add(btnBrowse, ccL());
    JButton btnUpdate = UiUtils.createButton("Update", this::actionPhilosopherDownload);
    JButton btnDownload = UiUtils.createButton("Download", this::actionPhilosopherDownload);
    p.add(btnUpdate, ccL());
    p.add(btnDownload, ccL().wrap());

    epPhiVer = new HtmlStyledJEditorPane("Philosopher version: N/A");
    p.add(Fragpipe.rename(epPhiVer, "philosopher.version-info", TAB_PREFIX, true),
        ccL().spanX().growX().wrap());
    p.add(SwingUtils.createClickableHtml(createPhilosopherCitationBody()),
        ccL().spanX().growX().wrap());
    return p;
  }

  private JPanel createPanelPython() {
    JPanel p = newMigPanel();
    p.setBorder(new TitledBorder("Python"));
    final String tip = "Python 3 is required for Spectral Library generation and DB splitting";
    final String ghost = "Select Python 3 binary (Anaconda Python recommended)";
    uiTextBinPython = UiUtils.uiTextBuilder().ghost(ghost).create();
    SwingUtils.addOnFocusLostAndContentChanged(uiTextBinPython, (s, s2) -> {
      Bus.post(new MessagePythonNewBin(s2));
    });
    FormEntry fe = fe(uiTextBinPython, "bin-python", TAB_PREFIX).tooltip(tip).create();

    p.add(fe.comp, ccL().split().growX());
    JButton btnBrowse = fe.browseButton("Browse", ghost, this::createPythonFilechooser, paths ->
        paths.stream().findFirst()
            .ifPresent(bin -> Bus.post(new MessagePythonNewBin(bin.toString()))));
    p.add(btnBrowse, ccL());
    final String url = ThisAppProps.def().getProperty(ThisAppProps.PROP_PYTHON_DOWNLOAD_URL);
    if (StringUtils.isNotBlank(url)) {
      JButton btnDonwload = UiUtils
          .createButton("Download", e -> SwingUtils.openBrowserOrThrow(url));
      p.add(btnDonwload, ccL().wrap());
    }
    epPythonVer = new HtmlStyledJEditorPane("Python version: N/A");
    p.add(epPythonVer, ccL().wrap());

    return p;
  }

  private JPanel createPanelDbsplit() {
    JPanel p = mu.newPanel("DB Splitting", true);

    StringBuilder tip = new StringBuilder()
        .append("Used for searching very large databases by splitting into smaller chunks.<br/>")
        .append("Requires <b>Python 3</b> with packages <b>Numpy, Pandas</b>")
        .append("Ways to get everything set up:").append("<ul>")
        .append("<li>Install Python 3 if you don't yet have it.</li>")
        .append(
            "<li>Install required python modules using <i>pip</i>, the python package manager, with command:</li>")
        .append("<ul>").append("<li>pip install numpy pandas</li>").append("</ul>")
        .append("</ul>");
    String tipHtml = SwingUtils.makeHtml(tip.toString());
    p.setToolTipText(tipHtml);

    Dimension dim = new Dimension(400, 25);
    epDbsplitText = new HtmlStyledJEditorPane(textDbsplitEnabled(false));
    epDbsplitText.setToolTipText(tipHtml);
    epDbsplitText.setPreferredSize(dim);
    epDbsplitErr = new HtmlStyledJEditorPane("Requires Python 3 with modules Numpy and Pandas.");
    epDbsplitErr.setPreferredSize(dim);

    mu.add(p, epDbsplitText).growX().pushX().wrap();
    mu.add(p, epDbsplitErr).growX().pushX().wrap();

    return p;
  }

  private JPanel createPanelSpeclibgen() {
    JPanel p = mu.newPanel("Spectral Library Generation", true);

    StringBuilder tip = new StringBuilder()
        .append("Requires <b>Python 3</b> with packages <b>Cython, Matplotlib, msproteomicstools</b>\n")
        .append("Optionally requires python module EasyPQP to enable EasyPQP functinoality.");
    String tipHtml = SwingUtils.makeHtml(tip.toString());
    p.setToolTipText(tipHtml);
    Dimension dim = new Dimension(200, 25);
    epSpeclibgenText = new HtmlStyledJEditorPane(textDbsplitEnabled(false));
    epSpeclibgenText.setToolTipText(tipHtml);
    epSpeclibgenText.setPreferredSize(dim);
    epSpeclibgenErr = new HtmlStyledJEditorPane("Requires Python 3 with modules Cython, Matplotlib, msproteomicstools.");
    epSpeclibgenErr.setPreferredSize(dim);

    mu.add(p, epSpeclibgenText).growX().wrap();
    mu.add(p, epSpeclibgenErr).growX().wrap();

    return p;
  }

  private JFileChooser createPhilosopherFilechooser() {
    JFileChooser fc = FileChooserUtils.create("Select Philosopher binary", "Select",
        false, FcMode.FILES_ONLY, true);
    if (OsUtils.isWindows()) {
      fc.addChoosableFileFilter(new FileNameExtensionFilter("Executables", "exe"));
    }
    FileChooserUtils.setPath(fc, Stream.of(
        uiTextBinPhi.getNonGhostText(),
        Fragpipe.propsVarGet(ThisAppProps.PROP_BINARIES_IN),
        JarUtils.getCurrentJarPath()));
    return fc;
  }

  private JFileChooser createPythonFilechooser() {
    JFileChooser fc = FileChooserUtils.create("Select Python 3 binary", "Select",
        false, FcMode.FILES_ONLY, true);
    if (OsUtils.isWindows()) {
      fc.addChoosableFileFilter(new FileNameExtensionFilter("Executables", "exe"));
    }

    PyInfo sysPy = null;
    try {
      sysPy = PyInfo.findSystemPython(3);
    } catch (UnexpectedException e) {
      log.debug("Something happened while checking system python for Python bin file chooser", e);
    }

    Stream<String> pathsToCheck = Stream
        .of(uiTextBinPython.getNonGhostText(), sysPy == null ? "" : sysPy.getCommand())
        .filter(StringUtils::isNotBlank)
        .flatMap(text -> {
          try {
            Path p = Paths.get(text);
            return p.isAbsolute() ? Stream.of(p, p.getParent()) : Stream.of(p);
          } catch (Exception e) {
            return Stream.empty();
          }
        }).distinct().map(Path::toString);
    FileChooserUtils.setPath(fc, pathsToCheck);
    return fc;
  }

  private String createPhilosopherCitationBody() {
    StringBuilder sb = new StringBuilder();
    sb.append("<p style=\"margin-top: 0\">");
    sb.append(
        "More info: <a href=\"https://nesvilab.github.io/philosopher/\">Philosopher GitHub page</a>");
    sb.append("<br/>");
    sb.append("</p>");
    return sb.toString();
  }

  private void actionPhilosopherDownload(ActionEvent e) {
    int choice = SwingUtils.showChoiceDialog(TabConfig.this, "Choose download type",
        "Do you want to attemp automatic download?\n"
            + "If you choose \"Manually\", you will be redirected to the download website.",
        new String[]{"Automatically", "Manually", "Cancel"}, 0);
    switch (choice) {
      case 0:
        new Thread(() -> {
          try {
            SwingUtils.setUncaughtExceptionHandlerMessageDialog(TabConfig.this);
            Philosopher.downloadPhilosopherAutomatically();
          } catch (IOException ex) {
            throw new IllegalStateException("Error downloading Philosopher automatically", ex);
          }
        }).start();
        break;
      case 1:
        Philosopher.downloadPhilosopherManually();
        break;
    }
  }
}
