/*
 * ICapellaUserPreferenceService.java
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

/**
 * The {@linkplain ICapellaUserPreferenceService} is the interface definition for the {@linkplain CapellaUserPreferenceService}
 */
public interface ICapellaUserPreferenceService
{
    /**
     * Saves one key to the {@linkplain CapellaUserPreference} file 
     * 
     * @param UserPreferenceKey the {@linkplain UserPreferenceKey}
     * @param value the {@linkplain Object} value
     */
    void Save(UserPreferenceKey UserPreferenceKey, Object value);

    /**
     * Reads one value associated to the specified {@linkplain UserPreferenceKey} from the {@linkplain CapellaUserPreference} file 
     * 
     * @param <TValue> the type of value to return
     * @param userPreferenceKey the {@linkplain UserPreferenceKey}
     * @param valueType the {@linkplain Class} of TValue
     * @param defaultValue a default value in case the value is not found
     * @return a TValue
     */
    <TValue> TValue Get(UserPreferenceKey userPreferenceKey, Class<TValue> valueType, TValue defaultValue);
    
}
