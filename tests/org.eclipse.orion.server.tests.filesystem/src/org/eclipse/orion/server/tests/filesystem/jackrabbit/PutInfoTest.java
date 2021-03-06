/*******************************************************************************
 *  Copyright (c) 2010, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.tests.filesystem.jackrabbit;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;

public class PutInfoTest extends org.eclipse.core.tests.filesystem.PutInfoTest {

	protected void doFSSetUp() throws Exception {
		baseStore = EFS.getStore(URI.create("jackrabbit://test"));
		baseStore.mkdir(EFS.NONE, null);
	}
}
