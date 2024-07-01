/*
 * MagicDrawObjectBrowserViewModel.java
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
package ViewModels.CapellaObjectBrowser;

import java.util.Collection;
import java.util.List;

import javax.swing.tree.TreeModel;

import org.eclipse.emf.ecore.EObject;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;

import Reactive.ObservableValue;
import Services.CapellaSession.ICapellaSessionService;
import ViewModels.ObjectBrowserBaseViewModel;
import ViewModels.CapellaObjectBrowser.Interfaces.ICapellaObjectBrowserViewModel;
import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain CapellaObjectBrowserViewModel} is the view model for the Capella object browser {@linkplain CapellaObjectBrowser}
 */
public class CapellaObjectBrowserViewModel extends ObjectBrowserBaseViewModel<ElementRowViewModel<? extends CapellaElement>> implements ICapellaObjectBrowserViewModel
{
    /**
     * The {@linkplain ICapellaSessionService} instance
     */
    protected final ICapellaSessionService SessionService;
    
    /**
     * Backing field for {@linkplain GetSelectedElement}
     */
    private ObservableValue<ElementRowViewModel<? extends CapellaElement>> selectedElement = new ObservableValue<>();
    
    /**
     * Gets the {@linkplain Observable} of {@linkplain ElementRowViewModel} that yields the selected element
     * 
     * @return an {@linkplain Observable} of {@linkplain ElementRowViewModel}
     */
    @Override
    public Observable<ElementRowViewModel<? extends CapellaElement>> GetSelectedElement()
    {
        return this.selectedElement.Observable();
    }
    
    /**
     * Initializes a new {@linkplain CapellaObjectBrowserViewModel}
     * 
     * @param sessionService the {@linkplain ICapellaSessionService}
     */
    public CapellaObjectBrowserViewModel(ICapellaSessionService sessionService)
    {
        this.SessionService = sessionService;
    }
    
    /**
     * Occurs when the selection changes
     * 
     * @param selectedRow the selected {@linkplain ClassRowViewModel}
     */
    @Override
    public void OnSelectionChanged(ElementRowViewModel<? extends CapellaElement> selectedRow)
    {
        this.selectedElement.Value(selectedRow);            
    }
    
    /**
     * Creates the {@linkplain OutlineModel} tree from the provided {@linkplain Collection} of {@linkplain EObject}
     * 
     * @param elements the {@linkplain Collection} of {@linkplain EObject}
     */
    @Override
    public void BuildTree(Collection<EObject> elements)
    {
        RootRowViewModel rootRowViewModel;
        
        if(elements != null)
        {
            rootRowViewModel = new RootRowViewModel("", (List<EObject>)elements);
        }
        else
        {
            rootRowViewModel = this.SessionService.GetModels();
        }
        
        this.browserTreeModel.Value(DefaultOutlineModel.createOutlineModel(
                new CapellaObjectBrowserTreeViewModel(rootRowViewModel),
                new CapellaObjectBrowserTreeRowViewModel(), true));
                
        this.isTheTreeVisible.Value(true);
    }

    /**
     * Updates this view model {@linkplain TreeModel}
     * 
     * @param isConnected a value indicating whether the session is open
     */
    protected void UpdateBrowserTrees(Boolean isConnected)
    {
    }
}
