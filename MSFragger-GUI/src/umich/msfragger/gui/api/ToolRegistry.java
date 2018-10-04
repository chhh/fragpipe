/*
 * Copyright 2018 Dmitry Avtonomov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package umich.msfragger.gui.api;

import java.util.Map;
import umich.msfragger.gui.spi.ITool;

/**
 *
 * @author Dmitry Avtonomov
 */
public class ToolRegistry {
    private Map<String, ITool> tools;
    
    private ToolRegistry() {}
    
    public ITool getTool(String name) {
        return tools.get(name);
    }
    
    public void addTool(ITool tool) {
        ITool old = tools.putIfAbsent(tool.getName(), tool);
        if (old != null)
            throw new IllegalStateException("Trying to add a tool with name '" 
                    + tool.getName() + "'. Another tool with same name already "
                            + "exists in the registry.");
    }
}
