<img src="https://sbforge.org/download/attachments/2326650/bitreplogoNY.png?version=1&modificationDate=1377984293249&api=v2" width="50" height="35">  BitRepository - Reference Code 
=========

The code for the Bit Repository reference platform.

This project includes implementations of the following components
* Commandline Client
* Reference Pillar
* WebClient
* Services
  * Alarm Service
  * Audit Trail Service
  * Monitoring Service
  * Integrity Service

For more information about the reference code, visit [Reference System](https://sbforge.org/display/BITMAG/Reference+system).

For more information about the Bit Repository project, its protocol, architecture, and components, visit [BitRepository.org](https://sbforge.org/display/BITMAG).

### Releases
[Latest Release](https://github.com/bitrepository/reference/releases/latest) and full [Changelogs](https://github.com/bitrepository/reference/releases).

### Protocol version
The newest release of the [Message-XML](https://github.com/bitrepository/message-xml/releases) protocol is used for this project.

### Running the tests on Ubuntu 26.04
Several tests start a PostgreSQL container (`postgres:18-alpine`) through Testcontainers, for example
`AuditDatabaseTest`, `AlarmDatabaseTest`, `ChecksumDatabaseTest` and `AuditTrailContributorDatabaseTest`.
On a stock Ubuntu 26.04 host with Docker these tests fail, because the container hangs on startup.

**Symptoms**
* Testcontainers fails with `ContainerLaunchException: Container startup failed for image postgres:18-alpine`.
* `docker run postgres:18-alpine` never finishes starting. The `gosu` process in the container uses about 100% CPU.
* `journalctl -k` shows `apparmor="DENIED" operation="signal" profile="docker-default"` for `comm="gosu"`.

**Cause**
Ubuntu 26.04 enables `kernel.apparmor_restrict_unprivileged_unconfined=1` by default. With it, Docker's switch to the
container's AppArmor profile becomes profile stacking (`docker-default//&unconfined`), and the default Docker profile
has no signal rule for that. AppArmor then denies signals that `gosu` needs. This is a host setting; it is not caused
by the Bit Repository code, and it does not depend on the Docker version.

**Workaround**
Check the setting, turn it off until the next reboot, and verify that the container now starts:
```bash
sysctl kernel.apparmor_restrict_unprivileged_unconfined
sudo sysctl -w kernel.apparmor_restrict_unprivileged_unconfined=0
docker run --rm --entrypoint gosu postgres:18-alpine postgres id
```
The last command should print `uid=70(postgres)`. To keep the setting after a reboot, put
`kernel.apparmor_restrict_unprivileged_unconfined = 0` in a file under `/etc/sysctl.d/` and run `sudo sysctl --system`.

This relaxes an Ubuntu hardening default for the whole machine, not only for Docker. A custom `docker-default` AppArmor
profile that allows signals to the stacked peer would avoid that, but it has not been tested here.
Ubuntu 25.04/25.10 and distributions built on 26.04 may be affected too; run the `sysctl` check to find out.

