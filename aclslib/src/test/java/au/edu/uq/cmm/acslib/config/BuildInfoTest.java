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

package au.edu.uq.cmm.acslib.config;

import static org.junit.Assert.*;

import org.junit.Test;

import au.edu.uq.cmm.aclslib.config.BuildInfo;

public class BuildInfoTest {

	@Test
	public void test() {
		BuildInfo bi = BuildInfo.readBuildInfo("au.edu.uq.cmm", "aclslib");
		assertNotNull(bi);
		assertEquals("aclslib", bi.getArtifactId());
		assertEquals("au.edu.uq.cmm", bi.getGroupId());
		assertEquals("0.0.1-SNAPSHOT", bi.getVersion());
		assertEquals("Thu May 30 23:22:10 EST 2013", bi.getBuildTimestamp());
		
		assertNull(BuildInfo.readBuildInfo("au.edu.uq.cmm", "nothing"));
		
		bi = BuildInfo.readBuildInfo("au.edu.uq.cmm", "empty");
		assertNotNull(bi);
		assertEquals("", bi.getArtifactId());
		assertEquals("", bi.getGroupId());
		assertEquals("", bi.getVersion());
		assertEquals("", bi.getBuildTimestamp());
		
		bi = BuildInfo.readBuildInfo("au.edu.uq.cmm", "nodate");
		assertNotNull(bi);
		assertEquals("nodate", bi.getArtifactId());
		assertEquals("au.edu.uq.cmm", bi.getGroupId());
		assertEquals("0.0.1-SNAPSHOT", bi.getVersion());
		assertEquals("", bi.getBuildTimestamp());
	}

}
