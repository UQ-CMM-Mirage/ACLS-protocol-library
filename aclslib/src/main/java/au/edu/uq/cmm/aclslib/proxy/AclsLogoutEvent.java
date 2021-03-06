/*
* Copyright 2012, CMM, University of Queensland.
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

package au.edu.uq.cmm.aclslib.proxy;

import au.edu.uq.cmm.aclslib.config.FacilityConfig;

@SuppressWarnings("serial")
public class AclsLogoutEvent extends AclsFacilityEvent {
    
    private String account;
    
    public AclsLogoutEvent(FacilityConfig source, String userName, String account) {
        super(source, userName);
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    @Override
    public String toString() {
        return "AclsLogoutEvent [facility=" + getFacilityName()
                + ", userName=" + getUserName() + ", account="
                + getAccount() + "]";
    }
}
