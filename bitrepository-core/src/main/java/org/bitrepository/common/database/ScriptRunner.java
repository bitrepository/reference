package org.bitrepository.common.database;


import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* Slightly modified version of the com.ibatis.common.jdbc.ScriptRunner class
* from the iBATIS Apache project. Only removed dependency on Resource class
* and a constructor
*/
public class ScriptRunner {
    private static final String DEFAULT_DELIMITER = ";";
    private static Logger log = LoggerFactory.getLogger(ScriptRunner.class);

    private Connection connection;

    private boolean stopOnError;
    private boolean autoCommit;

    private PrintWriter logWriter = new PrintWriter(System.out);
    private PrintWriter errorLogWriter = new PrintWriter(System.err);

    private String delimiter = DEFAULT_DELIMITER;
    private boolean fullLineDelimiter = false;

    public ScriptRunner(Connection connection,
                        boolean autoCommit,
                        boolean stopOnError) {
        this.connection = connection;
        this.autoCommit = autoCommit;
        this.stopOnError = stopOnError;
    }

    public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
        this.delimiter = delimiter;
        this.fullLineDelimiter = fullLineDelimiter;
    }

    /**
     * Runs an SQL script (read in using the Reader parameter)
     *
     * @param reader
     *            - the source of the script
     */
    public void runScript(Reader reader) throws IOException, SQLException {
        try {
            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                if (originalAutoCommit != this.autoCommit) {
                    connection.setAutoCommit(this.autoCommit);
                }
                runScript(connection, reader);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (IOException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error running script.  Cause: " + e, e);
        }
    }

    /**
     * Runs an SQL script (read in using the Reader parameter) using the
     * connection passed in
     *
     * @param conn the connection to use for the script.
     * @param reader the source of the script.
     * @throws SQLException if any SQL errors occur.
     * @throws IOException if there is an error reading from the Reader.
     */
    private void runScript(Connection conn, Reader reader) throws IOException,
            SQLException {
        StringBuffer command = null;
        try {
            LineNumberReader lineReader = new LineNumberReader(reader);
            String line = null;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new StringBuffer();
                }
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--")) {
                    log.debug(trimmedLine);
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("//")) {
                    // Do nothing
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("--")) {
                    // Do nothing
                } else if (trimmedLine.contains("connect")) {
                    // Ignore statement as this is supplied in the connection.
                } else if (!fullLineDelimiter
                        && trimmedLine.endsWith(getDelimiter())
                        || fullLineDelimiter
                        && trimmedLine.equals(getDelimiter())) {

                    command.append(trimLineForComment(line).substring(0, line
                            .lastIndexOf(getDelimiter())));
                    command.append(" ");

                    Statement statement = conn.createStatement();

                    log.info("Executing statement: " + command.toString());

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
                            resultSB.append(name + "\t");
                        }
                        resultSB.append("\n");
                        while (rs.next()) {
                            for (int i = 0; i < cols; i++) {
                                String value = rs.getString(i);
                                resultSB.append(value + "\t");
                            }
                            resultSB.append("\n");
                        }
                        log.info("Result: " + resultSB.toString());
                    }

                    command = null;
                    try {
                        statement.close();
                    } catch (Exception e) {
                        // Ignore to workaround a bug in Jakarta DBCP
                    }
                    Thread.yield();
                } else {
                    command.append(trimLineForComment(line));
                    command.append(" ");
                }
            }
            if (!autoCommit) {
                conn.commit();
            }
        } catch (SQLException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            conn.rollback();
        }
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
