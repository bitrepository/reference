package org.bitrepository.pillar.integration;

import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.utility.DockerImageName;

public class ArtemisFixedPortContainer extends ArtemisContainer {

    public ArtemisFixedPortContainer(final String image) {
        super(image);
    }

    public ArtemisFixedPortContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
    }

    public ArtemisFixedPortContainer withFixedExposedPort(int hostPort, int containerPort, InternetProtocol protocol) {
        super.addFixedExposedPort(hostPort, containerPort, protocol);
        return this;
    }
}
