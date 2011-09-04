/*
 * #%L
 * Bitrepository Integration
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.protocol.bitrepositorycollection.CollectionSettings;

/**
 * Settings for the ReferencePillar. TODO javadoc!!! or wait for the StandardCollection settings have been implemented.
 */
public interface PillarSettings extends CollectionSettings {

    public String getPillarId();
    
    public String getFileDirName();
    
    public String getLocalQueue();
    
    public Long getTimeToUploadValue();
    
    public TimeMeasureTYPE.TimeMeasureUnit getTimeToUploadMeasure();
    
    public Long getTimeToDownloadValue();
    
    public TimeMeasureTYPE.TimeMeasureUnit getTimeToDownloadMeasure();
}
