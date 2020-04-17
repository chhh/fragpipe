package com.dmtavt.fragpipe.messages;

import java.util.List;
import com.dmtavt.fragpipe.process.RunnableDescription;

public class MessageStartProcesses {
  public final List<RunnableDescription> runDescs;

  public MessageStartProcesses(List<RunnableDescription> pbi) {
    this.runDescs = pbi;
  }
}
