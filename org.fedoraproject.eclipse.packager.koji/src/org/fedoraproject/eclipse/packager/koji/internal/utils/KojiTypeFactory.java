/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fedoraproject.eclipse.packager.koji.internal.utils;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.parser.BigDecimalParser;
import org.apache.xmlrpc.parser.BigIntegerParser;
import org.apache.xmlrpc.parser.CalendarParser;
import org.apache.xmlrpc.parser.FloatParser;
import org.apache.xmlrpc.parser.I1Parser;
import org.apache.xmlrpc.parser.I2Parser;
import org.apache.xmlrpc.parser.I8Parser;
import org.apache.xmlrpc.parser.NodeParser;
import org.apache.xmlrpc.parser.NullParser;
import org.apache.xmlrpc.parser.SerializableParser;
import org.apache.xmlrpc.parser.TypeParser;
import org.apache.xmlrpc.serializer.BigDecimalSerializer;
import org.apache.xmlrpc.serializer.BigIntegerSerializer;
import org.apache.xmlrpc.serializer.CalendarSerializer;
import org.apache.xmlrpc.serializer.FloatSerializer;
import org.apache.xmlrpc.serializer.I1Serializer;
import org.apache.xmlrpc.serializer.I2Serializer;
import org.apache.xmlrpc.serializer.I8Serializer;
import org.apache.xmlrpc.serializer.NodeSerializer;
import org.apache.xmlrpc.serializer.NullSerializer;
import org.apache.xmlrpc.serializer.SerializableSerializer;

/**
 * 
 * Type mapper for XMLRPC calls.
 *
 */
public class KojiTypeFactory extends TypeFactoryImpl {

	/**
	 * 
	 * @param pController
	 */
	public KojiTypeFactory(XmlRpcController pController) {
		super(pController);
	}

	@Override
	public TypeParser getParser(XmlRpcStreamConfig pConfig,
			NamespaceContextImpl pContext, String pURI, String pLocalName) {
		if (NullSerializer.NIL_TAG.equals(pLocalName)) {
			return new NullParser();
		} else if (I1Serializer.I1_TAG.equals(pLocalName)) {
			return new I1Parser();
		} else if (I2Serializer.I2_TAG.equals(pLocalName)) {
			return new I2Parser();
		} else if (I8Serializer.I8_TAG.equals(pLocalName)) {
			return new I8Parser();
		} else if (FloatSerializer.FLOAT_TAG.equals(pLocalName)) {
			return new FloatParser();
		} else if (NodeSerializer.DOM_TAG.equals(pLocalName)) {
			return new NodeParser();
		} else if (BigDecimalSerializer.BIGDECIMAL_TAG.equals(pLocalName)) {
			return new BigDecimalParser();
		} else if (BigIntegerSerializer.BIGINTEGER_TAG.equals(pLocalName)) {
			return new BigIntegerParser();
		} else if (SerializableSerializer.SERIALIZABLE_TAG.equals(pLocalName)) {
			return new SerializableParser();
		} else if (CalendarSerializer.CALENDAR_TAG.equals(pLocalName)) {
			return new CalendarParser();
		} else {
			return super.getParser(pConfig, pContext, pURI, pLocalName);
		}
	}
}