/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.internal.server.servlets;

import java.net.URI;
import java.net.URL;
import java.util.*;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.*;
import org.eclipse.orion.internal.server.core.IAliasRegistry;
import org.eclipse.orion.internal.server.core.IWebResourceDecorator;
import org.eclipse.orion.internal.server.servlets.xfer.TransferResourceDecorator;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Activator for the server servlet bundle. Responsible for tracking the HTTP
 * service and registering/unregistering servlets.
 */
public class Activator implements BundleActivator, IAliasRegistry {

	public static volatile BundleContext bundleContext;

	/**
	 * Global flag for enabling debug tracing
	 */
	public static final boolean DEBUG = true;

	public static final String LOCATION_FILE_SERVLET = "/file"; //$NON-NLS-1$
	public static final String LOCATION_PROJECT_SERVLET = "/project"; //$NON-NLS-1$

	public static final String PI_SERVER_SERVLETS = "org.eclipse.orion.server.servlets"; //$NON-NLS-1$
	static Activator singleton;

	private Map<String, URI> aliases = Collections.synchronizedMap(new HashMap<String, URI>());
	private ServiceTracker<IWebResourceDecorator, IWebResourceDecorator> decoratorTracker;

	private URI rootStoreURI;
	private ServiceRegistration<IWebResourceDecorator> transferDecoratorRegistration;

	public static Activator getDefault() {
		return singleton;
	}

	public BundleContext getContext() {
		return bundleContext;
	}

	private synchronized ServiceTracker<IWebResourceDecorator, IWebResourceDecorator> getDecoratorTracker() {
		if (decoratorTracker == null) {
			decoratorTracker = new ServiceTracker<IWebResourceDecorator, IWebResourceDecorator>(bundleContext, IWebResourceDecorator.class, null);
			decoratorTracker.open();
		}
		return decoratorTracker;
	}

	/**
	 * Returns the root file system location for the workspace.
	 */
	public IPath getPlatformLocation() {
		BundleContext context = Activator.getDefault().getContext();
		Collection<ServiceReference<Location>> refs;
		try {
			refs = context.getServiceReferences(Location.class, Location.INSTANCE_FILTER);
		} catch (InvalidSyntaxException e) {
			// we know the instance location filter syntax is valid
			throw new RuntimeException(e);
		}
		if (refs.isEmpty())
			return null;
		ServiceReference<Location> ref = refs.iterator().next();
		Location location = context.getService(ref);
		try {
			if (location == null)
				return null;
			URL root = location.getURL();
			if (root == null)
				return null;
			// strip off file: prefix from URL
			return new Path(root.toExternalForm().substring(5));
		} finally {
			context.ungetService(ref);
		}
	}

	/**
	 * Returns the root location for storing content and metadata on this server.
	 */
	public URI getRootLocationURI() {
		return rootStoreURI;
	}

	public Collection<IWebResourceDecorator> getWebResourceDecorators() {
		ServiceTracker<IWebResourceDecorator, IWebResourceDecorator> tracker = getDecoratorTracker();
		return tracker.getTracked().values();
	}

	private void initializeFileSystem() {
		IPath location = getPlatformLocation();
		if (location == null)
			throw new RuntimeException("Unable to compute base file system location"); //$NON-NLS-1$

		// try Git repo first
		//		try {
		//			String path = getPlatformLocation().append("SHARED_REPO").toString();
		//			rootStoreURI = new URI("gitfs:/" + path + "?/");
		//
		//			// check that Git FS exists
		//			EFS.getFileSystem("gitfs");
		//
		//			return;
		//		} catch (URISyntaxException e) {
		//			if (DEBUG)
		//				System.out.println("Git repo is not accessible ");
		//		} catch (CoreException e) {
		//			if (DEBUG)
		//				System.out.println("Git repo is not accessible ");
		//		}

		//		if (result == null)
		//			// try Jackrabbit JCR repo
		//			try {
		//				result = EFS.getFileSystem("jackrabbit").getStore(new Path("//" + location.lastSegment()));
		//			} catch (CoreException e) {
		//				if (DEBUG)
		//					System.out.println("Jackrabbit JCR repo is not accessible ");
		//			}

		// fall back to using local file system
		IFileStore rootStore = EFS.getLocalFileSystem().getStore(location);
		try {
			rootStore.mkdir(EFS.NONE, null);
			rootStoreURI = rootStore.toURI();
		} catch (CoreException e) {
			throw new RuntimeException("Instance location is read only: " + rootStore, e); //$NON-NLS-1$
		}
	}

	public URI lookupAlias(String alias) {
		return aliases.get(alias);
	}

	public void registerAlias(String alias, URI location) {
		aliases.put(alias, location);
	}

	private void registerTransferDecorator() {
		transferDecoratorRegistration = bundleContext.registerService(IWebResourceDecorator.class, new TransferResourceDecorator(), null);
	}

	public void start(BundleContext context) throws Exception {
		singleton = this;
		bundleContext = context;
		initializeFileSystem();
		registerTransferDecorator();
	}

	public void stop(BundleContext context) throws Exception {
		if (decoratorTracker != null) {
			decoratorTracker.close();
			decoratorTracker = null;
		}
		unregisterTransferDecorator();
		bundleContext = null;
	}

	public void unregisterAlias(String alias) {
		aliases.remove(alias);
	}

	private void unregisterTransferDecorator() {
		if (transferDecoratorRegistration != null) {
			transferDecoratorRegistration.unregister();
			transferDecoratorRegistration = null;
		}
	}

}
