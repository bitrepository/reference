/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.service.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Runs a sql script as a sequence of JDBC statements.
 * <p>
 * Slightly modified version of the com.ibatis.common.jdbc.SqlScriptRunner class
 * from the iBATIS Apache project. Only removed dependency on Resource class
 * and a constructor
 */
public class SqlScriptRunner {
    private static final String DEFAULT_DELIMITER = ";";
    private static final Logger log = LoggerFactory.getLogger(SqlScriptRunner.class);
    private final Connection connection;
    private final boolean stopOnError;
    private final boolean autoCommit;
    private String delimiter = DEFAULT_DELIMITER;
    private boolean fullLineDelimiter = false;

    /**
     * @param connection  The connection to use
     * @param autoCommit  Enable autocommit
     * @param stopOnError Stop running the script, if a statement fails.
     */
    public SqlScriptRunner(Connection connection,
                           boolean autoCommit,
                           boolean stopOnError) {
        this.connection = connection;
        this.autoCommit = autoCommit;
        this.stopOnError = stopOnError;
    }

    /**
     * @param delimiter         The statement delimiter, eg. ';' for mysql.
     * @param fullLineDelimiter <code>true</code> if the delimiter used to distinguish between lines.
     */
    public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
        this.delimiter = delimiter;
        this.fullLineDelimiter = fullLineDelimiter;
    }

    /**
     * Runs an SQL script (read in using the Reader parameter).
     *
     * @param reader the source of the script
     * @throws RuntimeException if the script could not be run for any reason
     */
    public void runScript(Reader reader) {
        try {
            boolean originalAutoCommit = connection.getAutoCommit();
            if (originalAutoCommit != this.autoCommit) {
                connection.setAutoCommit(this.autoCommit);
            }
            runScript(connection, reader);
            connection.setAutoCommit(originalAutoCommit);
        } catch (Exception e) {
            throw new RuntimeException("Error running script.  Cause: " + e, e);
        }
    }

    /**
     * <p>
     * Runs an SQL script (read in using the Reader parameter) using the connection passed in.  Lines starting with
     * <code>--</code> or <code>//</code> are ignored, as well as empty lines. Lines containing "connect" are ignored as
     * the connection is made in the supplied connection.  The delimiter set determines when a multi line statement is
     * completely parsed and ready to submit to the database.  If stopOnError is set any SQLException is not caught,
     * otherwise it is just logged locally.</p>
     *
     * <p>The ResultSet for each statement is logged as a tabulator separated multi-line string.</p>
     *
     * @param conn   the connection to use for the script.
     * @param reader the source of the script.
     * @throws SQLException if any SQL errors occur.
     * @throws IOException  if there is an error reading from the Reader.
     */
    private void runScript(Connection conn, Reader reader) throws IOException, SQLException {
        StringBuilder command = null;
        LineNumberReader lineReader = new LineNumberReader(reader);
        String line;
        while ((line = lineReader.readLine()) != null) {
            if (command == null) {
                command = new StringBuilder();
            }
            String trimmedLine = line.trim();
            if (proceedWithLine(trimmedLine)) {
                if (!fullLineDelimiter
                        && trimmedLine.endsWith(getDelimiter())
                        || fullLineDelimiter
                        && trimmedLine.equals(getDelimiter())) {

                    command.append(trimLineForComment(line), 0, line
                            .lastIndexOf(getDelimiter()));
                    command.append(" ");

                    Statement statement = conn.createStatement();

                    log.debug("Executing statement: " + command);

                    boolean hasResults = false;
                    if (stopOnError) {
                        hasResults = statement.execute(command.toString());
                    } else {
                        try {
                            statement.execute(command.toString());
                        } catch (SQLException e) {
                            e.fillInStackTrace();
                            log.error("Error executing: " + command, e);
                        }
                    }

                    if (autoCommit && !conn.getAutoCommit()) {
                        conn.commit();
                    }

                    ResultSet rs = statement.getResultSet();
                    StringBuilder resultSB = new StringBuilder();
                    if (hasResults && rs != null) {
                        ResultSetMetaData md = rs.getMetaData();
                        int cols = md.getColumnCount();
                        for (int i = 0; i < cols; i++) {
                            String name = md.getColumnLabel(i);
                            resultSB.append(name).append("\t");
                        }
                        resultSB.append("\n");
                        while (rs.next()) {
                            for (int i = 0; i < cols; i++) {
                                String value = rs.getString(i);
                                resultSB.append(value).append("\t");
                            }
                            resultSB.append("\n");
                        }
                        log.info("Result: " + resultSB);
                    }

                    command = null;
                    try {
                        statement.close();
                    } catch (Exception e) {
                        // Ignore to work around a bug in Jakarta DBCP
                    }
                    Thread.yield();
                } else {
                    command.append(trimLineForComment(line));
                    command.append(" ");
                }
            }
        }
        if (!autoCommit) {
            conn.commit();
        }

    }

    private boolean proceedWithLine(String trimmedLine) {
        if (trimmedLine.startsWith("--") ||
                trimmedLine.startsWith("//")) {
            return false;
        } else if (trimmedLine.length() == 0) {
            return false;
        } else return !trimmedLine.contains("connect");
    }

    private String trimLineForComment(String line) {
        if (line.contains("--")) {
            return line.substring(0, line.indexOf("--"));
        } else {
            return line;
        }
    }

    private String getDelimiter() {
        return delimiter;
    }
}
