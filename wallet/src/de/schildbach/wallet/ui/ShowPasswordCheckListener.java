/*
 * Copyright 2012-2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.ui;

import javax.annotation.Nonnull;

import android.text.InputType;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

/**
 * @author Andreas Schildbach
 */
public final class ShowPasswordCheckListener implements OnCheckedChangeListener
{
	private int basicInputType;
	private EditText[] passwordViews;

	public ShowPasswordCheckListener(final boolean isNumeric, @Nonnull final EditText... passwordViews)
	{
		this.basicInputType = isNumeric ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT;
		this.passwordViews = passwordViews;
	}

	@Override
	public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
	{
		for (final EditText passwordView : passwordViews)
			passwordView.setInputType(basicInputType
					| (isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD));
	}
}
