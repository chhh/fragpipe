package com.dmtavt.fragpipe.cmd;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.github.chhh.utils.UsageTrigger;

public class CmdPhilosopherWorkspaceClean extends CmdBase {
  public static final String NAME = "WorkspaceClean";

  public CmdPhilosopherWorkspaceClean(boolean isRun, Path workDir) {
    super(isRun, workDir);
  }

  public CmdPhilosopherWorkspaceClean(boolean isRun, String title, Path workDir) {
    super(isRun, title, workDir);
  }

  @Override
  public String getCmdName() {
    return NAME;
  }

  public boolean configure(UsageTrigger usePhilosopher) {
    initPreConfig();

    {
      List<String> cmd = new ArrayList<>();
      cmd.add(usePhilosopher.useBin(wd));
      cmd.addAll(asParts("workspace --clean --nocheck"));
      ProcessBuilder pb = new ProcessBuilder(cmd);
      pb.directory(wd.toFile());
      pbis.add(PbiBuilder.from(pb));
    }

    isConfigured = true;
    return true;
  }

  @Override
  public boolean usesPhi() {
    return true;
  }

}
