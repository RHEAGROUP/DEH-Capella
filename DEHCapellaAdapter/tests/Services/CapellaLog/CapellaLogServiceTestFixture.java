/*
 * CapellaLogServiceTestFixture.java
 *
 * Copyright (c) 2020-2024 Starion Group S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski, Antoine Théate
 *
 * This file is part of DEH-Capella
 *
 * The DEH-Capella is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * The DEH-Capella is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Services.CapellaLog;

import static org.mockito.Mockito.*;

import org.apache.logging.log4j.Level;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

class CapellaLogServiceTestFixture
{
    private CapellaLogService service;
    private ILog platformLogger;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.platformLogger = mock(ILog.class);
        this.service = new CapellaLogService(this.platformLogger);
    }

    @Test
    public void VerifyAppend()
    {
        assertDoesNotThrow(() -> this.service.Append("message"));
        assertDoesNotThrow(() -> this.service.Append("message at warn level", Level.WARN));
        assertDoesNotThrow(() -> this.service.Append("message at error level", Level.ERROR));
        assertDoesNotThrow(() -> this.service.Append("message at error level with", Level.ERROR, "some", "args"));
        assertDoesNotThrow(() -> this.service.Append("message %s %s", Level.INFO, "with", "args"));
        assertDoesNotThrow(() -> this.service.Append("message with succes", true));
        assertDoesNotThrow(() -> this.service.Append("message reporting failure", false));
        assertDoesNotThrow(() -> this.service.Append("message reporting an exception", new Exception()));
        assertDoesNotThrow(() -> this.service.Append("message with %s %s reporting an exception ", new Exception(), "some", "args"));
        
        verify(this.platformLogger, times(3)).info(any(String.class));
        verify(this.platformLogger, times(3)).error(any(String.class));
        verify(this.platformLogger, times(2)).error(any(String.class), any(Exception.class));
        verify(this.platformLogger, times(1)).warn(any(String.class));
    }
}
