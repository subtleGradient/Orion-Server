/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.tests.filesystem.performance;

import org.eclipse.orion.server.tests.filesystem.git.performance.GitFileStorePerformanceTest;
import org.eclipse.orion.server.tests.filesystem.git.performance.GitPerformanceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Runs performance tests for all file stores.
 */
@RunWith(Suite.class)
@SuiteClasses({ GitPerformanceTest.class, LocalPerformanceTest.class,
		GitFileStorePerformanceTest.class })
public class AllPerformanceTests {
	// goofy junit4, no class body needed
}
