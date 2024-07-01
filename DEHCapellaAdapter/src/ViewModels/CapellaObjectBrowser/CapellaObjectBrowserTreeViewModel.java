/*
 * MagicDrawObjectBrowserTreeViewModel.java
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

import javax.swing.tree.TreeModel;

import org.eclipse.emf.ecore.EObject;

import Services.CapellaSession.ICapellaSessionService;
import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;
import ViewModels.ObjectBrowser.BrowserTreeBaseViewModel;

/**
 * The {@linkplain CapellaObjectBrowserTreeViewModel} is the {@linkplain TreeModel} for the Capella object browser
 */
public class CapellaObjectBrowserTreeViewModel extends BrowserTreeBaseViewModel
{
    /**
     * The root element of the tree
     */
    private Object root;

    /**
     * Gets the root element of the tree
     * 
     * @return an {@linkplain Object}
     */
    @Override
    public Object getRoot()
    {
        return this.root;
    }
    
    /**
     * Initializes a new {@linkplain MagicDrawObjectBrowserTreeRowViewModel}
     * 
     * @param rootRowViewModel the {@linkplain RootRowViewModel}
     */
    public CapellaObjectBrowserTreeViewModel(RootRowViewModel rootRowViewModel)
    {
        this.root = rootRowViewModel;
    }
}
