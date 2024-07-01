/*
 * CapellaUserPreferenceService.java
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
package Services.CapellaUserPreference;

import Services.UserPreferenceService.UserPreferenceBaseService;
import Services.UserPreferenceService.UserPreferenceService;

/**
 * The {@linkplain CapellaUserPreferenceService} is dst adapter specific {@linkplain UserPreferenceService}
 */
public class CapellaUserPreferenceService extends UserPreferenceBaseService<CapellaUserPreference> implements ICapellaUserPreferenceService
{
    /**
     * Gets the {@linkplain Class} of {@linkplain #TUserPreference}
     */
    @Override
    protected Class<CapellaUserPreference> GetUserPreferenceType()
    {
        return CapellaUserPreference.class;
    }

    /**
     * Gets the user preference file name
     */
    @Override
    protected String GetFileName()
    {
        return "Capella";
    }
    
    /**
     * Saves one key to the {@linkplain CapellaUserPreference} file 
     * 
     * @param userPreferenceKey the {@linkplain UserPreferenceKey}
     * @param value the {@linkplain Object} value
     */
    @Override
    public void Save(UserPreferenceKey userPreferenceKey, Object value)
    {
        this.GetUserPreference().preferences.put(userPreferenceKey, value);
        this.Save();
    }
    
    /**
     * Reads one value associated to the specified {@linkplain UserPreferenceKey} from the {@linkplain CapellaUserPreference} file 
     * 
     * @param <TValue> the type of value to return
     * @param userPreferenceKey the {@linkplain UserPreferenceKey}
     * @param valueType the {@linkplain Class} of TValue
     * @param defaultValue a default value in case the value is not found
     * @return a TValue
     */
    @Override
    @SuppressWarnings("unchecked")
    public <TValue> TValue Get(UserPreferenceKey userPreferenceKey, Class<TValue> valueType, TValue defaultValue)
    {
        this.Read();
        var value = this.GetUserPreference().preferences.get(userPreferenceKey);
        
        if(valueType.isInstance(value))
        {
            return (TValue)value;
        }
        
        return defaultValue;
    }
}
