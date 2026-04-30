package org.bitrepository.pillar.integration;

import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresFixedPortContainer extends PostgreSQLContainer {

    public PostgresFixedPortContainer(final String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public PostgresFixedPortContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
    }

    public PostgresFixedPortContainer withFixedExposedPort(int hostPort, int containerPort, InternetProtocol protocol) {
        super.addFixedExposedPort(hostPort, containerPort, protocol);
        return this;
    }

}
