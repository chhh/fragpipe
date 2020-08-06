package com.dmtavt.fragpipe.cmd;

import com.dmtavt.fragpipe.Fragpipe;
import com.dmtavt.fragpipe.api.InputLcmsFile;
import com.dmtavt.fragpipe.util.RewritePepxml;
import com.github.chhh.utils.StringUtils;
import com.github.chhh.utils.UsageTrigger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

  public boolean configure(Path jarFragpipe, UsageTrigger usePhi, int threads, String cmdLineOpts,
      List<Tuple2<InputLcmsFile, Path>> lcmsToPepxml) {
    initPreConfig();

    String phi = usePhi.getBin(); // TODO: the directory where it's invoked is not given, so there won't be a workspace

    lcmsToPepxml.stream().map

    for (Tuple2<InputLcmsFile, Path> kv : lcmsToPepxml) {
      InputLcmsFile lcms = kv.v1();
      Path pepxml = kv.v2();

      // we never know if pepxml is rewritten or not, so always rewrite
      ProcessBuilder pbRewrite = pbRewritePepxml(jarFragpipe, pepxml);
      pbRewrite.directory(wd.toFile());
      pbis.add(new PbiBuilder().setName("Rewrite pepxml").setPb(pbRewrite).create());

      // PTM Prophet itself
      List<String> cmd = new ArrayList<>();
      cmd.add(phi);
      cmd.add("ptmprophet ");
      cmd.add("--maxthreads ");
      cmd.add(Integer.toString(Math.max(1, threads)));
      Seq.of(cmdLineOpts.split(" "))
          .map(String::trim).filter(StringUtils::isNotBlank).forEach(cmd::add);
      cmd.add(pepxml.toString());

      final ProcessBuilder pb = new ProcessBuilder(cmd);
      pb.directory(lcms.getPath().getParent().toFile()); // PTM Prophet is run from the directory where the RAW is
      pbis.add(new PbiBuilder().setPb(pb).create());
    }

    isConfigured = true;
    return true;
  }

  @Override
  public boolean usesPhi() {
    return true;
  }

  private static ProcessBuilder pbRewritePepxml(Path jarFragpipe, Path pepxml) {
    if (jarFragpipe == null) {
      throw new IllegalArgumentException("jar can't be null");
    }
    List<String> cmd = new ArrayList<>();
    cmd.add(Fragpipe.getBinJava());
    cmd.add("-cp");
    cmd.add(jarFragpipe.toAbsolutePath().toString());
    cmd.add(RewritePepxml.class.getCanonicalName());
    cmd.add(pepxml.toAbsolutePath().normalize().toString());
    return new ProcessBuilder(cmd);
  }
}
