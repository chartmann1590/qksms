/*
 * Copyright (C) 2024 Charles Hartmann
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.charles.messenger.feature.compose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.charles.messenger.R
import com.charles.messenger.common.util.Colors
import com.charles.messenger.common.util.extensions.getColorCompat

class SuggestionChipsAdapter(
    private val onChipClicked: (String) -> Unit
) : RecyclerView.Adapter<SuggestionChipsAdapter.ChipViewHolder>() {

    var suggestions: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var theme: Colors.Theme? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.suggestion_chip_item, parent, false)
        return ChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(suggestions[position], theme)
    }

    override fun getItemCount(): Int = suggestions.size

    inner class ChipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chip: Button = itemView.findViewById(R.id.suggestionChip)

        fun bind(suggestion: String, theme: Colors.Theme?) {
            chip.text = suggestion
            
            val context = itemView.context
            val isDarkMode = (context.resources.configuration.uiMode and 
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
                android.content.res.Configuration.UI_MODE_NIGHT_YES
            
            if (isDarkMode) {
                // Dark theme: dark background, white text
                chip.setBackgroundColor(context.getColorCompat(R.color.bubbleDark))
                chip.setTextColor(context.getColorCompat(R.color.textPrimaryDark))
            } else {
                // Light theme: light background, dark text
                chip.setBackgroundColor(context.getColorCompat(R.color.bubbleLight))
                chip.setTextColor(context.getColorCompat(R.color.textPrimary))
            }
            
            chip.setOnClickListener {
                onChipClicked(suggestion)
            }
        }
    }
}
