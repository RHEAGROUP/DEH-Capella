/*
 * CapellaTracedElementCollection.java
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
package Utils.Stereotypes;

import java.util.ArrayList;

import org.polarsys.capella.core.data.capellacore.CapellaElement;

import Services.MappingEngineService.IMappableThingCollection;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.Thing;

/**
 * The {@linkplain CapellaTracedElementCollection} class represents a collection of{@linkplain MappedElementRowViewModel}.
 * 
 * The {@linkplain CapellaTracedElementCollection} is usable by the {@linkplain MappingEngine} and 
 * each item of the collection represents a mapping in the {@linkplain MappingDirection.FromDstToHub}
 * 
 * Wrapping CapellaTracesCollection this way solves the following problem,
 * The {@linkplain IMappingEngine} having to know the type of things to transform at runtime to be able to invoke the right rule; 
 * Meaning that defining a rule that takes as input a {@linkplain ArrayList} of any type isn't supported because of the java generic implementation.
 */
@SuppressWarnings("serial")
public class CapellaTracedElementCollection extends ArrayList<MappedElementRowViewModel<? extends Thing, ? extends CapellaElement>> implements IMappableThingCollection { }
