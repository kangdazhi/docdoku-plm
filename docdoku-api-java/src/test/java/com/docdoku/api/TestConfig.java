/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.api;

import com.docdoku.api.client.ApiClient;

import java.util.logging.Logger;

public class TestConfig {

    public static String URL;
    public static String LOGIN;
    public static String PASSWORD;
    public static String WORKSPACE;
    public static String EMAIL;
    public static String NAME;
    public static String LANGUAGE;
    public static String TIMEZONE;
    public static boolean DEBUG;
    public static ApiClient GUEST_CLIENT;
    public static ApiClient BASIC_CLIENT;
    public static ApiClient JWT_CLIENT;
    public static ApiClient COOKIE_CLIENT;
    private static final Logger LOGGER = Logger.getLogger(TestConfig.class.getName());

    static {
        parseProperties();
        createClients();
    }

    private static void parseProperties() {
        URL = System.getProperty("url") != null ? System.getProperty("url") : "http://localhost:8080/api";
        LOGIN = System.getProperty("login") != null ? System.getProperty("login") : "test";
        NAME = System.getProperty("name") != null ? System.getProperty("name") : "test";
        PASSWORD = System.getProperty("password") != null ? System.getProperty("password") : "test";
        WORKSPACE = System.getProperty("workspace") != null ? System.getProperty("workspace") : "test";
        EMAIL = System.getProperty("email") != null ? System.getProperty("email") : "";
        LANGUAGE = System.getProperty("language") != null ? System.getProperty("language") : "en";
        TIMEZONE = System.getProperty("timezone") != null ? System.getProperty("timezone") : "CET";
        DEBUG = System.getProperty("debug") != null ? Boolean.parseBoolean(System.getProperty("debug")) : false;
    }

    private static void createClients() {
        GUEST_CLIENT = new DocdokuPLMClient(URL, DEBUG).getClient();
        BASIC_CLIENT = new DocdokuPLMBasicClient(URL, LOGIN, PASSWORD, DEBUG).getClient();
        JWT_CLIENT = new DocdokuPLMBasicClient(URL, LOGIN, PASSWORD, DEBUG).getClient();
        COOKIE_CLIENT = new DocdokuPLMCookieClient(URL, LOGIN, PASSWORD, DEBUG).getClient();
    }
}
