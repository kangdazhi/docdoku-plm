/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 * 
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.core.exceptions;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;

import java.text.MessageFormat;
import java.util.Locale;

/**
 *
 * @author Florent Garin
 */
public class AccessRightException extends ApplicationException {

    private String mName;
    
    
    public AccessRightException(String pMessage) {
        super(pMessage);
    }
    
    public AccessRightException(Locale pLocale, User pUser) {
        this(pLocale, pUser.toString());
    }
    
    public AccessRightException(Locale pLocale, Account pAccount) {
        this(pLocale, pAccount.toString());
    }

    public AccessRightException(Locale pLocale, String pName) {
        super(pLocale);
        mName=pName;
    }

    @Override
    public String getLocalizedMessage() {
        String message = getBundleDefaultMessage();
        return MessageFormat.format(message,mName);     
    }
}