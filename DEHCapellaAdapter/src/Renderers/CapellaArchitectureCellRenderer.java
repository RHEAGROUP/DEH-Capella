/*
 * CapellaArchitectureCellRenderer.java
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
package Renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import Enumerations.CapellaArchitecture;
import ViewModels.Interfaces.IHaveTargetArchitecture;
import ViewModels.Rows.MappedElementRowViewModel;

/**
 * The {@linkplain CapellaArchitectureCellRenderer} is the custom cell renderer for {@linkplain CapellaArchitecture} cells
 */
@SuppressWarnings("serial")
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class CapellaArchitectureCellRenderer extends JComboBox<CapellaArchitecture> implements TableCellRenderer
{
    /**
     * Initializes a new {@linkplain CapellaArchitectureCellRenderer}
     */
    public CapellaArchitectureCellRenderer()
    {
        this.setOpaque(true);
    }

    /**
     * Returns the component used for drawing the cell. This method is
     * used to configure the renderer appropriately before drawing.
     * 
     * @param table the {@linkplain JTable} instance
     * @param cellValue the value
     * @param isSelected a value indicating whether the current row is selected
     * @param hasFocus a value indicating whether the current row has focus
     * @param row the row number
     * @param columnIndex the column number
     * @return a {@linkplain JComboBox}
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object cellValue, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex)
    {
        var rowViewModel = table.getModel().getValueAt(rowIndex, 0);
        
        if(!(rowViewModel instanceof IHaveTargetArchitecture))
        {
            var label = new JLabel();
            label.setBackground(Color.WHITE);
            return label;
        }
        
        var container = new JPanel();
        container.getInsets(new Insets(2, 2, 2, 2));
        this.setEditable(true);
        this.setToolTipText("Select the target Architecture");
        
        this.setModel(new DefaultComboBoxModel<CapellaArchitecture>(CapellaArchitecture.values()));
        this.setSelectedIndex(3);
                
        this.setSelectedItem(((IHaveTargetArchitecture)rowViewModel).GetTargetArchitecture());
        
        return this;
    }
}
