/*
 * Copyright 2024-2025 André Laugks <alaugks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.List;

/**
 * Eager catalog source backed by a pre-built list of trans units.
 */
public class TransUnitsCatalog extends AbstractCatalog {

	List<TransUnitInterface> transUnits;

	/**
	 * Creates a catalog backed by the given trans units.
	 *
	 * @param transUnits the trans units to serve; used directly without copying
	 */
	public TransUnitsCatalog(List<TransUnitInterface> transUnits) {
		this.transUnits = transUnits;
	}

	@Override
	public List<TransUnitInterface> getTransUnits() {
		return this.transUnits;
	}

}
