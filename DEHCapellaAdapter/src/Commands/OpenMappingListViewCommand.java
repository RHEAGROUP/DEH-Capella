/*
 * OpenMappingListViewCommand.java
 *
 * Copyright (c) 2020-2024 Starion Group S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
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
package Commands;

import Views.CapellaMappingListViewPanel;

/**
 * The {@linkplain OpenMappingListViewCommand} is the command handler for {@linkplain CapellaMappingListViewPanel}
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public final class OpenMappingListViewCommand extends OpenAdapterPanelCommand<CapellaMappingListViewPanel>
{
    /**
     * Initializes a new {@linkplain OpenMappingListViewCommand}
     */
    public OpenMappingListViewCommand()
    {
        super(new CapellaMappingListViewPanel());
    }
}
