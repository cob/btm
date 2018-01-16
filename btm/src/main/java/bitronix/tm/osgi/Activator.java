/*
 * Copyright (C) 2006-2013 Bitronix Software (http://www.bitronix.be)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bitronix.tm.osgi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.common.XAResourceProducer;
import bitronix.tm.utils.InitializationException;

public class Activator implements BundleActivator {

	private final static Logger log = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration tmRegistration;
	private ServiceRegistration utRegistration;
	private Map<String, ServiceRegistration> dsRegistrations;

    @Override
	public void start(BundleContext context) {
		dsRegistrations = new HashMap<String, ServiceRegistration>();

        TransactionManager tm = TransactionManagerServices.getTransactionManager();
        tmRegistration = context.registerService(TransactionManager.class.getName(), tm, null);
        utRegistration = context.registerService(UserTransaction.class.getName(), tm, null);

        Configuration conf = TransactionManagerServices.getConfiguration();
        log.info(String.format("Started JTA for server ID '%s'.", conf.getServerId()));
	}

    @Override
	public void stop(BundleContext context) throws Exception {
		BitronixTransactionManager tm = TransactionManagerServices.getTransactionManager();
        tm.shutdown();

        tmRegistration.unregister();
        utRegistration.unregister();

        for (ServiceRegistration reg : dsRegistrations.values()) {
            reg.unregister();
        }
        dsRegistrations.clear();

        Configuration conf = TransactionManagerServices.getConfiguration();
        log.info(String.format("Stopped JTA for server ID '%s'.", conf.getServerId()));
	}

}
