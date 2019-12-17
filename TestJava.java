/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package examples;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import cc.kave.commons.model.events.CommandEvent;
import cc.kave.commons.model.events.IIDEEvent;
import cc.kave.commons.utils.io.ReadingArchive;
import cc.kave.commons.utils.io.json.JsonUtils;

/**
 * This class contains several code examples that explain how to read enriched
 * event streams with the CARET platform. It cannot be run, the code snippets
 * serve as documentation.
 */
public class EventExamples {

	/**
	 * 4: Processing events
	 */
	private static void process(IIDEEvent event) {
		// once you have access to the instantiated event you can dispatch the
		// type. As the events are not nested, we did not implement the visitor
		// pattern, but resorted to instanceof checks.
		if (event instanceof CommandEvent) {
			// if the correct type is identified, you can cast it...
			CommandEvent ce = (CommandEvent) event;
			// ...and access the special context for this kind of event
			System.out.println(ce.CommandId);
		} else {
			// there a many different event types to process, it is recommended
			// that you browse the package to see all types and consult the
			// website for the documentation of the semantics of each event...
		}
	}
} 
