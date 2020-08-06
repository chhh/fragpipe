package com.dmtavt.fragpipe.cmd;

import com.dmtavt.fragpipe.api.InputLcmsFile;
import com.github.chhh.utils.StringUtils;
import com.github.chhh.utils.UsageTrigger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.NotImplementedException;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

public class CmdPtmProphet extends CmdBase {
  public static String NAME = "PtmProphet";

  public CmdPtmProphet(boolean isRun, Path workDir) {
    super(isRun, workDir);
  }

  @Override
  public String getCmdName() {
    return NAME;
  }

  public boolean configure(UsageTrigger usePhi, int threads, String cmdLineOpts,
      List<Tuple2<InputLcmsFile, Path>> lcmsToPepxml) {
    initPreConfig();

    String phi = usePhi.getBin(); // TODO: the directory where it's invoked is not given, so there won't be a workspace

    for (Tuple2<InputLcmsFile, Path> kv : lcmsToPepxml) {
      InputLcmsFile lcms = kv.v1();
      Path pepxml = kv.v2();

      List<String> cmd = new ArrayList<>();
      cmd.add(phi);
      cmd.add("ptmprophet ");
      cmd.add("--maxthreads ");
      cmd.add(Integer.toString(Math.max(1, threads)));
      Seq.of(cmdLineOpts.split(" "))
          .map(String::trim).filter(StringUtils::isNotBlank).forEach(cmd::add);
      cmd.add(pepxml.toString());
    }

    isConfigured = true;
    return true;
  }

  @Override
  public boolean usesPhi() {
    return true;
  }
}
