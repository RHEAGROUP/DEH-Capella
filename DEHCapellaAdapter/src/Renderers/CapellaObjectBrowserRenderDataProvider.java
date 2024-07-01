/*
 * CapellaObjectBrowserRenderDataProvider.java
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
package Renderers;

import java.awt.Color;

import javax.swing.Icon;

import org.polarsys.capella.core.data.capellacore.AbstractPropertyValue;
import org.polarsys.capella.core.data.capellacore.Relationship;
import org.polarsys.capella.core.data.capellacore.Structure;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.capellamodeller.SystemEngineering;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.datavalue.DataValue;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;

import Utils.ImageLoader.ImageLoader;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.RequirementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;
import ViewModels.ObjectBrowser.RenderDataProvider.ObjectBrowserRenderDataProvider;
import cdp4common.commondata.ClassKind;

/**
 * The {@linkplain MagicDrawObjectBrowserRenderDataProvider} is the override {@linkplain ObjectBrowserRenderDataProvider} for the {@linkplain MagicDrawObjectBrowser}
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class CapellaObjectBrowserRenderDataProvider extends ObjectBrowserRenderDataProvider
{
    /**
     * Gets the specified row view model node name
     * 
     * @param rowViewModel the row view model to get the name from
     * @return a {@linkplain String}
     */
    @Override
    public String getDisplayName(Object rowViewModel)
    {
        if(rowViewModel instanceof IElementRowViewModel)
        {
            return ((IElementRowViewModel<?>)rowViewModel).GetName();
        }

        return "undefined";
    }
    
    /**
     * Gets an value indicating to the tree whether the display name for this object should use HTMLrendering
     * 
     * @param rowViewModel the row view model
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean isHtmlDisplayName(Object rowViewModel)
    {
        return rowViewModel instanceof RequirementRowViewModel;
    }

    /**
     * Gets the background color to be used for rendering this node. Returns
     * null if the standard table background or selected color should be used.
     * 
     * @param rowViewModel the row view model
     * @return a {@linkplain Color}
     */
    @Override
    public Color getBackground(Object rowViewModel)
    {
        if (rowViewModel instanceof IRowViewModel)
        {
            if(((IRowViewModel)rowViewModel).GetIsSelected())
            {
                return new Color(104, 143, 184);
            }
            
            if(((IRowViewModel)rowViewModel).GetIsHighlighted())
            {
                return Color.YELLOW;
            }            
        }
        
        return Color.WHITE;
    }

    /**
     * Gets the foreground color to be used for rendering this node. Returns
     * null if the standard table foreground or selected color should be used.
     * 
     * @param rowViewModel the row view model
     * @return a {@linkplain Color}
     */
    @Override
    public Color getForeground(Object rowViewModel)
    {
        return null;
    }

    /**
     * Gets a description for this object suitable for use in a tool tip. 
     * 
     * @param rowViewModel the row view model
     * @return a {@linkplain String}
     */
    @Override
    public String getTooltipText(Object rowViewModel)
    {
        return null;
    }

    /**
     * Gets the background color to be used for rendering this node. Returns
     * null if the standard table background or selected color should be used.
     * 
     * @param rowViewModel the row view model
     * @return a {@linkplain Color}
     */
    @Override
    public Icon getIcon(Object rowViewModel)
    {
        if(rowViewModel instanceof IElementRowViewModel)
        {
            var element = (IElementRowViewModel<?>)rowViewModel;
            
            if(element.GetElement() instanceof SystemEngineering)
            {
                return ImageLoader.GetDstIcon();
            }
            else if(element.GetElement() instanceof Structure)
            {
                return ImageLoader.GetIcon(ImageLoader.ThingFolder, "parametergroup.png");
            }
            else if(element.GetElement() instanceof Relationship)
            {
                return ImageLoader.GetIcon(ClassKind.Relationship);
            }
            else if(element.GetElement() instanceof ComponentPort)
            {
                return ImageLoader.GetIcon(ClassKind.ElementUsage);
            }
            else if(element.GetElement() instanceof Component)
            {
                return ImageLoader.GetIcon(ClassKind.ElementDefinition);
            }
            else if(element.GetElement() instanceof Part)
            {
                return ImageLoader.GetIcon(ClassKind.ElementUsage);
            }
            else if(element.GetElement() instanceof Requirement)
            {
                return ImageLoader.GetIcon(ClassKind.Requirement);
            }
            else if(element.GetElement() instanceof RequirementsPkg)
            {
                return ImageLoader.GetIcon(ClassKind.RequirementsSpecification);
            }
            else if(element.GetElement() instanceof Property)
            {
                return ImageLoader.GetIcon(ClassKind.Parameter);
            }
            else if(element.GetElement() instanceof AbstractPropertyValue)
            {
                return ImageLoader.GetIcon(ClassKind.Parameter);
            }
            else if(element.GetElement() instanceof DataValue)
            {
                return ImageLoader.GetIcon(ClassKind.Parameter);
            }
        }
        
        return ImageLoader.GetDstIcon();
    }
}
