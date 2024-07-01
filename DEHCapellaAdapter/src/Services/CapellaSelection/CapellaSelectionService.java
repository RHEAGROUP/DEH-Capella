/*
 * CapellaSelectionService.java
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
package Services.CapellaSelection;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import Annotations.ExludeFromCodeCoverageGeneratedReport;
import Reactive.ObservableValue;
import io.reactivex.Observable;

/**
 * The {@linkplain CapellaSelectionService} is a wrapper around the jface {@linkplain ISelectionService}
 */
@ExludeFromCodeCoverageGeneratedReport
public class CapellaSelectionService implements ICapellaSelectionService
{
    /**
     * The PartId of common browsers one can have in Capella
     */
    private final String[] BrowsersIdentifiers = {"capella.project.explorer","org.eclipse.jdt.ui.PackageExplorer","org.eclipse.ui.navigator.ProjectExplorer"};
    
    /**
     * The current class logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain ISelectionService} this {@linkplain CapellaSelectionService} wraps
     */
    private ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
    
    /**
     * Backing field for {@linkplain SelectionChanged}
     */
    private ObservableValue<EObject> selectionChanged = new ObservableValue<EObject>();
    
    /**
     * Gets an {@linkplain Observable} of {@linkplain EObject} that yields whenever the selection has changed
     * 
     * @return an {@linkplain Observable} of {@linkplain EObject}
     */
    @Override
    public Observable<EObject> SelectionChanged()
    {
        return this.selectionChanged.Observable();
    }
    
    /**
     * Initializes a new {@linkplain CapellaSelectionService}
     */
    @ExludeFromCodeCoverageGeneratedReport
    public CapellaSelectionService()
    {    
        this.selectionService.addSelectionListener(new @ExludeFromCodeCoverageGeneratedReport ISelectionListener()
        {
            /**
             * Occurs when the selection changes
             * 
             * @param part the view part involved
             * @param selection the {@linkplain ISelection}
             */
            @Override
            @ExludeFromCodeCoverageGeneratedReport
            public void selectionChanged(IWorkbenchPart part, ISelection selection)
            {
                if(selection instanceof StructuredSelection)
                {
                    var element = ((StructuredSelection) selection).getFirstElement();
                    
                    if(element instanceof EObject)
                    {
                        selectionChanged.Value((EObject)element);
                    }
                }
            }
        });
    }
    
    /**
     * Gets the selected items from the {@linkplain ISelectionService}
     * 
     * @return a {@linkplain Collection} of {@linkplain EObject}
     */
    @Override
    public Collection<EObject> GetSelection()
    {
        var selectedElements = new ArrayList<EObject>();
        
        for (var element : this.GetSelectedItems())
        {
            if(element instanceof EObject)
            {
                selectedElements.add((EObject)element);
            }
        }
        
        return selectedElements;
    }
    

    /**
     * Gets the selected items from the first browser that has some selected items
     * 
     * @return the selection as {@linkplain ISelection}
     */
    private StructuredSelection GetSelectedItems()
    {
        var window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        
        for (String browser : BrowsersIdentifiers)
        {
            var selection = window.getSelectionService().getSelection(browser);
            
            if(selection instanceof StructuredSelection)
            {
                return (StructuredSelection)selection;
            }
        }
        
        this.logger.info("No selection has been found");
        return StructuredSelection.EMPTY;
    }
}
