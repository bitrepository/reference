/*
 * #%L
 * Bitrepository Command Line
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.commandline.outputformatter;

import java.util.Collection;

import org.bitrepository.commandline.resultmodel.FileInfoResult;

/**
 * Classes for formatting and outputting results from the GetFileIDs client 
 */
public interface GetFileInfosOutputFormatter {

    /**
     * Format and output the result header 
     */
    void formatHeader();

    /**
     * Format and output a result line 
     * @param results The results to outout
     */
    void formatResult(Collection<FileInfoResult> results);
}