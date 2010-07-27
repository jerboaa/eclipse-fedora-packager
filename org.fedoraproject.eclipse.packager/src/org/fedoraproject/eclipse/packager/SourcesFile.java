/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.fedoraproject.eclipse.packager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class SourcesFile {

	private IFile sourcesFile;
	Map<String, String> sources = new HashMap<String, String>();

	public SourcesFile(IFile sources) {
		sourcesFile = sources;
		parseSources();
	}

	private void parseSources() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(
					sourcesFile.getContents()));
			String line = br.readLine();
			while (line != null) {
				String[] source = line.split("\\s+"); //$NON-NLS-1$
				if (source.length != 2) {
					continue;
				}
				sources.put(source[1], source[0]);
				line = br.readLine();
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public Map<String, String> getSources() {
		return sources;
	}
	
	public String getSource(String key) {
		return sources.get(key);
	}

	public void checkSources(Set<String> sourcesToGet) {
		ArrayList<String> toRemove = new ArrayList<String>();
		for (String source : sourcesToGet) {
			IResource r = sourcesFile.getParent().findMember(source);
			// matched source name
			if (r != null && checkMD5(getSources().get(source), r)) {
				// match
				toRemove.add(source);
			}
		}

		sourcesToGet.removeAll(toRemove);
	}

	public Set<String> getSourcesToDownload() {
		HashSet<String> missingSources = new HashSet<String>();
		for (String source : sources.keySet()) {
			IResource r = sourcesFile.getParent().findMember(source);
			// matched source name
			if (r == null || !checkMD5(sources.get(source), r)) {
				// match
				missingSources.add(source);
			}
		}
		return missingSources;
	}

	public void deleteSource(String file) throws CoreException {
		IContainer branch = sourcesFile.getParent();
		IResource toDelete = branch.findMember(file);
		if (toDelete != null) {
			toDelete.delete(true, null);
		}
	}

	public static boolean checkMD5(String other, IResource r) {
		// open file
		File file = r.getLocation().toFile();
		String md5 = getMD5(file);

		// perform check
		return md5 == null ? false : md5.equalsIgnoreCase(other);
	}

	public static String getMD5(File file) {
		String result = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte buf[] = new byte[(int) file.length()];
			fis.read(buf); // read entire file into buf array
			result = DigestUtils.md5Hex(buf);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// Do nothing
				}
			}
		}

		return result;
	}

}
