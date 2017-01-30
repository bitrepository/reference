/*
 * #%L
 * bitrepository-core
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.common.utils;

import java.util.ArrayList;
import java.util.List;

public final class ListUtils {

    public static List<String> mergeLists(List<String> list1, List<String> list2) {
        List<String> res = new ArrayList<String>(list1.size() + list2.size());
        for(String element : list1) {
            res.add(element);
        }
        for(String element : list2) {
            res.add(element);
        }
        return res;
    }
}
