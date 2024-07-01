/*
 * ICapellaObjectBrowserViewModel.java
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
package ViewModels.CapellaObjectBrowser.Interfaces;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.netbeans.swing.outline.OutlineModel;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.NamedElement;

import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.Interfaces.IObjectBrowserBaseViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain IMagicDrawObjectBrowserViewModel} is the interface definition for the {@linkplain CapellaObjectBrowserViewModel}
 */
public interface ICapellaObjectBrowserViewModel extends IObjectBrowserBaseViewModel<ElementRowViewModel<? extends CapellaElement>>
{
    /**
     * Compute eligible rows where the represented {@linkplain Class} can be transfered,
     * and return the filtered collection for feedback application on the tree
     * 
     * @param selectedRow the collection of selected view model {@linkplain ClassRowViewModel}
     */
    void OnSelectionChanged(ElementRowViewModel<? extends CapellaElement> selectedRow);

    /**
     * Creates the {@linkplain OutlineModel} tree from the provided {@linkplain Collection} of {@linkplain EObject}
     * 
     * @param elements the {@linkplain Collection} of {@linkplain EObject}
     */
    void BuildTree(Collection<EObject> elements);
}
