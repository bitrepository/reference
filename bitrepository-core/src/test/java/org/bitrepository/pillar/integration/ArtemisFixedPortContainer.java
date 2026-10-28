package org.bitrepository.pillar.integration;

import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.utility.DockerImageName;

/**
 * Artemis container that only publishes the port the test actually pins a host port for.
 * <p>
 * The {@link ArtemisContainer} base class exposes seven ports (8161, 61616, 5445, 5672, 61613, 1883 and 61614) and
 * Testcontainers publishes every exposed port without a fixed binding using a random host port. With rootless Docker
 * those random ports are allocated in the network namespace of the daemon but bound by RootlessKit in the host's
 * network namespace, so a port already taken on the host makes the container fail to start with
 * "bind: address already in use". Discarding the unused exposed ports limits each broker to the single port pinned
 * by the fixture.
 */
public class ArtemisFixedPortContainer extends ArtemisContainer {

    /**
     * The Artemis core protocol port, which is the only port the bitrepository message bus connects to.
     */
    private static final int ARTEMIS_CORE_PORT = 61616;

    public ArtemisFixedPortContainer(final String image) {
        this(DockerImageName.parse(image));
    }

    public ArtemisFixedPortContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        // Replaces the seven ports of the base class, which would all be published on random host ports.
        withExposedPorts(ARTEMIS_CORE_PORT);
    }

    public ArtemisFixedPortContainer withFixedExposedPort(int hostPort, int containerPort, InternetProtocol protocol) {
        super.addFixedExposedPort(hostPort, containerPort, protocol);
        return this;
    }
}
