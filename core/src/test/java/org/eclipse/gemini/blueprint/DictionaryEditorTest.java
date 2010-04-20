/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint;

import java.util.Dictionary;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class DictionaryEditorTest extends AbstractDependencyInjectionSpringContextTests {

	private Dictionary dictionary;


	/**
	 * @param dictionary The dictionary to set.
	 */
	public void setDictionary(Dictionary property) {
		this.dictionary = property;
	}

	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
		beanFactory.registerCustomEditor(Dictionary.class, PropertiesEditor.class);
		super.customizeBeanFactory(beanFactory);
	}

	protected String[] getConfigLocations() {
		//return new String[] { "/org/eclipse/gemini/blueprint/dict-editor.xml" };
		return null;
	}

	public void tstInjection() {
		assertNotNull(dictionary);
	}

	public void tstInjectedValue() {
		assertSame(applicationContext.getBean("dictionary"), dictionary);
	}

	public void testSanity() throws Exception {
		System.out.println(String[][].class);
	}
}