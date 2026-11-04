package org.bitrepository.pillar.integration;

import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * PostgreSQL container that only publishes the database port it is given a fixed host port for.
 * <p>
 * Testcontainers publishes every exposed port without a fixed binding using a random host port, which with rootless
 * Docker can fail with "bind: address already in use" when RootlessKit tries to bind the port on the host. The
 * PostgreSQL module currently only exposes the database port 5432, so restricting the exposed ports keeps this
 * container free of random host ports even if that changes.
 */
public class PostgresFixedPortContainer extends PostgreSQLContainer {

    public PostgresFixedPortContainer(final String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public PostgresFixedPortContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        withExposedPorts(POSTGRESQL_PORT);
    }

    public PostgresFixedPortContainer withFixedExposedPort(int hostPort, int containerPort, InternetProtocol protocol) {
        super.addFixedExposedPort(hostPort, containerPort, protocol);
        return this;
    }

}
