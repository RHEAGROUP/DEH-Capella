/*
 * PropertyRowViewModel.java
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
package ViewModels.CapellaObjectBrowser.Rows;

import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.polarsys.capella.core.data.capellacore.AbstractPropertyValue;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.datavalue.DataValue;
import org.reflections.ReflectionUtils;

import Reactive.ObservableCollection;
import ViewModels.CapellaObjectBrowser.Interfaces.IElementRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import ViewModels.ObjectBrowser.Interfaces.IRowViewModel;

/**
 * The {@linkplain PropertyRowViewModel} is the row view model that represents a {@linkplain Property}
 */
public class PropertyRowViewModel extends ElementRowViewModel<Property> implements IHaveContainedRows<PropertyValueBaseRowViewModel<? extends CapellaElement>>
{
    /**
     * The {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    private ObservableCollection<PropertyValueBaseRowViewModel<? extends CapellaElement>> containedRows = new ObservableCollection<>();
        
    /**
     * Gets the contained row the implementing view model has
     * 
     * @return An {@linkplain ObservableCollection} of {@linkplain IElementRowViewModel}
     */
    @Override
    public ObservableCollection<PropertyValueBaseRowViewModel<? extends CapellaElement>> GetContainedRows()
    {
        return this.containedRows;
    }
    
    /**
     * Initializes a new {@linkplain ComponentRowViewModel}
     * 
     * @param parent the {@linkplain IRowViewModel} parent of this view model
     * @param element the {@linkplain Property} represented by this row view model
     */
    public PropertyRowViewModel(IElementRowViewModel<?> parent, Property element)
    {
        super(parent, element);
        this.ComputeContainedRows();
    }
    
    /**
     * Computes the contained rows of this row view model
     */
    @Override
    public void ComputeContainedRows() 
    {
        for (var element : this.GetElement().eContents())
        {
            if(element instanceof AbstractPropertyValue)
            {
                this.GetContainedRows().add(new PropertyAbstractPropertyValueRowViewModel(this, (AbstractPropertyValue)element));
            }
        }
        
        this.ProcessDataValues();
    }

    /**
     * Process data values for the represented {@linkplain Property}
     */
    @SuppressWarnings("unchecked")
    private void ProcessDataValues()
    {
       final String patternString = "getOwned";

        Pattern pattern = Pattern.compile(patternString);

        var allMethods = ReflectionUtils.getAllMethods(
                this.GetElement().getClass(), 
                x -> x.getModifiers() == Modifier.PUBLIC && pattern.matcher(x.getName()).find());
        
        for (var method : allMethods)
        {
            try
            {
                var invoke = method.invoke(this.GetElement());
                
                if(invoke instanceof DataValue)
                {
                    this.GetContainedRows().add(
                            new PropertyDataValueRowViewModel(
                                    this, ((DataValue)invoke), method.getName().replaceAll(patternString, "")));
                }                
                
            } 
            catch (Exception exception)
            {
                LogManager.getLogger().catching(exception);
            }
        }
    }
}
