package org.openstreetmap.atlas.utilities.cli.operations;

/**
 * All commands must have a remote user on a particular host
 *
 * @author cstaylor
 */
public interface Operation
{
    int STANDARD_SUCCESS_CODE = 0;

    /**
     * @param username
     *            the SSH username for connecting with the remote server
     * @return fluent interface returns this
     */
    Operation asUser(String username);

    /**
     * @param hostname
     *            the hostname or IP of the remote server
     * @return fluent interface returns this
     */
    Operation onHost(String hostname);

    /**
     * @param portNumber
     *            the non-standard SSH port we should use
     * @return fluent interface returns this
     */
    Operation onPort(int portNumber);
}
