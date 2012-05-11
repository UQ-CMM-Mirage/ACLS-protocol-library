/*
* Copyright 2012, CMM, University of Queensland.
*
* This file is part of AclsLib.
*
* AclsLib is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* AclsLib is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with AclsLib. If not, see <http://www.gnu.org/licenses/>.
*/

package au.edu.uq.cmm.aclslib.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class loads configurations from JSON files.
 * 
 * @author scrawley
 */
public class JsonConfigLoader <C> {
    private static final Logger LOG = 
            LoggerFactory.getLogger(JsonConfigLoader.class);
    
    private final Class<C> clazz;
    
    
    public JsonConfigLoader(Class<C> clazz) {
        super();
        this.clazz = clazz;
    }

    /**
     * Read the configuration from an input stream
     * 
     * @param configFile
     * @return the configuration or null if there was a problem reading.
     * @throws ConfigurationException 
     */
    public C readConfiguration(InputStream is)
            throws ConfigurationException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            C res = mapper.readValue(is, clazz);
            return res;
        } catch (JsonParseException ex) {
            throw new ConfigurationException("The input is not valid JSON", ex);
        } catch (JsonMappingException ex) {
            throw new ConfigurationException("The input JSON doesn't match " +
                    "the " + clazz.getCanonicalName() + " class", ex);
        } catch (IOException ex) {
            throw new ConfigurationException(
                    "IO error while reading the JSON", ex);
        } 
    }

    /**
     * Load the configuration from a file.
     * 
     * @param configFile
     * @return the configuration or null if it couldn't be found / read.
     * @throws ConfigurationException 
     */
    public C loadConfiguration(String configFile) 
            throws ConfigurationException {
        InputStream is = null;
        try {
            File cf = new File(configFile == null ? "config.json" : configFile);
                is = new FileInputStream(cf);
                return readConfiguration(is);
        } catch (IOException ex) {
            throw new ConfigurationException("Cannot open file '" + configFile + "'", ex);
        } finally {
            closeQuietly(is);
        }
    }

    /**
     * Load the configuration from a URL.  This understands any URL that the
     * JVM has a protocol handler for, and also "classpath:" URLs. 
     * @return the configuration or null
     * @param urlString the URL for the config file
     * @throws URISyntaxException 
     * @throws MalformedURLException 
     */
    public C loadConfigurationFromUrl(String urlString) 
            throws ConfigurationException {
        InputStream is = null;
        try {
            URI uri = new URI(urlString);
            if (uri.getScheme().equals("classpath")) {
                String path = uri.getSchemeSpecificPart();
                LOG.debug("Loading configuration from classpath resource '" +
                        path + "'");
                is = JsonConfigLoader.class.getClassLoader().getResourceAsStream(path);
                if (is == null) {
                    throw new IllegalArgumentException("Cannot locate resource '" +
                            path + "' on the classpath");
                }
            } else {
                LOG.debug("Loading configuration from url '" + urlString + "'");
                is = uri.toURL().openStream();
            }
            return readConfiguration(is);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(
                    "Invalid urlString '" + urlString + "'", ex);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(
                    "Invalid urlString '" + urlString + "'", ex);
        } catch (IOException ex) {
            throw new ConfigurationException("Cannot open URL input stream", ex);
        } finally {
            closeQuietly(is);
        }
    }
    
    private static void closeQuietly(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException ex) {
            // ignore
        }
    }
}
