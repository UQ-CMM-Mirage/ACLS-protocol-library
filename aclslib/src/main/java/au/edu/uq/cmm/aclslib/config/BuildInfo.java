/*
* Copyright 2013, CMM, University of Queensland.
*
* This file is part of AclsLib.
*
* AclsLib is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* AclsLib is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with AclsLib. If not, see <http://www.gnu.org/licenses/>.
*/

package au.edu.uq.cmm.aclslib.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Extra and present build information for a Maven module from the stuff
 * that Maven puts into the META-INF directory.
 * 
 * @author steve
 */
public class BuildInfo {
	private static Logger LOG = LoggerFactory.getLogger(BuildInfo.class);
	
	private String buildTimestamp = "";
	private String groupId = "";
	private String artifactId = "";
	private String version = "";
	
	private BuildInfo() {
		super();
	}
	
	/**
	 * Extract the key information from a "pom.properties" file for
	 * a Maven model.  The file is located at the standard place on 
	 * the classpath based on the group and artifact ids.
	 * 
	 * @param groupId the Maven group id 
	 * @param artifactId the Maven artifact id
	 * @return the BuildInfo, or null if the properties can't be located or read.
	 */
	public static BuildInfo readBuildInfo(String groupId, String artifactId) {
		String path = "/META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
		InputStream is = BuildInfo.class.getResourceAsStream(path);
		if (is == null) {
			LOG.info("Cannot find build info for " + groupId + "/" + artifactId +
					" at classpath://" + path);
			return null;
		}
		try {
			return readBuildInfo(is);
		} catch (IOException ex) {
			LOG.info("Error reading classpath:" + path, ex);
			return null;
		} finally {
			try {
				is.close();
			} catch (IOException ex) {
				// ignore it
			}
		}
	}

	/**
	 * Extract the key information from a "pom.properties" file for
	 * a Maven model.  The file is read from the supplied stream.
	 * 
	 * @param is the input stream for the properties file
	 * @return the BuildInfo
	 * @throws IOException in the unlikely event that the stream can't be read.
	 */
	public static BuildInfo readBuildInfo(InputStream is) throws IOException {
		BuildInfo buildInfo = new BuildInfo();
		// Read / parse by hand because we want to snarf the comment
		// containing the timestamp.  (That's the best we have in the 
		// way of a build date.)
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#")) {
				// If it looks like it contains 
				if (line.matches(".*(?<!\\d)\\d{4}(?!\\d).*")) {
					buildInfo.buildTimestamp = line.substring(1).trim();
				}
			} else {
				String[] parts = line.split("=", 2);
				if (parts[0].equals("groupId")) {
					buildInfo.groupId = parts[1];
				} else if (parts[0].equals("artifactId")) {
					buildInfo.artifactId = parts[1];
				} else if (parts[0].equals("version")) {
					buildInfo.version = parts[1];
				} 
			}
		}
		return buildInfo;
	}

	public String getBuildTimestamp() {
		return buildTimestamp;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}
}
