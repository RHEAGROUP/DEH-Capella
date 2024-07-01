/*
 * OpenAdapterPanelCommand.java
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
package Commands;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import Views.ViewParts.BaseViewPart;

/**
 * The {@linkplain OpenHubPanelCommand} is the command that show or hide the Hub Panel
 * 
 * @param <TViewPart> the type of {@linkplain BaseViewPart} that this handler handles
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public abstract class OpenAdapterPanelCommand<TViewPart extends BaseViewPart<?,?>> extends AbstractHandler
{
    /**
     * The current class logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * The view this command handler handles
     */
    private TViewPart view;
    
    /**
     * Initializes a new {@linkplain OpenAdapterPanelCommand}
     * 
     *  @param view the {@linkplain BaseViewPart} that this handler handles
     */
    protected OpenAdapterPanelCommand(TViewPart view)
    {
        this.view = view;
    }
    
    /**
     * Executes with the map of parameter values by name in the parameter event
     * 
     * @param event the {@linkplain ExecutionEvent}
     * @return an {@linkplain Object}
     * @throws ExecutionException might throw {@linkplain ExecutionException}
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        try
        {
            this.view.ShowHide(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
            return event;
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
            return null;
        }
    }    
}
