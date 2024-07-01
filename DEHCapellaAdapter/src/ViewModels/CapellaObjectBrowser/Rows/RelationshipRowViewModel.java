/*
 * PackageRowViewModel.java
 *
 * Copyright (c) 2020-2024 Starion Group S.A.
 *
 * Author: Sam GerenÃ©, Alex Vorobiev, Nathanael Smiechowski, Antoine ThÃ©ate
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
package ViewModels.CapellaObjectBrowser.Rows;

import org.polarsys.capella.core.data.cs.AbstractDeploymentLink;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.capellacore.Relationship;
import org.polarsys.capella.core.data.cs.ComponentRealization;

import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain RelationshipRowViewModel} is is the row view model that represents a {@linkplain Component}
 */
public class RelationshipRowViewModel extends ElementRowViewModel<Relationship>
{
    /**
     * Initializes a new {@linkplain RelationshipRowViewModel}
     * 
     * @param parent the {@linkplain IRowViewModel} parent of this view model
     * @param element the {@linkplain Component} represented in this row view model
     */
    public RelationshipRowViewModel(IElementRowViewModel<?> parent, Relationship element)
    {
        super(parent, element);
        this.SetName();
    }
    
    
    private void SetName()
    {        
        if(this.GetElement() instanceof AbstractDeploymentLink && ((AbstractDeploymentLink)this.GetElement()).getDeployedElement() != null)
        {
            this.SetName(String.format("➤ %s", ((AbstractDeploymentLink)this.GetElement()).getDeployedElement().getName()));
            return;
        }
        
        if(this.GetElement() instanceof ComponentRealization && ((ComponentRealization)this.GetElement()).getRealizedComponent() != null)
        {
            this.SetName(String.format("▲ %s", ((ComponentRealization)this.GetElement()).getRealizedComponent().getName()));
            return;
        }
        
        this.SetName(this.GetElement().getClass().getSimpleName().replace("Impl", ""));
    }
}
